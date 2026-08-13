// ============================================
//   edit-profile.js
// ============================================

// ─────────────────────────────────────────
// Regex สำหรับ validate ฝั่ง client (ก่อน submit จริง)
// ─────────────────────────────────────────
const nameRegex    = /^[ก-์a-zA-Z]+$/;
const emailRegex   = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex   = /^0[689][0-9]{8}$/;
const accountRegex = /^[0-9\-]{6,30}$/;
// ─────────────────────────────────────────
// Toast helper — แสดงผลจากข้อความที่ server ส่งมาผ่าน flash attribute
// ─────────────────────────────────────────
function showToast(message, isError) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.remove('error');
    if (isError) toast.classList.add('error');
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2800);
}

(function () {
    const el = document.getElementById('serverMsg');
    const success = el ? el.dataset.success : null;
    const error   = el ? el.dataset.error   : null;
    if (success && success !== 'null' && success.trim() !== '') {
        showToast(success, false);
    } else if (error && error !== 'null' && error.trim() !== '') {
        showToast(error, true);
    }
})();

// ─────────────────────────────────────────
// Toggle show/hide password
// ─────────────────────────────────────────
document.querySelectorAll('.toggle-password').forEach(btn => {
    btn.addEventListener('click', () => {
        const target = document.getElementById(btn.dataset.target);
        const icon = btn.querySelector('i');
        if (target.type === 'password') {
            target.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            target.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    });
});

// ─────────────────────────────────────────
// Real-time validation: ชื่อ / นามสกุล / อีเมล / เบอร์โทร (เหมือน manager)
// ─────────────────────────────────────────
function attachNameValidation(inputId, label) {
    const input   = document.getElementById(inputId);
    const errorEl = document.getElementById(inputId + 'Error');
    input.addEventListener('input', () => {
        const v = input.value.trim();
        if (!v) {
            errorEl.textContent = `กรุณากรอก${label}`;
        } else if (!nameRegex.test(v)) {
            errorEl.textContent = `${label}ใช้ได้เฉพาะภาษาไทยและอังกฤษเท่านั้น ไม่มีเว้นวรรคหรืออักขระพิเศษ`;
        } else {
            errorEl.textContent = '';
        }
    });
}
attachNameValidation('firstname', 'ชื่อ');
attachNameValidation('lastname', 'นามสกุล');

const emailInputEl = document.getElementById('email');
emailInputEl.addEventListener('input', () => {
    const v = emailInputEl.value.trim();
    const errorEl = document.getElementById('emailError');
    if (!v) errorEl.textContent = 'กรุณากรอกอีเมล';
    else if (!emailRegex.test(v)) errorEl.textContent = 'รูปแบบอีเมลไม่ถูกต้อง';
    else errorEl.textContent = '';
});

const phoneInputEl = document.getElementById('phone');
phoneInputEl.addEventListener('input', () => {
    const v = phoneInputEl.value.trim();
    const errorEl = document.getElementById('phoneError');
    if (!v) errorEl.textContent = '';   // เบอร์โทรไม่บังคับกรอก
    else if (!phoneRegex.test(v)) errorEl.textContent = 'เบอร์โทรต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06 08 09 เท่านั้น';
    else errorEl.textContent = '';
});
// ─────────────────────────────────────────
// FORM 1: ข้อมูลส่วนตัว — validate ก่อน submit จริง (ไม่มี fetch)
// ─────────────────────────────────────────
document.getElementById('personalForm').addEventListener('submit', function (e) {
    const firstname = document.getElementById('firstname').value.trim();
    const lastname  = document.getElementById('lastname').value.trim();
    const email     = document.getElementById('email').value.trim();
    const phone     = document.getElementById('phone').value.trim();

    document.getElementById('firstnameError').textContent = '';
    document.getElementById('lastnameError').textContent  = '';
    document.getElementById('emailError').textContent     = '';
    document.getElementById('phoneError').textContent     = '';

     if (!firstname || !nameRegex.test(firstname)) {
        e.preventDefault();
        document.getElementById('firstnameError').textContent = 'กรุณากรอกชื่อให้ถูกต้อง (ไทย/อังกฤษ ไม่มีอักขระพิเศษ)';
        return;
    }
    if (!lastname || !nameRegex.test(lastname)) {
        e.preventDefault();
        document.getElementById('lastnameError').textContent = 'กรุณากรอกนามสกุลให้ถูกต้อง (ไทย/อังกฤษ ไม่มีอักขระพิเศษ)';
        return;
    }
    if (!email || !emailRegex.test(email)) {
        e.preventDefault();
        document.getElementById('emailError').textContent = 'กรุณากรอกอีเมลให้ถูกต้อง';
        return;
    }
    if (phone && !phoneRegex.test(phone)) {
        e.preventDefault();
        document.getElementById('phoneError').textContent = 'เบอร์โทรต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06 08 09 เท่านั้น';
        return;
    }
    // ผ่านการตรวจสอบแล้ว ปล่อยให้ฟอร์ม submit ไปที่ server ตามปกติ
});

// ─────────────────────────────────────────
// FORM 2: ข้อมูลธนาคาร — validate ก่อน submit จริง
// ─────────────────────────────────────────
document.getElementById('bankForm').addEventListener('submit', function (e) {
    const bankName      = document.getElementById('bankName').value;
    const accountName   = document.getElementById('accountName').value.trim();
    const accountNumber = document.getElementById('accountNumber').value.trim();

    document.getElementById('bankNameError').textContent      = '';
    document.getElementById('accountNameError').textContent   = '';
    document.getElementById('accountNumberError').textContent = '';

    if (!bankName) {
        e.preventDefault();
        document.getElementById('bankNameError').textContent = 'กรุณาเลือกธนาคาร';
        return;
    }
    if (!accountName) {
        e.preventDefault();
        document.getElementById('accountNameError').textContent = 'กรุณากรอกชื่อบัญชี';
        return;
    }
    if (!accountNumber || !accountRegex.test(accountNumber)) {
        e.preventDefault();
        document.getElementById('accountNumberError').textContent = 'กรุณากรอกเลขบัญชีให้ถูกต้อง';
        return;
    }
});

// ─────────────────────────────────────────
// FORM 3: ลายเซ็น — preview ไฟล์ก่อนอัปโหลด (validate ก่อน submit จริง)
// ─────────────────────────────────────────
document.getElementById('signatureFile').addEventListener('change', function (e) {
    const file = e.target.files[0];
    const wrap = document.getElementById('signaturePreviewWrap');
    const img  = document.getElementById('signaturePreviewImg');
    if (!file) { wrap.style.display = 'none'; return; }

    const reader = new FileReader();
    reader.onload = function (ev) {
        img.src = ev.target.result;
        wrap.style.display = 'block';
    };
    reader.readAsDataURL(file);
});

document.getElementById('signatureForm').addEventListener('submit', function (e) {
    const fileInput = document.getElementById('signatureFile');
    const errorEl   = document.getElementById('signatureError');
    errorEl.textContent = '';

    if (!fileInput.files.length) {
        e.preventDefault();
        errorEl.textContent = 'กรุณาเลือกไฟล์ลายเซ็นก่อน';
        return;
    }
});

// ─────────────────────────────────────────
// FORM 4: เปลี่ยนรหัสผ่าน — validate ก่อน submit จริง
// ─────────────────────────────────────────
document.getElementById('passwordForm').addEventListener('submit', function (e) {
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword     = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    document.getElementById('newPasswordError').textContent     = '';
    document.getElementById('confirmPasswordError').textContent = '';

    if (!currentPassword || !newPassword || !confirmPassword) {
        e.preventDefault();
        showToast('กรุณากรอกรหัสผ่านให้ครบทุกช่อง', true);
        return;
    }
    if (newPassword.length < 8) {
        e.preventDefault();
        document.getElementById('newPasswordError').textContent = 'รหัสผ่านใหม่ต้องมีอย่างน้อย 8 ตัวอักษร';
        return;
    }
    if (newPassword !== confirmPassword) {
        e.preventDefault();
        document.getElementById('confirmPasswordError').textContent = 'รหัสผ่านไม่ตรงกัน';
        return;
    }
});