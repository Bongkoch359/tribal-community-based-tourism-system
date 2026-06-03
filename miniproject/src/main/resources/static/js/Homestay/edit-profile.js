// ============================================
//   edit-profile.js  (Session-based version)
//   GET  /owner/profile       — โหลดข้อมูล
//   PUT  /owner/profile       — บันทึกข้อมูลส่วนตัว
//   PUT  /owner/change-password — เปลี่ยนรหัสผ่าน
// ============================================

// ─── DOM Refs ─────────────────────────────────
const firstnameInput   = document.getElementById('firstname');
const lastnameInput    = document.getElementById('lastname');
const emailInput       = document.getElementById('email');
const phoneInput       = document.getElementById('phone');
const bankNameInput    = document.getElementById('bankName');
const bankBranchInput  = document.getElementById('bankBranch');
const accountNameInput = document.getElementById('accountName');
const accountNumInput  = document.getElementById('accountNumber');
const currentPwInput   = document.getElementById('currentPassword');
const newPwInput       = document.getElementById('newPassword');
const confirmPwInput   = document.getElementById('confirmPassword');
const resetBtn         = document.getElementById('resetBtn');
const toast            = document.getElementById('toast');

let originalData = {};

// ─── Init ─────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadProfile();
    bindEvents();
});

// ─── โหลดโปรไฟล์จาก Session ─────────────────
async function loadProfile() {
    try {
        const res = await fetch('/owner/profile');

        if (res.status === 401) {
            window.location.href = '/owner/login';
            return;
        }
        if (!res.ok) throw new Error('โหลดข้อมูลล้มเหลว');

        const data = await res.json();
        fillForm(data);
        originalData = { ...data };

        // แจ้ง HTML ว่าโหลดเสร็จแล้ว (สำหรับ bankAlert)
        document.dispatchEvent(new CustomEvent('profileLoaded', { detail: data }));

    } catch (err) {
        showToast('ไม่สามารถโหลดข้อมูลได้: ' + err.message, true);
    }
}

function fillForm(data) {
    firstnameInput.value   = data.firstname     || '';
    lastnameInput.value    = data.lastname      || '';
    emailInput.value       = data.email         || '';
    phoneInput.value       = data.phone         || '';
    bankNameInput.value    = data.bankName      || '';
    bankBranchInput.value  = data.bankBranch    || '';
    accountNameInput.value = data.accountName   || '';
    accountNumInput.value  = data.accountNumber || '';

    // Profile card
    const fullName = `${data.firstname || ''} ${data.lastname || ''}`.trim();
    document.getElementById('profileName').textContent  = fullName || 'ไม่ระบุชื่อ';
    document.getElementById('profileEmail').textContent = data.email || '-';

    // Avatar initials
    const initials = ((data.firstname?.[0] || '') + (data.lastname?.[0] || '')).toUpperCase() || 'HO';
    document.getElementById('avatarInitials').textContent = initials;
}

// ─── Bind Events ──────────────────────────────
function bindEvents() {

    // Toggle password visibility
    document.querySelectorAll('.toggle-password').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.target);
            const hidden = input.type === 'password';
            input.type      = hidden ? 'text' : 'password';
            btn.textContent = hidden ? '🙈' : '👁';
        });
    });

    // Live validation
    firstnameInput.addEventListener('input',   () => validateField(firstnameInput, 'firstnameError', 'กรุณากรอกชื่อ'));
    lastnameInput.addEventListener('input',    () => validateField(lastnameInput,  'lastnameError',  'กรุณากรอกนามสกุล'));
    emailInput.addEventListener('input',       validateEmail);
    phoneInput.addEventListener('input',       validatePhone);
    bankNameInput.addEventListener('input',    validateBankName);
    accountNameInput.addEventListener('input', validateAccountName);
    accountNumInput.addEventListener('input',  validateAccountNumber);
    newPwInput.addEventListener('input',       validateNewPassword);
    confirmPwInput.addEventListener('input',   validateConfirmPassword);

    resetBtn.addEventListener('click', resetForm);

    document.getElementById('editProfileForm').addEventListener('submit', handleSubmit);
}

// ─── Submit ───────────────────────────────────
async function handleSubmit(e) {
    e.preventDefault();

    const profileValid = validateField(firstnameInput, 'firstnameError', 'กรุณากรอกชื่อ')
                      && validateField(lastnameInput,  'lastnameError',  'กรุณากรอกนามสกุล')
                      && validateEmail()
                      && validatePhone()
                      && validateBankName()
                      && validateAccountName()
                      && validateAccountNumber();

    if (!profileValid) return;

    const saveBtn = document.getElementById('saveBtn');
    saveBtn.disabled = true;
    saveBtn.innerHTML = '⏳ กำลังบันทึก...';

    try {
        // 1) บันทึกข้อมูลส่วนตัว
        await saveProfile();

        // 2) เปลี่ยนรหัสผ่าน (ถ้ากรอกมา)
        if (newPwInput.value.trim()) {
            await savePassword();
        }

        showToast('✓ บันทึกข้อมูลสำเร็จ');
        clearPasswordFields();

        // ซ่อน bankAlert ถ้ากรอกข้อมูลธนาคารครบแล้ว
        const bankAlert = document.getElementById('bankAlert');
        if (bankAlert) bankAlert.style.display = 'none';

    } catch (err) {
        showToast(err.message, true);
    } finally {
        saveBtn.disabled  = false;
        saveBtn.innerHTML = '<span>✓</span> บันทึกข้อมูล';
    }
}

