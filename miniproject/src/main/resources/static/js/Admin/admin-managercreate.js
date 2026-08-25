 var fieldIds = ['firstname', 'lastname', 'phone', 'tribe', 'email', 'pwd', 'cpwd'];

   document.getElementById('create-manager-form').addEventListener('submit', function (e) {
    hideJsAlert();
    fieldIds.forEach(function (id) {
      document.getElementById(id).classList.remove('input-error');
    });

    var hasEmpty = false;
    fieldIds.forEach(function (id) {
      var el = document.getElementById(id);
      if (!el.value.trim()) {
        el.classList.add('input-error');
        hasEmpty = true;
      }
    });

    if (hasEmpty) {
      e.preventDefault();
      showJsAlert('กรุณากรอกข้อมูลให้ถูกต้องและครบถ้วน');
      return;
    }

    // [เพิ่มใหม่] ตรวจสอบรูปแบบเบอร์โทรศัพท์เบื้องต้น (เช่น ต้องเป็นตัวเลข 9-10 หลัก)
    var phone = document.getElementById('phone');
    var phoneRegex = /^0[0-9]{8,9}$/;
    if (!phoneRegex.test(phone.value.trim())) {
      e.preventDefault();
      phone.classList.add('input-error');
      showJsAlert('กรุณากรอกเบอร์โทรศัพท์ให้ถูกต้อง (ตัวเลข 9-10 หลัก)');
      return;
    }

    // [เพิ่มใหม่] ตรวจสอบรูปแบบอีเมลเบื้องต้น
    var email = document.getElementById('email');
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.value.trim())) {
      e.preventDefault();
      email.classList.add('input-error');
      showJsAlert('กรุณากรอกรูปแบบอีเมลให้ถูกต้อง');
      return;
    }

    var pwd  = document.getElementById('pwd');
    var cpwd = document.getElementById('cpwd');

    if (pwd.value.length < 8) {
      e.preventDefault();
      pwd.classList.add('input-error');
      showJsAlert('รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร');
      return;
    }

    if (pwd.value !== cpwd.value) {
      e.preventDefault();
      pwd.classList.add('input-error');
      cpwd.classList.add('input-error');
      showJsAlert('รหัสผ่านและการยืนยันรหัสผ่านไม่ตรงกัน');
      return;
    }
  });

  // ฟังก์ชันสำหรับแสดงข้อความแจ้งเตือนสีแดงด้านบนฟอร์ม
function showJsAlert(message) {
  var alertBox = document.getElementById('js-alert');
  if (alertBox) {
    alertBox.innerText = message;
    alertBox.style.display = 'flex';
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

// ฟังก์ชันสำหรับซ่อนข้อความแจ้งเตือน
function hideJsAlert() {
  var alertBox = document.getElementById('js-alert');
  if (alertBox) {
    alertBox.innerText = '';
    alertBox.style.display = 'none';
  }
}