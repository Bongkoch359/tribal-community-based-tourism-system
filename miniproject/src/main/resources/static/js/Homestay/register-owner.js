// ============================================================
//  register-owner.js
// ============================================================

'use strict';

const homestayImages = {};

// ============================================================
//  HELPER
// ============================================================
function showError(input, spanId, msg) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
    const span = document.getElementById(spanId);
    if (span) { span.textContent = '⚠ ' + msg; span.style.display = 'block'; }
}
function showValid(input, spanId) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
    const span = document.getElementById(spanId);
    if (span) { span.style.display = 'none'; }
}
function showSpanError(spanId, msg) {
    const span = document.getElementById(spanId);
    if (span) { span.textContent = '⚠ ' + msg; span.style.display = 'block'; }
}
function hideSpan(spanId) {
    const span = document.getElementById(spanId);
    if (span) { span.style.display = 'none'; }
}

// ============================================================
//  STEP 1 — VALIDATORS
// ============================================================
function validateFirstname() {
    const input = document.getElementById('firstname');
    const val = input.value.trim();
    if (val === '') { showError(input, 'firstnameError', 'กรุณากรอกชื่อ'); return false; }
    if (!/^[ก-๙a-zA-Z]+$/.test(val)) { showError(input, 'firstnameError', 'ชื่อใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษเท่านั้น'); return false; }
    if (val.length < 2 || val.length > 20) { showError(input, 'firstnameError', 'ชื่อต้องมีความยาว 2–20 ตัวอักษร'); return false; }
    showValid(input, 'firstnameError'); return true;
}
function validateLastname() {
    const input = document.getElementById('lastname');
    const val = input.value.trim();
    if (val === '') { showError(input, 'lastnameError', 'กรุณากรอกนามสกุล'); return false; }
    if (!/^[ก-๙a-zA-Z]+$/.test(val)) { showError(input, 'lastnameError', 'นามสกุลใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษเท่านั้น'); return false; }
    if (val.length < 2 || val.length > 20) { showError(input, 'lastnameError', 'นามสกุลต้องมีความยาว 2–20 ตัวอักษร'); return false; }
    showValid(input, 'lastnameError'); return true;
}
function validateEmail() {
    const input = document.getElementById('email');
    const val = input.value;
    if (val === '') { showError(input, 'emailError', 'อีเมลต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'emailError', 'อีเมลต้องไม่มีช่องว่างระหว่างตัวอักษร'); return false; }
    if (!/^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/.test(val)) { showError(input, 'emailError', 'รูปแบบอีเมลไม่ถูกต้อง (เช่น example@email.com)'); return false; }
    const localPart = val.split('@')[0];
    if (localPart.length < 5 || localPart.length > 20) { showError(input, 'emailError', 'อีเมล (ก่อน @) ต้องมีความยาว 5–20 ตัวอักษร'); return false; }
    showValid(input, 'emailError'); return true;
}
function validatePhone() {
    const input = document.getElementById('phone');
    const val = input.value;
    if (val === '') { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องไม่มีช่องว่างระหว่างตัวเลข'); return false; }
    if (!/^\d+$/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องเป็นตัวเลข [ 0-9 ] เท่านั้น'); return false; }
    if (val.length !== 10) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องมี 10 หลักเท่านั้น'); return false; }
    if (!/^0[689]/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องขึ้นต้นด้วย 06, 08 หรือ 09 เท่านั้น'); return false; }
    showValid(input, 'phoneError'); return true;
}
function validatePassword() {
    const input = document.getElementById('password');
    const val = input.value;
    if (val === '') { showError(input, 'passwordError', 'รหัสผ่านต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'passwordError', 'รหัสผ่านต้องไม่มีช่องว่าง'); return false; }
    if (!/^[a-zA-Z0-9!#_.]+$/.test(val)) { showError(input, 'passwordError', 'รหัสผ่านใช้ได้เฉพาะตัวอักษร ตัวเลข และ [ ! # _ . ]'); return false; }
    if (val.length < 8 || val.length > 16) { showError(input, 'passwordError', 'รหัสผ่านต้องมีความยาว 8–16 ตัวอักษร'); return false; }
    showValid(input, 'passwordError'); return true;
}
function validateConfirmPassword() {
    const input = document.getElementById('confirmPassword');
    const val = input.value;
    const pw  = document.getElementById('password').value;
    if (val === '') { showError(input, 'confirmPasswordError', 'กรุณายืนยันรหัสผ่าน'); return false; }
    if (val !== pw) { showError(input, 'confirmPasswordError', 'รหัสผ่านไม่ตรงกัน กรุณากรอกใหม่'); return false; }
    showValid(input, 'confirmPasswordError'); return true;
}
function validateAgree() {
    const cb = document.getElementById('agree');
    if (!cb.checked) { showSpanError('agreeError', 'กรุณายอมรับเงื่อนไขการใช้งาน'); return false; }
    hideSpan('agreeError'); return true;
}

function validateStep1() {
    const ok = [
        validateFirstname(), validateLastname(), validateEmail(), validatePhone(),
        validatePassword(), validateConfirmPassword(), validateAgree()
    ].every(Boolean);
    if (ok) {
        goToPage(2);
    } else {
        const first = document.querySelector('#page-1 .is-invalid');
        if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); first.focus(); }
    }
}
// ============================================================
//  TERMS & CONDITIONS MODAL
// ============================================================
(function () {
    const termsLink   = document.getElementById('termsLink');
    const termsBody   = document.getElementById('termsBody');
    const acceptBtn   = document.getElementById('acceptTermsBtn');
    const agreeCheckbox = document.getElementById('agree');

    if (!termsLink || !termsBody || !acceptBtn || !agreeCheckbox) return;

    // กันไม่ให้ href="#" เลื่อนหน้า และกัน event ทะลุไปโดน label/checkbox
    termsLink.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
    });

    // บังคับเลื่อนอ่านจนสุดก่อนถึงจะกดปุ่ม "ยอมรับเงื่อนไข" ได้
    termsBody.addEventListener('scroll', () => {
        const scrolledToBottom =
            termsBody.scrollTop + termsBody.clientHeight >= termsBody.scrollHeight - 5;
        if (scrolledToBottom) acceptBtn.disabled = false;
    });

    // เผื่อเนื้อหาสั้นจนไม่มี scrollbar เลย -> ปลดล็อกปุ่มให้อัตโนมัติ
    document.getElementById('termsModal')?.addEventListener('shown.bs.modal', () => {
        if (termsBody.scrollHeight <= termsBody.clientHeight) acceptBtn.disabled = false;
    });

    // กดยอมรับใน modal -> ติ๊ก checkbox ให้ และปลดล็อกให้กดเองได้ต่อไป
    acceptBtn.addEventListener('click', () => {
        agreeCheckbox.checked = true;
        agreeCheckbox.disabled = false;
        validateAgree(); // เคลียร์ error message ถ้ามี
    });
})();

// ============================================================
//  STEP 2 — DYNAMIC HOMESTAY + IMAGE UPLOAD
// ============================================================
let homestayCount = 0;

function addHomestay() {
    homestayCount++;
    const idx = homestayCount;
    homestayImages[idx] = [];

    const list  = document.getElementById('homestay-list');
    const block = document.createElement('div');
    block.className = 'homestay-block border rounded p-3 mb-3';
    block.id = `homestay-block-${idx}`;
    block.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-2">
            <strong class="text-success"><i class="fas fa-house me-1"></i> โฮมสเตย์ที่ ${idx}</strong>
            ${idx > 1 ? `<button type="button" class="btn btn-sm btn-outline-danger"
                onclick="removeHomestay(${idx})">
                <i class="fas fa-trash"></i> ลบ</button>` : ''}
        </div>

        <div class="mb-2">
            <label class="form-label required-label">ชื่อโฮมสเตย์</label>
            <input type="text" class="form-control" id="hs_name_${idx}" placeholder="ชื่อโฮมสเตย์">
            <span class="field-error" id="hs_nameError_${idx}"></span>
        </div>

        <div class="mb-2">
            <label class="form-label required-label">ที่อยู่โฮมสเตย์</label>
            <input type="text" class="form-control" id="hs_address_${idx}"
                   placeholder="บ้านเลขที่ / หมู่ / ตำบล / อำเภอ">
            <span class="field-error" id="hs_addressError_${idx}"></span>
        </div>

        <div class="mb-2">
            <label class="form-label">รายละเอียดโฮมสเตย์ <span class="text-muted">(ไม่บังคับ)</span></label>
            <textarea class="form-control" id="hs_desc_${idx}" rows="3"
                      placeholder="อธิบายจุดเด่น บรรยากาศ หรือสิ่งอำนวยความสะดวก..."></textarea>
            <span class="field-error" id="hs_descError_${idx}"></span>
        </div>

        <div class="mb-2">
            <label class="form-label">รูปภาพโฮมสเตย์ <span class="text-muted">(ไม่บังคับ)</span></label>
            <div class="hs-upload-zone" id="hs_zone_${idx}"
                 onclick="document.getElementById('hs_img_${idx}').click()"
                 ondragover="event.preventDefault(); this.style.borderColor='#2d6a2d'"
                 ondragleave="this.style.borderColor='#b5d5b7'"
                 ondrop="handleHsDrop(event, ${idx})">
                <input type="file" id="hs_img_${idx}" accept="image/jpeg,image/png,image/webp"
                       multiple style="display:none"
                       onchange="handleHsImages(${idx}, this.files); this.value=''">
                <i class="fas fa-cloud-arrow-up" style="font-size:1.8rem; color:#ccc; display:block; margin-bottom:6px;"></i>
                <p style="color:#aaa; font-size:0.85rem; margin:0;">คลิกหรือลากรูปมาวางที่นี่</p>
                <small style="color:#bbb; font-size:0.78rem;">JPG, PNG, WEBP — หลายรูปพร้อมกัน ขนาดสูงสุด 5MB/รูป</small>
            </div>
            <div id="hs_preview_${idx}"
                 style="display:none; grid-template-columns:repeat(3,1fr); gap:6px; margin-top:8px;"></div>
        </div>
    `;
    list.appendChild(block);

    if (!document.getElementById('hs-upload-style')) {
        const style = document.createElement('style');
        style.id = 'hs-upload-style';
        style.textContent = `
            .hs-upload-zone {
                border: 2px dashed #b5d5b7; border-radius: 8px;
                padding: 1.2rem; text-align: center; cursor: pointer;
                background: #f9fdf9; transition: border-color 0.2s;
            }
            .hs-upload-zone:hover { border-color: #2d6a2d; }
            .hs-preview-card {
                position: relative; border-radius: 6px;
                overflow: hidden; aspect-ratio: 4/3;
            }
            .hs-preview-card img { width:100%; height:100%; object-fit:cover; }
            .hs-remove-btn {
                position: absolute; top: 3px; right: 3px;
                background: rgba(220,53,69,0.85); color: #fff;
                border: none; border-radius: 50%;
                width: 20px; height: 20px; font-size: 0.7rem;
                cursor: pointer; display: flex;
                align-items: center; justify-content: center;
            }
            .hs-img-num {
                position: absolute; bottom: 3px; left: 3px;
                background: rgba(0,0,0,0.5); color: #fff;
                font-size: 0.7rem; padding: 1px 5px; border-radius: 3px;
            }
        `;
        document.head.appendChild(style);
    }

    bindHomestayListeners(idx);
}

function removeHomestay(idx) {
    document.getElementById(`homestay-block-${idx}`)?.remove();
    delete homestayImages[idx];
}

function handleHsImages(idx, fileList) {
    const MAX = 5 * 1024 * 1024;
    Array.from(fileList).forEach(file => {
        if (!['image/jpeg','image/png','image/webp'].includes(file.type)) {
            alert(`"${file.name}" ไม่รองรับ — ใช้ได้เฉพาะ JPG, PNG, WebP`); return;
        }
        if (file.size > MAX) { alert(`"${file.name}" มีขนาดเกิน 5MB`); return; }
        const dup = homestayImages[idx]?.some(f => f.name === file.name && f.size === file.size);
        if (!dup) homestayImages[idx].push(file);
    });
    renderHsPreviews(idx);
}

function handleHsDrop(e, idx) {
    e.preventDefault();
    document.getElementById(`hs_zone_${idx}`).style.borderColor = '#b5d5b7';
    handleHsImages(idx, e.dataTransfer.files);
}

function renderHsPreviews(idx) {
    const grid = document.getElementById(`hs_preview_${idx}`);
    grid.innerHTML = '';
    if (!homestayImages[idx] || homestayImages[idx].length === 0) {
        grid.style.display = 'none'; return;
    }
    grid.style.display = 'grid';
    homestayImages[idx].forEach((file, i) => {
        const blobUrl = URL.createObjectURL(file);
        const card = document.createElement('div');
        card.className = 'hs-preview-card';
        card.innerHTML = `
            <img src="${blobUrl}" alt="">
            <button type="button" class="hs-remove-btn" onclick="removeHsImage(${idx}, ${i})">
                <i class="fas fa-xmark"></i>
            </button>
            <span class="hs-img-num">${i + 1}</span>
        `;
        grid.appendChild(card);
    });
}

function removeHsImage(idx, imgIdx) {
    homestayImages[idx].splice(imgIdx, 1);
    renderHsPreviews(idx);
}

// ============================================================
//  SUBMIT ALL
// ============================================================
async function submitAll() {
    if (!validateAllHomestays()) return;

    const btn = document.querySelector('.btn-submit');
    if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>กำลังส่ง...'; }

    const fd = new FormData();

    fd.append('firstname', document.getElementById('firstname').value.trim());
    fd.append('lastname',  document.getElementById('lastname').value.trim());
    fd.append('email',     document.getElementById('email').value.trim());
    fd.append('phone',     document.getElementById('phone').value.trim());
    fd.append('password',  document.getElementById('password').value.trim());

    const blocks = document.querySelectorAll('.homestay-block');
    let hsIndex  = 0;

    blocks.forEach(block => {
        const idx = block.id.split('-').pop();
        fd.append(`homestays[${hsIndex}].homestayname`, document.getElementById(`hs_name_${idx}`).value.trim());
        fd.append(`homestays[${hsIndex}].address`,      document.getElementById(`hs_address_${idx}`).value.trim());
        fd.append(`homestays[${hsIndex}].description`,  document.getElementById(`hs_desc_${idx}`).value.trim());
        (homestayImages[idx] || []).forEach(file => {
            fd.append(`homestays[${hsIndex}].images`, file);
        });
        hsIndex++;
    });

    try {
        const res  = await fetch('/owner/register', { method: 'POST', body: fd });
        const data = await res.json();
        if (data.success) {
            goToPage(3);
        } else {
            alert('เกิดข้อผิดพลาด: ' + data.message);
            if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-paper-plane me-2"></i>ส่งคำขอสมัคร'; }
        }
    } catch {
        alert('เกิดข้อผิดพลาด กรุณาลองใหม่');
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-paper-plane me-2"></i>ส่งคำขอสมัคร'; }
    }
}

// ============================================================
//  VALIDATORS HOMESTAY
// ============================================================
function bindHomestayListeners(idx) {
    const nameInput    = document.getElementById(`hs_name_${idx}`);
    const addressInput = document.getElementById(`hs_address_${idx}`);
    const descInput    = document.getElementById(`hs_desc_${idx}`);
    nameInput.addEventListener('blur',    () => validateHsName(idx));
    addressInput.addEventListener('blur', () => validateHsAddress(idx));
    descInput.addEventListener('blur',    () => validateHsDesc(idx));
    nameInput.addEventListener('input',    () => { if (nameInput.classList.contains('is-invalid'))    validateHsName(idx); });
    addressInput.addEventListener('input', () => { if (addressInput.classList.contains('is-invalid')) validateHsAddress(idx); });
    descInput.addEventListener('input',    () => { if (descInput.classList.contains('is-invalid'))    validateHsDesc(idx); });
}
function validateHsName(idx) {
    const input = document.getElementById(`hs_name_${idx}`);
    const val = input.value.trim();
    if (val === '') { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ต้องไม่เป็นค่าว่าง'); return false; }
    if (!/^[ก-๙a-zA-Z\s]+$/.test(val)) { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษ'); return false; }
    if (val.length < 3 || val.length > 255) { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ต้องมีความยาว 3–255 ตัวอักษร'); return false; }
    showValid(input, `hs_nameError_${idx}`); return true;
}
function validateHsAddress(idx) {
    const input = document.getElementById(`hs_address_${idx}`);
    const val = input.value.trim();
    if (val === '') { showError(input, `hs_addressError_${idx}`, 'ที่อยู่โฮมสเตย์ต้องไม่เป็นค่าว่าง'); return false; }
    if (!/^[ก-๙a-zA-Z0-9\s\/,\-]+$/.test(val)) { showError(input, `hs_addressError_${idx}`, 'ที่อยู่ใช้ได้เฉพาะตัวอักษรไทย อังกฤษ ตัวเลข และ / , -'); return false; }
    if (val.length > 255) { showError(input, `hs_addressError_${idx}`, 'ที่อยู่โฮมสเตย์ต้องไม่เกิน 255 ตัวอักษร'); return false; }
    showValid(input, `hs_addressError_${idx}`); return true;
}
function validateHsDesc(idx) {
    const input = document.getElementById(`hs_desc_${idx}`);
    const val = input.value.trim();
    if (val === '') { showValid(input, `hs_descError_${idx}`); return true; }
    if (val.length < 3 || val.length > 5000) { showError(input, `hs_descError_${idx}`, 'รายละเอียดโฮมสเตย์ต้องมีความยาว 3–5000 ตัวอักษร (หากกรอก)'); return false; }
    showValid(input, `hs_descError_${idx}`); return true;
}
function validateAllHomestays() {
    const blocks = document.querySelectorAll('.homestay-block');
    if (blocks.length === 0) { alert('กรุณาเพิ่มข้อมูลโฮมสเตย์อย่างน้อย 1 แห่ง'); return false; }
    let allOk = true;
    blocks.forEach(block => {
        const idx = block.id.split('-').pop();
        if (!validateHsName(idx) || !validateHsAddress(idx) || !validateHsDesc(idx)) allOk = false;
    });
    if (!allOk) {
        const first = document.querySelector('#page-2 .is-invalid');
        if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); first.focus(); }
    }
    return allOk;
}

// ============================================================
//  NAVIGATION
// ============================================================
function goToPage(n) {
    [1, 2, 3].forEach(i => {
        document.getElementById(`page-${i}`).classList.add('d-none');
        document.getElementById(`step-dot-${i}`)?.classList.remove('active', 'done');
    });
    document.getElementById(`page-${n}`).classList.remove('d-none');
    for (let i = 1; i < n; i++) {
        document.getElementById(`step-dot-${i}`)?.classList.add('done');
        document.getElementById(`step-line-${i}`)?.classList.add('done');
    }
    document.getElementById(`step-dot-${n}`)?.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
function goBack() { goToPage(1); }

// ============================================================
//  PASSWORD TOGGLE
// ============================================================
document.querySelectorAll('.toggle-pw').forEach(icon => {
    icon.addEventListener('click', () => {
        const input = document.getElementById(icon.dataset.target);
        if (!input) return;
        if (input.type === 'password') { input.type = 'text'; icon.classList.replace('fa-eye', 'fa-eye-slash'); }
        else { input.type = 'password'; icon.classList.replace('fa-eye-slash', 'fa-eye'); }
    });
});

// ============================================================
//  STEP 1 — LIVE VALIDATION
// ============================================================
const step1Fields = [
    { id: 'firstname',       fn: validateFirstname       },
    { id: 'lastname',        fn: validateLastname        },
    { id: 'email',           fn: validateEmail           },
    { id: 'phone',           fn: validatePhone           },
    { id: 'password',        fn: validatePassword        },
    { id: 'confirmPassword', fn: validateConfirmPassword },
];
step1Fields.forEach(({ id, fn }) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener('blur', fn);
    el.addEventListener('input', () => { if (el.classList.contains('is-invalid')) fn(); });
});

document.getElementById('password')?.addEventListener('input', () => {
    const cp = document.getElementById('confirmPassword');
    if (cp && cp.value !== '') validateConfirmPassword();
});

// ============================================================
//  INIT
// ============================================================
addHomestay();