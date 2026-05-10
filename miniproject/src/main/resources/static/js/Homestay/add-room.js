// ════════════════════════════════════════════════════════
//  add-room.js  — จัดการฟอร์มเพิ่มห้องพัก (Base64)
// ════════════════════════════════════════════════════════

'use strict';

let selectedFiles = [];

function handleFileSelect(fileList) {
    addFiles(Array.from(fileList));
    document.getElementById('roomImages').value = '';
}

function handleDragOver(e) {
    e.preventDefault();
    document.getElementById('dropZone').classList.add('dragover');
}
function handleDragLeave() {
    document.getElementById('dropZone').classList.remove('dragover');
}
function handleDrop(e) {
    e.preventDefault();
    document.getElementById('dropZone').classList.remove('dragover');
    addFiles(Array.from(e.dataTransfer.files));
}

function addFiles(newFiles) {
    const ALLOWED  = ['image/jpeg', 'image/png', 'image/webp'];
    const MAX_SIZE = 5 * 1024 * 1024;
    let errMsg = '';

    newFiles.forEach(file => {
        if (!ALLOWED.includes(file.type)) {
            errMsg = `"${file.name}" ไม่รองรับ — ใช้ได้เฉพาะ JPG, PNG, WebP`;
            return;
        }
        if (file.size > MAX_SIZE) {
            errMsg = `"${file.name}" มีขนาดเกิน 5MB`;
            return;
        }
        const dup = selectedFiles.some(f => f.name === file.name && f.size === file.size);
        if (!dup) selectedFiles.push(file);
    });

    document.getElementById('imageError').textContent = errMsg;
    renderPreviews();
}

function renderPreviews() {
    const grid        = document.getElementById('previewGrid');
    const placeholder = document.getElementById('uploadPlaceholder');

    grid.innerHTML = '';

    if (selectedFiles.length === 0) {
        grid.style.display = 'none';
        placeholder.style.display = 'flex';
        placeholder.innerHTML = `
            <i class="fas fa-cloud-arrow-up"></i>
            <p>คลิกหรือลากไฟล์มาวางที่นี่</p>
            <small>JPG, PNG, WebP — หลายรูปพร้อมกัน ขนาดสูงสุด 5MB/รูป</small>`;
        return;
    }

    placeholder.style.display = 'none';
    grid.style.display = 'grid';

    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const sizeText = file.size >= 1024 * 1024
                ? (file.size / (1024 * 1024)).toFixed(1) + ' MB'
                : Math.round(file.size / 1024) + ' KB';

            const card = document.createElement('div');
            card.className = 'preview-card';
            card.innerHTML = `
                <img src="${e.target.result}" alt="${file.name}">
                <button type="button" class="remove-btn" onclick="removeFile(${index})" title="ลบรูปนี้">
                    <i class="fas fa-xmark"></i>
                </button>
                <div class="file-size-label">${sizeText}</div>
            `;
            grid.appendChild(card);
        };
        reader.readAsDataURL(file);
    });

    const badge = document.getElementById('imageCountBadge');
    if (badge) {
        badge.textContent = selectedFiles.length + ' รูป';
        badge.style.display = 'inline-block';
    }
}

function removeFile(index) {
    selectedFiles.splice(index, 1);
    renderPreviews();
}

function selectStatus(radio) {
    document.querySelectorAll('.status-option').forEach(el => el.classList.remove('selected'));
    radio.closest('.status-option').classList.add('selected');
}

function toggleChip(label) {
    const cb = label.querySelector('input[type="checkbox"]');
    setTimeout(() => label.classList.toggle('checked', cb.checked), 0);
}

function validateField(id, errorId, rules) {
    const el  = document.getElementById(id);
    const err = document.getElementById(errorId);
    if (!el || !err) return true;
    const val = el.value.trim();
    for (const { check, msg } of rules) {
        if (!check(val)) {
            el.classList.add('is-invalid');
            el.classList.remove('is-valid');
            err.textContent = msg;
            return false;
        }
    }
    el.classList.remove('is-invalid');
    el.classList.add('is-valid');
    err.textContent = '';
    return true;
}

