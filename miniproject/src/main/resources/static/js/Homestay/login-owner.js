const emailInput    = document.getElementById('email');
const passwordInput = document.getElementById('password');
const emailError    = document.getElementById('emailError');
const passwordError = document.getElementById('passwordError');

function showPendingBanner(msg) {
    const banner = document.getElementById('pendingBanner');
    const span   = document.getElementById('pendingMsg');
    if (banner && span) {
        span.textContent = msg;
        banner.classList.remove('d-none');
    }
}

function validateEmail() {
    const val = emailInput.value;
    if (val === '') {
        showError(emailInput, emailError, 'อีเมลต้องไม่เป็นค่าว่าง');
        return false;
    }
    if (/\s/.test(val)) {
        showError(emailInput, emailError, 'อีเมลต้องไม่มีช่องว่างระหว่างตัวอักษร');
        return false;
    }
    const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(val)) {
        showError(emailInput, emailError, 'รูปแบบอีเมลไม่ถูกต้อง (เช่น example@email.com)');
        return false;
    }
    const localPart = val.split('@')[0];
    if (localPart.length < 5 || localPart.length > 20) {
        showError(emailInput, emailError, 'อีเมล (ก่อน @) ต้องมีความยาว 5-20 ตัวอักษร');
        return false;
    }
    showValid(emailInput, emailError);
    return true;
}

function validatePassword() {
    const val = passwordInput.value;
    if (val === '') {
        showError(passwordInput, passwordError, 'รหัสผ่านต้องไม่เป็นค่าว่าง');
        return false;
    }
    if (/\s/.test(val)) {
        showError(passwordInput, passwordError, 'รหัสผ่านต้องไม่มีช่องว่าง');
        return false;
    }
    const allowedRegex = /^[a-zA-Z0-9!#_.]+$/;
    if (!allowedRegex.test(val)) {
        showError(passwordInput, passwordError, 'รหัสผ่านใช้ได้เฉพาะตัวอักษร ตัวเลข และ [ ! # _ . ]');
        return false;
    }
    if (val.length < 8 || val.length > 16) {
        showError(passwordInput, passwordError, 'รหัสผ่านต้องมีความยาว 8-16 ตัวอักษร');
        return false;
    }
    showValid(passwordInput, passwordError);
    return true;
}

function showError(input, span, msg) {
    input.classList.add('invalid');
    input.classList.remove('valid');
    span.textContent = '⚠ ' + msg;
    span.style.display = 'block';
}

function showValid(input, span) {
    input.classList.remove('invalid');
    input.classList.add('valid');
    span.style.display = 'none';
}

emailInput.addEventListener('blur', validateEmail);
passwordInput.addEventListener('blur', validatePassword);

emailInput.addEventListener('input', () => {
    if (emailInput.classList.contains('invalid')) validateEmail();
});
passwordInput.addEventListener('input', () => {
    if (passwordInput.classList.contains('invalid')) validatePassword();
});

document.getElementById('loginForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const emailOk    = validateEmail();
    const passwordOk = validatePassword();
    if (!emailOk || !passwordOk) return;

    fetch('/owner/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email:    emailInput.value.trim(),
            password: passwordInput.value.trim()
        })
    })
    .then(res => res.json())
    .then(data => {
    if (data.success) {
        window.location.href = '/owner/dashboard';
    } else {
        // กรณีบัญชีรอ Admin อนุมัติ → แสดง banner แยก ไม่ mark ช่อง email ว่า invalid
        if (data.message && data.message.includes('ยังไม่ได้รับการอนุมัติ')) {
            showPendingBanner(data.message);
        } else {
            showError(emailInput, emailError, data.message);
        }
    }
})
    .catch(() => {
        showError(emailInput, emailError, 'เกิดข้อผิดพลาด กรุณาลองใหม่');
    });
});