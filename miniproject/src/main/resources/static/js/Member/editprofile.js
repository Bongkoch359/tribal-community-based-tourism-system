  /* ล้าง error เดิมทั้งหมดก่อนตรวจใหม่ทุกครั้ง */
  function clearErrors() {
    document.querySelectorAll('.form-control').forEach(el => el.classList.remove('input-error'));
    document.querySelectorAll('.field-error-msg').forEach(el => el.remove());
    document.querySelectorAll('.alert-error').forEach(el => el.remove());
  }

  /* ทำให้ input เป็นกรอบแดง + แสดงข้อความ error ใต้ช่องนั้น */
  function setFieldError(inputEl, msg) {
    inputEl.classList.add('input-error');
    const p = document.createElement('p');
    p.className = 'field-error-msg';
    p.innerHTML = `
      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <span>${msg}</span>`;
   
    const wrap = inputEl.closest('.input-wrap');
    (wrap || inputEl).insertAdjacentElement('afterend', p);
  }

  function validateForm() {
    clearErrors();

    const fnEl    = document.getElementById('firstname');
    const lnEl    = document.getElementById('lastname');
    const phoneEl = document.getElementById('phone');
    const pw1El   = document.getElementById('newPassword');
    const pw2El   = document.getElementById('confirmPassword');

    const fn    = fnEl.value.trim();
    const ln    = lnEl.value.trim();
    const phone = phoneEl.value.trim();
    const pw1   = pw1El.value;
    const pw2   = pw2El.value;

    let hasError = false;

    // — ชื่อว่าง
    if (!fn) {
      setFieldError(fnEl, 'กรุณากรอกชื่อ');
      hasError = true;
    }

    // นามสกุลว่าง
    if (!ln) {
      setFieldError(lnEl, 'กรุณากรอกนามสกุล');
      hasError = true;
    }

    // เบอร์โทรไม่ถูกต้อง (ถ้ากรอก)
    // ต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06, 08 หรือ 09 เท่านั้น
    if (phone) {
      if (!/^0[689][0-9]{8}$/.test(phone)) {
        setFieldError(phoneEl, 'เบอร์โทรศัพท์ไม่ถูกต้อง ต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06, 08 หรือ 09');
        hasError = true;
      }
    }

  
    if (pw1 || pw2) {
      if (pw1.length < 6) {
        setFieldError(pw1El, 'รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร');
        hasError = true;
      } else if (pw1 !== pw2) {
        setFieldError(pw2El, 'รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน');
        hasError = true;
      }
    }

    if (hasError) {
      const firstBad = document.querySelector('.input-error');
      if (firstBad) firstBad.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return false;
    }

   
    return true;
  }

  /* Toggle แสดง/ซ่อน รหัสผ่าน */
  function togglePw(id, btn) {
    const input = document.getElementById(id);
    const isText = input.type === 'text';
    input.type = isText ? 'password' : 'text';
    btn.innerHTML = isText
      ? `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
           <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
           <circle cx="12" cy="12" r="3"/>
         </svg>`
      : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
           <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94
                    M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19
                    m-6.72-1.07a3 3 0 11-4.24-4.24"/>
           <line x1="1" y1="1" x2="23" y2="23"/>
         </svg>`;
  }

  /* Password strength bar */
  function checkStrength(val) {
    const wrap = document.getElementById('strengthWrap');
    const fill = document.getElementById('strengthFill');
    const lbl  = document.getElementById('strengthLabel');
    if (!val) { wrap.style.display = 'none'; return; }
    wrap.style.display = 'block';
    let s = 0;
    if (val.length >= 6)           s++;
    if (val.length >= 10)          s++;
    if (/[A-Z]/.test(val))         s++;
    if (/[0-9]/.test(val))         s++;
    if (/[^A-Za-z0-9]/.test(val))  s++;
    const lv = [
      { p: '20%',  c: '#ef4444', t: 'อ่อนมาก' },
      { p: '40%',  c: '#f97316', t: 'อ่อน' },
      { p: '60%',  c: '#eab308', t: 'ปานกลาง' },
      { p: '80%',  c: '#22c55e', t: 'แข็งแรง' },
      { p: '100%', c: '#16a34a', t: 'แข็งแรงมาก' },
    ][Math.min(s, 4)];
    fill.style.width      = lv.p;
    fill.style.background = lv.c;
    lbl.textContent       = lv.t;
    lbl.style.color       = lv.c;
  }

  /* ตรวจสอบรหัสผ่านตรงกัน real-time */
  function checkMatch() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;
    const msg = document.getElementById('matchMsg');
    if (!pw2) { msg.style.display = 'none'; return; }
    msg.style.display = 'block';
    if (pw1 === pw2) {
      msg.textContent = '✓ รหัสผ่านตรงกัน';
      msg.style.color = '#16a34a';
    } else {
      msg.textContent = '✗ รหัสผ่านไม่ตรงกัน';
      msg.style.color = '#dc2626';
    }
  }

  function toggleUserMenu() {
    const wrapper = document.getElementById('userMenuWrapper');
    if (wrapper) wrapper.classList.toggle('open');
  }
  document.addEventListener('click', function(e) {
    const wrapper = document.getElementById('userMenuWrapper');
    if (wrapper && !wrapper.contains(e.target)) {
      wrapper.classList.remove('open');
    }
  });