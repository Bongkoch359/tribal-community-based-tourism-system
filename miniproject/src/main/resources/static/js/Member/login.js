
  document.getElementById('login-form').addEventListener('submit', function(e) {
  const email    = document.getElementById('email');
  const password = document.getElementById('password');
  const eVal = email.value.trim();
  const pVal = password.value.trim();

  email.classList.remove('input-error');
  password.classList.remove('input-error');
  hideAlert();


  if (!eVal && !pVal) {
    e.preventDefault();
    email.classList.add('input-error');
    password.classList.add('input-error');
    showAlert('กรุณากรอกอีเมลและรหัสผ่าน');
    return;
  } else if (!eVal) {
    e.preventDefault();
    email.classList.add('input-error');
    showAlert('กรุณากรอกอีเมล');
    return;
  } else if (!pVal) {
    e.preventDefault();
    password.classList.add('input-error');
    showAlert('กรุณากรอกรหัสผ่าน');
    return;
  }
});

  function showAlert(msg) {
    var box = document.getElementById('js-alert');
    document.getElementById('js-alert-msg').textContent = msg;
    box.style.display = 'flex';
    box.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function hideAlert() {
    document.getElementById('js-alert').style.display = 'none';
  }

function togglePassword() {
  const passwordInput = document.getElementById('password');
  const eyeIcon = document.getElementById('eye-icon');
  
  if (passwordInput.type === 'password') {
    passwordInput.type = 'text';
    eyeIcon.innerHTML = `
      <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"></path>
      <line x1="1" y1="1" x2="23" y2="23"></line>
    `;
  } else {
    passwordInput.type = 'password';
    eyeIcon.innerHTML = `
      <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12z"></path>
      <circle cx="12" cy="12" r="3"></circle>
    `;
  }
}