// ✅ แปลง File → Base64 string
function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload  = e => resolve(e.target.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

// ─── Submit — แปลงรูปเป็น Base64 แล้วส่งเป็น JSON ───
async function submitForm() {
    const ok = [
    validateField('typename', 'typenameError', [
        { check: v => v, msg: 'กรุณาเลือกประเภทที่พัก' }
    ]),
    validateField('bedtype', 'bedtypeError', [
        { check: v => v, msg: 'กรุณาเลือกประเภทเตียง' }
    ]),
    validateField('pricepernight', 'priceError', [
        { check: v => v,                 msg: 'กรุณากรอกราคาต่อคืน' },
        { check: v => parseFloat(v) > 0, msg: 'ราคาต้องมากกว่า 0' }
    ]),
    validateField('maxguest', 'maxguestError', [
        { check: v => v,                                      msg: 'กรุณากรอกจำนวนผู้เข้าพัก' },
        { check: v => parseInt(v) >= 1 && parseInt(v) <= 10, msg: 'ต้องอยู่ระหว่าง 1–10 คน' }
    ]),
    validateField('totalrooms', 'totalroomsError', [
        { check: v => v,                msg: 'กรุณากรอกจำนวนห้อง' },
        { check: v => parseInt(v) >= 1, msg: 'จำนวนห้องต้องมากกว่า 0' }
    ]),
    validateField('description', 'descriptionError', [
        { check: v => v, msg: 'กรุณากรอกคำอธิบาย' }
    ]),
    validateField('roomcondition', 'roomconditionError', [
        { check: v => v, msg: 'กรุณากรอกเงื่อนไขการเข้าพัก' }
    ]),
].every(Boolean);


    if (!ok) {
        document.querySelector('.is-invalid')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    // ✅ แปลงไฟล์ทุกรูปเป็น Base64
    const base64Images = await Promise.all(selectedFiles.map(f => fileToBase64(f)));

    // ✅ รวบรวม facilitiesIds ที่เลือก
    const facilitiesIds = Array.from(
    document.querySelectorAll('input[name="facilitiesIds"]:checked')
    ).map(cb => cb.value);

    const payload = {
        homestayid:    document.querySelector('input[name="homestayid"]').value,
        typename:      document.getElementById('typename').value,
        bedtype:       document.getElementById('bedtype').value,
        pricepernight: document.getElementById('pricepernight').value,
        maxguest:      document.getElementById('maxguest').value,
        totalrooms:    document.getElementById('totalrooms').value,
        description:   document.getElementById('description').value,
        roomcondition: document.getElementById('roomcondition').value,
        status:        document.getElementById('status').value,  
        facilitiesIds: facilitiesIds,
        images:        base64Images
    };

    const facErr = document.getElementById('facilitiesError');
if (facilitiesIds.length === 0) {
    facErr.textContent = 'กรุณาเลือกสิ่งอำนวยความสะดวกอย่างน้อย 1 รายการ';
    facErr.style.display = 'block';
    if (ok) { facErr.scrollIntoView({ behavior: 'smooth', block: 'center' }); }
    return;
} else {
    facErr.textContent = '';
    facErr.style.display = 'none';
}

// ✅ เช็ครูป
const imgErr = document.getElementById('imageError');
if (selectedFiles.length === 0) {
    imgErr.textContent = 'กรุณาอัปโหลดรูปภาพอย่างน้อย 1 รูป';
    imgErr.style.display = 'block';
    imgErr.scrollIntoView({ behavior: 'smooth', block: 'center' });
    return;
} else {
    imgErr.textContent = '';
    imgErr.style.display = 'none';
}

if (!ok) {
    document.querySelector('.is-invalid')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    return;
}
    // disable ปุ่ม
    const btn = document.querySelector('.btn-save');
    btn.disabled = true;
    btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> กำลังบันทึก...`;

    fetch('/addroom', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            window.location.href = '/owner/rooms?homestayid=' + payload.homestayid;
        } else {
            alert('เกิดข้อผิดพลาด: ' + data.message);
            btn.disabled = false;
            btn.innerHTML = `<i class="fas fa-floppy-disk"></i> บันทึกประเภทที่พัก`;
        }
    })
    .catch(() => {
        alert('เกิดข้อผิดพลาด กรุณาลองใหม่');
        btn.disabled = false;
        btn.innerHTML = `<i class="fas fa-floppy-disk"></i> บันทึกประเภทที่พัก`;
    });
}

// ─── Live validation ───
['typename', 'bedtype', 'pricepernight', 'maxguest', 'totalrooms'].forEach(id => {
    const map = {
        typename:      ['typenameError',   [{ check: v => v, msg: 'กรุณาเลือกประเภทที่พัก' }]],
        bedtype:       ['bedtypeError',    [{ check: v => v, msg: 'กรุณาเลือกประเภทเตียง' }]],
        pricepernight: ['priceError',      [{ check: v => v, msg: 'กรุณากรอกราคาต่อคืน' }, { check: v => parseFloat(v) > 0, msg: 'ราคาต้องมากกว่า 0' }]],
        maxguest:      ['maxguestError',   [{ check: v => v, msg: 'กรุณากรอกจำนวนผู้เข้าพัก' }, { check: v => parseInt(v) >= 1 && parseInt(v) <= 10, msg: 'ต้องอยู่ระหว่าง 1–10 คน' }]],
        totalrooms:    ['totalroomsError', [{ check: v => v, msg: 'กรุณากรอกจำนวนห้อง' }, { check: v => parseInt(v) >= 1, msg: 'จำนวนห้องต้องมากกว่า 0' }]],
    };
    document.getElementById(id)?.addEventListener('blur', () => {
        if (map[id]) validateField(id, map[id][0], map[id][1]);
    });
});