async function saveProfile() {
    const res = await fetch('/owner/profile', {
        method:  'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            firstname:     firstnameInput.value.trim(),
            lastname:      lastnameInput.value.trim(),
            email:         emailInput.value.trim(),
            phone:         phoneInput.value.trim(),
            bankName:      bankNameInput.value.trim(),
            bankBranch:    bankBranchInput.value.trim(),
            accountName:   accountNameInput.value.trim(),
            accountNumber: accountNumInput.value.trim(),
        }),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || 'บันทึกข้อมูลล้มเหลว');

    fillForm(result);
    originalData = { ...result };
}

async function savePassword() {
    if (!validateNewPassword() || !validateConfirmPassword()) return;

    if (!currentPwInput.value.trim()) {
        throw new Error('กรุณากรอกรหัสผ่านปัจจุบัน');
    }

    const res = await fetch('/owner/change-password', {
        method:  'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            currentPassword: currentPwInput.value,
            newPassword:     newPwInput.value,
        }),
    });

    const result = await res.json();
    if (!res.ok) throw new Error(result.message || 'เปลี่ยนรหัสผ่านล้มเหลว');
}

// ─── Validation ───────────────────────────────
function validateField(input, errorId, msg) {
    const empty = !input.value.trim();
    showError(input, errorId, empty ? msg : '');
    return !empty;
}

function validateEmail() {
    const val = emailInput.value.trim();
    const ok  = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    const msg = !val ? 'กรุณากรอกอีเมล' : !ok ? 'รูปแบบอีเมลไม่ถูกต้อง' : '';
    showError(emailInput, 'emailError', msg);
    return !msg;
}

function validatePhone() {
    const val = phoneInput.value.trim();
    if (!val) { showError(phoneInput, 'phoneError', ''); return true; }
    const ok = /^[0-9]{9,10}$/.test(val);
    showError(phoneInput, 'phoneError', ok ? '' : 'เบอร์โทรต้องเป็นตัวเลข 9-10 หลัก');
    return ok;
}

function validateBankName() {
    const val = bankNameInput.value.trim();
    const msg = !val ? 'กรุณากรอกชื่อธนาคาร'
              : val.length < 2 || val.length > 100 ? 'ชื่อธนาคารต้องมีความยาว 2–100 ตัวอักษร'
              : '';
    showError(bankNameInput, 'bankNameError', msg);
    return !msg;
}

function validateAccountName() {
    const val = accountNameInput.value.trim();
    const msg = !val ? 'กรุณากรอกชื่อบัญชี'
              : val.length < 2 || val.length > 200 ? 'ชื่อบัญชีต้องมีความยาว 2–200 ตัวอักษร'
              : '';
    showError(accountNameInput, 'accountNameError', msg);
    return !msg;
}

function validateAccountNumber() {
    const val = accountNumInput.value.trim();
    const msg = !val ? 'กรุณากรอกเลขบัญชีธนาคาร'
              : /\s/.test(val) ? 'เลขบัญชีต้องไม่มีช่องว่าง'
              : !/^\d+$/.test(val) ? 'เลขบัญชีต้องเป็นตัวเลขเท่านั้น'
              : val.length < 10 || val.length > 30 ? 'เลขบัญชีต้องมีความยาว 10–30 หลัก'
              : '';
    showError(accountNumInput, 'accountNumberError', msg);
    return !msg;
}

function validateNewPassword() {
    const val = newPwInput.value;
    if (!val) { showError(newPwInput, 'newPasswordError', ''); return true; }
    const ok = val.length >= 6;
    showError(newPwInput, 'newPasswordError', ok ? '' : 'รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร');
    return ok;
}

function validateConfirmPassword() {
    const match = confirmPwInput.value === newPwInput.value;
    showError(confirmPwInput, 'confirmPasswordError', match ? '' : 'รหัสผ่านไม่ตรงกัน');
    return match;
}

function showError(input, errorId, msg) {
    const el = document.getElementById(errorId);
    if (el) el.textContent = msg;
    input.classList.toggle('is-error', !!msg);
}

// ─── Reset ────────────────────────────────────
function resetForm() {
    fillForm(originalData);
    clearPasswordFields();
    document.querySelectorAll('.error-msg').forEach(el => el.textContent = '');
    document.querySelectorAll('.form-input').forEach(el => el.classList.remove('is-error'));
}

function clearPasswordFields() {
    currentPwInput.value = '';
    newPwInput.value     = '';
    confirmPwInput.value = '';
}

// ─── Toast ────────────────────────────────────
let toastTimer;
function showToast(msg, isError = false) {
    toast.textContent = msg;
    toast.classList.toggle('error', isError);
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 3000);
}