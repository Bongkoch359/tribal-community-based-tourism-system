document.addEventListener('DOMContentLoaded', function () {
  var fieldIds = ['firstname', 'lastname', 'phone', 'tribe', 'email', 'pwd', 'cpwd'];

  // ฟังก์ชันสลับแสดง/ซ่อนรหัสผ่าน (รูปตา)
  function setupTogglePassword(toggleId, inputId) {
    var toggleBtn = document.getElementById(toggleId);
    var inputField = document.getElementById(inputId);
    if (toggleBtn && inputField) {
      toggleBtn.addEventListener('click', function () {
        if (inputField.type === 'password') {
          inputField.type = 'text';
          toggleBtn.textContent = 'visibility';
        } else {
          inputField.type = 'password';
          toggleBtn.textContent = 'visibility_off';
        }
      });
    }
  }

  setupTogglePassword('toggle-pwd', 'pwd');
  setupTogglePassword('toggle-cpwd', 'cpwd');

  // ฟังก์ชันแสดง Error แต่ละช่อง
  function showFieldError(fieldId, message) {
    var el = document.getElementById(fieldId);
    var group = document.getElementById('group-' + fieldId);
    var errorDiv = document.getElementById('error-' + fieldId);

    if (el) el.classList.add('input-error');
    if (group) group.classList.add('has-error');
    if (errorDiv) {
      errorDiv.innerHTML = '<span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle;">warning</span> ' + message;
      errorDiv.style.display = 'block';
    }
  }

  // ฟังก์ชันล้าง Error แต่ละช่อง
  function clearFieldError(fieldId) {
    var el = document.getElementById(fieldId);
    var group = document.getElementById('group-' + fieldId);
    var errorDiv = document.getElementById('error-' + fieldId);

    if (el) el.classList.remove('input-error');
    if (group) group.classList.remove('has-error');
    if (errorDiv) {
      errorDiv.textContent = '';
      errorDiv.style.display = 'none';
    }
  }

  // [Real-time Check] ตรวจสอบอีเมลซ้ำทันทีเมื่อผู้ใช้พิมพ์เสร็จแล้วคลิกออกนอกช่อง (blur)
  var emailInput = document.getElementById('email');
  if (emailInput) {
    emailInput.addEventListener('blur', function () {
      var emailVal = emailInput.value.trim();
      if (emailVal === '') {
        clearFieldError('email');
        return;
      }

      var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(emailVal)) {
        showFieldError('email', 'กรุณากรอกรูปแบบอีเมลให้ถูกต้อง');
        return;
      }

      // ยิง API ไปเช็คอีเมลซ้ำกับฐานข้อมูลทันที
      fetch('/admin/manager/api/check-email?email=' + encodeURIComponent(emailVal))
        .then(response => response.json())
        .then(isDuplicate => {
          if (isDuplicate) {
            showFieldError('email', 'อีเมลนี้มีผู้ใช้งานแล้ว กรุณาใช้อีเมลอื่น');
          } else {
            clearFieldError('email');
          }
        })
        .catch(error => console.error('Error checking email:', error));
    });
  }

  // ตรวจสอบเมื่อกดปุ่ม Submit ฟอร์ม
  document.getElementById('create-manager-form').addEventListener('submit', function (e) {
    hideJsAlert();

    var hasError = false;
    var firstErrorField = null;

    // 1. เช็คค่าว่าง (รองรับทั้ง input และ select) ครั้งเดียวจบ
    fieldIds.forEach(function (id) {
      var el = document.getElementById(id);
      if (el && (!el.value || !el.value.trim())) {
        var actionText = (el.tagName === 'SELECT') ? 'กรุณาเลือก' : 'กรุณากรอก';
        showFieldError(id, actionText + getFieldLabel(id));
        
        if (!firstErrorField) firstErrorField = el;
        hasError = true;
      }
    });

    if (hasError) {
      e.preventDefault();
      showJsAlert('กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน');
      if (firstErrorField) firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }

    // 2. เช็คเบอร์โทรศัพท์
    var phone = document.getElementById('phone');
    var phoneRegex = /^0[0-9]{8,9}$/;
    if (!phoneRegex.test(phone.value.trim())) {
      e.preventDefault();
      showFieldError('phone', 'กรุณากรอกเบอร์โทรศัพท์ให้ถูกต้อง (ตัวเลข 9-10 หลัก)');
      phone.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }

    // 3. เช็ครหัสผ่าน (ความยาว 8-16 ตัวอักษร)
    var pwd = document.getElementById('pwd');
    if (pwd.value.length < 8 || pwd.value.length > 16) {
      e.preventDefault();
      showFieldError('pwd', 'รหัสผ่านต้องมีความยาว 8-16 ตัวอักษร');
      pwd.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }

    // 4. เช็ครหัสผ่านตรงกันไหม
    var cpwd = document.getElementById('cpwd');
    if (pwd.value !== cpwd.value) {
      e.preventDefault();
      showFieldError('pwd', 'รหัสผ่านไม่ตรงกัน');
      showFieldError('cpwd', 'รหัสผ่านและการยืนยันรหัสผ่านไม่ตรงกัน');
      cpwd.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
  });

  function getFieldLabel(id) {
    var labels = {
      'firstname': 'ชื่อ',
      'lastname': 'นามสกุล',
      'phone': 'เบอร์โทรศัพท์',
      'tribe': 'ชุมชนที่รับผิดชอบ',
      'email': 'อีเมล',
      'pwd': 'รหัสผ่าน',
      'cpwd': 'ยืนยันรหัสผ่าน'
    };
    return labels[id] || 'ข้อมูลนี้';
  }

  function showJsAlert(message) {
    var alertBox = document.getElementById('js-alert');
    if (alertBox) {
      alertBox.innerText = message;
      alertBox.style.display = 'flex';
    }
  }

  function hideJsAlert() {
    var alertBox = document.getElementById('js-alert');
    if (alertBox) {
      alertBox.innerText = '';
      alertBox.style.display = 'none';
    }
  }
});