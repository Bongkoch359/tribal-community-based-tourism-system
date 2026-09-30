 function showFieldError(id, msg) {
    const errorEl = document.getElementById(id + '-error');
    const inputEl = document.getElementById(id);
    if (errorEl) { errorEl.textContent = msg; errorEl.classList.add('show'); }
    if (inputEl) inputEl.classList.add('input-error');
  }

  function clearFieldError(id) {
    const errorEl = document.getElementById(id + '-error');
    const inputEl = document.getElementById(id);
    if (errorEl) { errorEl.textContent = ''; errorEl.classList.remove('show'); }
    if (inputEl) inputEl.classList.remove('input-error');
  }

  const fieldIds = ['firstname', 'lastname', 'email', 'phone', 'password', 'confirmPassword'];

  // อนุญาตเฉพาะตัวอักษรไทย, อังกฤษ และเว้นวรรค ห้ามตัวเลข/อักขระพิเศษ
  const nameRegex = /^[a-zA-Zก-๙\s]+$/;
  // เบอร์มือถือไทย ต้องขึ้นต้นด้วย 06, 08 หรือ 09
  const phoneRegex = /^(06|08|09)[0-9]{8}$/;
  // ห้ามมีตัวอักษรไทยในรหัสผ่าน
  const thaiCharRegex = /[ก-๙]/;

  document.getElementById('register-form').addEventListener('submit', function (e) {
    fieldIds.forEach(clearFieldError);

    const firstname       = document.getElementById('firstname').value.trim();
    const lastname        = document.getElementById('lastname').value.trim();
    const email            = document.getElementById('email').value.trim();
    const phone             = document.getElementById('phone').value.trim();
    const password         = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    let hasError = false;

    // อนุญาตเฉพาะตัวอักษรไทย, อังกฤษ และเว้นวรรค ห้ามตัวเลข/อักขระพิเศษ
    if (!firstname)                      { showFieldError('firstname', 'กรุณากรอกชื่อ'); hasError = true; }
    else if (!nameRegex.test(firstname)) { showFieldError('firstname', 'กรอกได้เฉพาะตัวอักษรไทยหรืออังกฤษเท่านั้น'); hasError = true; }

    if (!lastname)                      { showFieldError('lastname', 'กรุณากรอกนามสกุล'); hasError = true; }
    else if (!nameRegex.test(lastname)) { showFieldError('lastname', 'กรอกได้เฉพาะตัวอักษรไทยหรืออังกฤษเท่านั้น'); hasError = true; }

    if (!email)            { showFieldError('email', 'กรุณากรอกอีเมล'); hasError = true; }

    if (!password)                    { showFieldError('password', 'กรุณากรอกรหัสผ่าน'); hasError = true; }
    else if (thaiCharRegex.test(password)) { showFieldError('password', 'รหัสผ่านห้ามมีตัวอักษรภาษาไทย'); hasError = true; }
    else if (password.length < 6)     { showFieldError('password', 'รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร'); hasError = true; }

    if (!confirmPassword)                       { showFieldError('confirmPassword', 'กรุณากรอกยืนยันรหัสผ่าน'); hasError = true; }
    else if (thaiCharRegex.test(confirmPassword)) { showFieldError('confirmPassword', 'รหัสผ่านห้ามมีตัวอักษรภาษาไทย'); hasError = true; }
    else if (password !== confirmPassword)      { showFieldError('confirmPassword', 'รหัสผ่านไม่ตรงกัน'); hasError = true; }

    if (hasError) { e.preventDefault(); return; }

    // เบอร์มือถือไทย ต้องขึ้นต้นด้วย 06, 08 หรือ 09 และมี 10 หลัก
    if (phone && !phoneRegex.test(phone)) {
      showFieldError('phone', 'กรุณากรอกเบอร์มือถือให้ถูกต้อง (ขึ้นต้นด้วย 06, 08 หรือ 09 จำนวน 10 หลัก)');
      e.preventDefault();
      return;
    }
  });

  // ล้าง error ทันทีที่ผู้ใช้เริ่มพิมพ์ใหม่ในช่องนั้น (สำหรับช่องที่ไม่มี live-pattern check ด้านล่าง)
  fieldIds.forEach(function (id) {
    const el = document.getElementById(id);
    if (el) el.addEventListener('input', function () { clearFieldError(id); });
  });

  // ── Real-time validation: ขึ้นแดงทันทีตอนพิมพ์ผิด, หายทันทีตอนพิมพ์ถูก ──
  function liveValidate(id, testFn, invalidMsg) {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener('input', function () {
      const val = el.value.trim();
      if (val === '') { clearFieldError(id); return; } 
      if (!testFn(val)) {
        showFieldError(id, invalidMsg);
      } else {
        clearFieldError(id);
      }
    });
  }

  liveValidate('firstname', v => nameRegex.test(v), 'กรอกได้เฉพาะตัวอักษรไทยหรืออังกฤษเท่านั้น');
  liveValidate('lastname',  v => nameRegex.test(v), 'กรอกได้เฉพาะตัวอักษรไทยหรืออังกฤษเท่านั้น');
  liveValidate('phone', v => /^[0-9]*$/.test(v) && (v.length < 2 || /^(06|08|09)/.test(v)),
                'เบอร์มือถือต้องขึ้นต้นด้วย 06, 08 หรือ 09 และเป็นตัวเลขเท่านั้น');

  // รหัสผ่าน: ห้ามมีตัวอักษรไทย + ต้องมีอย่างน้อย 6 ตัวอักษร
  document.getElementById('password').addEventListener('input', function () {
    const val = this.value;
    if (val === '') {
      clearFieldError('password');
    } else if (thaiCharRegex.test(val)) {
      showFieldError('password', 'รหัสผ่านห้ามมีตัวอักษรภาษาไทย');
    } else if (val.length < 6) {
      showFieldError('password', 'รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร');
    } else {
      clearFieldError('password');
    }

    // เช็คซ้ำกับยืนยันรหัสผ่านทันที ถ้ามีการกรอกไว้แล้ว
    const confirmEl = document.getElementById('confirmPassword');
    const confirmVal = confirmEl.value;
    if (confirmVal !== '') {
      if (thaiCharRegex.test(confirmVal)) {
        showFieldError('confirmPassword', 'รหัสผ่านห้ามมีตัวอักษรภาษาไทย');
      } else if (confirmVal !== val) {
        showFieldError('confirmPassword', 'รหัสผ่านไม่ตรงกัน');
      } else {
        clearFieldError('confirmPassword');
      }
    }
  });

  // ยืนยันรหัสผ่าน: ห้ามมีตัวอักษรไทย + ต้องตรงกับรหัสผ่าน
  document.getElementById('confirmPassword').addEventListener('input', function () {
    const val = this.value;
    const passwordVal = document.getElementById('password').value;
    if (val === '') {
      clearFieldError('confirmPassword');
    } else if (thaiCharRegex.test(val)) {
      showFieldError('confirmPassword', 'รหัสผ่านห้ามมีตัวอักษรภาษาไทย');
    } else if (val !== passwordVal) {
      showFieldError('confirmPassword', 'รหัสผ่านไม่ตรงกัน');
    } else {
      clearFieldError('confirmPassword');
    }
  });
