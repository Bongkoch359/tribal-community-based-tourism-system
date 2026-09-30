const cfg = window.PAYMENT_CONFIG || {};
/* ── นับถอยหลังเวลาชำระเงิน ── */
(function () {
    const deadlineStr = cfg.deadlineIso;
    if (!deadlineStr) return;
    const deadline = new Date(deadlineStr);
    if (isNaN(deadline.getTime())) {
        console.error('Invalid deadline date:', deadlineStr);
        return;
    }
    const el = document.getElementById('remainText');
    if (!el) return; // ในหน้านี้ยังไม่มี element id="remainText"
    function tick() {
        const diff = deadline - Date.now();
        if (diff <= 0) {
            el.textContent = '⚠️ หมดกำหนดชำระเงินแล้ว';
            el.classList.add('deadline-expired');
            return;
        }
        const days  = Math.floor(diff / 86400000);
        const hours = Math.floor((diff % 86400000) / 3600000);
        const mins  = Math.floor((diff % 3600000) / 60000);
        const secs  = Math.floor((diff % 60000) / 1000);
        if (days > 0) {
            el.textContent = `เหลืออีก ${days} วัน ${hours} ชั่วโมง`;
        } else {
            const h = String(hours).padStart(2, '0');
            const m = String(mins).padStart(2, '0');
            const s = String(secs).padStart(2, '0');
            el.textContent = `เหลืออีก ${h}:${m}:${s}`;
        }
        setTimeout(tick, 1000);
    }
    tick();
})();

/* ── เมนูผู้ใช้ ── */
function toggleUserMenu() {
    document.getElementById('userMenuWrapper').classList.toggle('open');
}
document.addEventListener('click', function (e) {
    const w = document.getElementById('userMenuWrapper');
    if (w && !w.contains(e.target)) w.classList.remove('open');
});

/* ── คัดลอกเลขบัญชี ── */
function copyAccount(btn) {
    navigator.clipboard.writeText(btn.dataset.account).then(() => {
        btn.textContent = '✅ คัดลอกแล้ว';
        btn.classList.add('copied');
        setTimeout(() => {
            btn.textContent = 'คัดลอก';
            btn.classList.remove('copied');
        }, 2000);
    });
}

/* ── ตรวจชนิดไฟล์สลิป (JPG, PNG, PDF เท่านั้น) ── */
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'application/pdf'];
const ALLOWED_EXTS  = ['jpg', 'jpeg', 'png', 'pdf'];

function isValidSlipFile(file) {
    const ext = (file.name.split('.').pop() || '').toLowerCase();
    return ALLOWED_TYPES.includes(file.type) && ALLOWED_EXTS.includes(ext);
}

function clearSlipErrors() {
    const fileInput = document.getElementById('slipFile');
    const errEl     = document.getElementById('slipFileError');
    const typeErrEl = document.getElementById('slipFileTypeError');
    if (fileInput) fileInput.classList.remove('input-error');
    if (errEl)     errEl.classList.remove('show');
    if (typeErrEl) typeErrEl.classList.remove('show');
}

function handleFileSelect(input) {
    clearSlipErrors();

    if (!input.files || !input.files[0]) return;
    const file = input.files[0];

    // ชนิดไฟล์ไม่ถูกต้อง -> เคลียร์ทิ้งทันที
    if (!isValidSlipFile(file)) {
        input.value = '';
        input.classList.add('input-error');
        document.getElementById('slipFileTypeError').classList.add('show');
        document.getElementById('previewArea').style.display = 'none';
        document.getElementById('fileName').textContent = '';
        return;
    }

    document.getElementById('fileName').textContent = file.name;
    const previewImg = document.getElementById('previewImg');

    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function (e) {
            previewImg.src = e.target.result;
            previewImg.style.display = 'inline-block';
            document.getElementById('previewArea').style.display = 'block';
        };
        reader.readAsDataURL(file);
    } else {
        // PDF: ไม่มีรูป preview แสดงแค่ชื่อไฟล์
        previewImg.src = '';
        previewImg.style.display = 'none';
        document.getElementById('previewArea').style.display = 'block';
    }
}

function removeFile() {
    const fileInput = document.getElementById('slipFile');
    fileInput.value = '';
    clearSlipErrors();
    document.getElementById('previewArea').style.display = 'none';
    document.getElementById('fileName').textContent = '';
}

/* ── Drag & Drop (ทำงานเมื่อมี element id="dropZone" เท่านั้น) ── */
const dz = document.getElementById('dropZone');
if (dz) {
    ['dragenter', 'dragover'].forEach(ev =>
        dz.addEventListener(ev, e => { e.preventDefault(); dz.classList.add('drag'); }));
    ['dragleave', 'drop'].forEach(ev =>
        dz.addEventListener(ev, e => { e.preventDefault(); dz.classList.remove('drag'); }));
    dz.addEventListener('drop', e => {
        const f = e.dataTransfer.files[0];
        if (!f) return;
        const dt = new DataTransfer();
        dt.items.add(f);
        const input = document.getElementById('slipFile');
        input.files = dt.files;
        handleFileSelect(input);
    });
}

/* ── STEP 1: ตรวจไฟล์ แล้วเปิด modal ยืนยัน ── */
function submitPayment() {
    const fileInput   = document.getElementById('slipFile');
    const errorEl     = document.getElementById('slipFileError');
    const typeErrorEl = document.getElementById('slipFileTypeError');

    clearSlipErrors();

    if (!fileInput.files[0]) {
        fileInput.classList.add('input-error');
        errorEl.classList.add('show');
        fileInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    if (!isValidSlipFile(fileInput.files[0])) {
        fileInput.classList.add('input-error');
        typeErrorEl.classList.add('show');
        fileInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    const overlay = document.getElementById('confirmModalOverlay');
    overlay.classList.add('show');
    requestAnimationFrame(() => {
        requestAnimationFrame(() => { overlay.classList.add('in'); });
    });
}

function closeConfirmModal() {
    const overlay = document.getElementById('confirmModalOverlay');
    overlay.classList.remove('in');
    setTimeout(() => { overlay.classList.remove('show'); }, 200);
}

/* ── STEP 2: ยืนยันจริง -> ส่งฟอร์ม + โชว์ modal สำเร็จ ── */
function confirmAndSubmit() {
    closeConfirmModal();

    const form = document.getElementById('paymentForm');

    // ย้าย input ตัวจริงเข้าฟอร์ม (ไม่ clone) เพื่อให้ .files ไม่หาย
    const fileInput = document.getElementById('slipFile');
    fileInput.name = 'slipFile';
    form.appendChild(fileInput);

    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.textContent = '⏳ กำลังส่งข้อมูล…';

    // รอ confirm modal ปิดก่อนค่อยเปิด success modal
    setTimeout(() => {
        showPayModal();
        setTimeout(() => { form.submit(); }, 1800);
    }, 250);
}

/* ── SUCCESS MODAL ── */
function showPayModal() {
    const overlay = document.getElementById('payModalOverlay');
    const bar = document.getElementById('payModalProgressBar');
    overlay.classList.add('show');
    requestAnimationFrame(() => {
        requestAnimationFrame(() => { overlay.classList.add('in'); });
    });
    setTimeout(() => { bar.style.width = '100%'; }, 150);
}