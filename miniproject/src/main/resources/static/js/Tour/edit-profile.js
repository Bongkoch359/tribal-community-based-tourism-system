// ─── Regex ───
        const nameRegex = /^[ก-์a-zA-Z]+$/;
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const phoneRegex = /^0[689][0-9]{8}$/;
        const accountRegex = /^[0-9\-]{10,20}$/;

        // ─── Toast helper (Error / Alert) ───
        function showToast(message, isError) {
            const toast = document.getElementById('toast');
            toast.textContent = message;
            toast.classList.remove('error');
            if (isError) toast.classList.add('error');
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 3200);
        }

        // ─── Success Modal (แบบเดียวกับ addtour) ───
        function showSuccessModal(title, desc) {
            const modal = document.getElementById('successModal');
            const fill = document.getElementById('progressFill');

            if (title) document.getElementById('modalTitle').textContent = title;
            if (desc) document.getElementById('modalDesc').textContent = desc;

            modal.classList.add('show');

            fill.style.animation = 'none';
            fill.offsetHeight; // trigger reflow
            fill.style.animation = 'progress-drain 2.2s linear forwards';

            setTimeout(() => {
                modal.classList.remove('show');
            }, 2200);
        }

        // ─── Auto-show Success Modal หรือ Error Toast เมื่อโหลดหน้าจาก Server ───
        (function () {
            const el = document.getElementById('serverMsg');
            const success = el ? el.dataset.success : null;
            const error = el ? el.dataset.error : null;

            if (success && success !== 'null' && success.trim() !== '') {
                showSuccessModal('บันทึกสำเร็จ!', success);
            } else if (error && error !== 'null' && error.trim() !== '') {
                showToast(error, true);
            }
        })();

        // ─── Toggle show/hide password ───
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

        // ─── Real-time validation: ชื่อ / นามสกุล ───
        function attachNameValidation(inputId, label) {
            const input = document.getElementById(inputId);
            const errorEl = document.getElementById(inputId + 'Error');
            input.addEventListener('input', () => {
                const v = input.value.trim();
                if (!v) {
                    errorEl.textContent = `กรุณากรอก${label}`;
                    errorEl.classList.remove('ok');
                } else if (!nameRegex.test(v)) {
                    errorEl.textContent = `${label}ใช้ได้เฉพาะภาษาไทยและอังกฤษเท่านั้น ไม่มีเว้นวรรคหรืออักขระพิเศษ`;
                    errorEl.classList.remove('ok');
                } else {
                    errorEl.textContent = '';
                }
            });
        }
        attachNameValidation('firstname', 'ชื่อ');
        attachNameValidation('lastname', 'นามสกุล');

        // ─── Real-time: อีเมล ───
        const emailInput = document.getElementById('emailInput');
        const emailError = document.getElementById('emailError');
        emailInput.addEventListener('input', () => {
            const v = emailInput.value.trim();
            if (!v) {
                emailError.textContent = 'กรุณากรอกอีเมล';
            } else if (!emailRegex.test(v)) {
                emailError.textContent = 'รูปแบบอีเมลไม่ถูกต้อง';
            } else {
                emailError.textContent = '';
            }
        });

        // ─── Real-time: เบอร์โทร ───
        const phoneInput = document.getElementById('phoneInput');
        const phoneError = document.getElementById('phoneError');
        phoneInput.addEventListener('input', () => {
            const v = phoneInput.value;
            if (!v) {
                phoneError.textContent = 'กรุณากรอกเบอร์โทร';
            } else if (!phoneRegex.test(v)) {
                phoneError.textContent = 'เบอร์โทรต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06 08 09 เท่านั้น';
            } else {
                phoneError.textContent = '';
            }
        });

        // ─── Real-time: ชื่อบัญชี ───
        const accountNameInput = document.getElementById('accountNameInput');
        const accountNameError = document.getElementById('accountNameError');
        accountNameInput.addEventListener('input', () => {
            accountNameError.textContent = accountNameInput.value.trim() ? '' : 'กรุณากรอกชื่อบัญชี';
        });

        // ─── Real-time: เลขบัญชีธนาคาร ───
        const accountNumberInput = document.getElementById('accountNumberInput');
        const accountNumberError = document.getElementById('accountNumberError');
        accountNumberInput.addEventListener('input', () => {
            const v = accountNumberInput.value.trim();
            if (!v) {
                accountNumberError.textContent = 'กรุณากรอกเลขบัญชีธนาคาร';
            } else if (!accountRegex.test(v)) {
                accountNumberError.textContent = 'เลขบัญชีต้องเป็นตัวเลข 10-20 หลัก (ใส่ขีด - ได้)';
            } else {
                accountNumberError.textContent = '';
            }
        });

        // ─── Real-time: ยืนยันรหัสผ่านใหม่ ───
        const newPw = document.getElementById('newPassword');
        const confirmPw = document.getElementById('confirmPassword');
        const confirmPwError = document.getElementById('confirmPasswordError');
        function checkMatch() {
            if (!confirmPw.value) { confirmPwError.textContent = ''; return; }
            confirmPwError.textContent = newPw.value === confirmPw.value ? '' : 'รหัสผ่านไม่ตรงกัน';
        }
        newPw.addEventListener('input', checkMatch);
        confirmPw.addEventListener('input', checkMatch);

        // ─── Validate bank form ก่อน submit ───
        document.getElementById('bankForm').addEventListener('submit', function (e) {
            const bank = document.getElementById('bankNameSelect').value;
            const accName = accountNameInput.value.trim();
            const acc = accountNumberInput.value.trim();

            if (!bank) {
                e.preventDefault();
                document.getElementById('bankNameError').textContent = 'กรุณาเลือกธนาคาร';
                return;
            }
            if (!accName) {
                e.preventDefault();
                accountNameError.textContent = 'กรุณากรอกชื่อบัญชี';
                return;
            }
            if (!acc || !accountRegex.test(acc)) {
                e.preventDefault();
                accountNumberError.textContent = 'กรุณากรอกเลขบัญชีให้ถูกต้อง';
                return;
            }
        });

        // ─── Preview ไฟล์ลายเซ็นก่อนอัปโหลด ───
        const signatureFileInput = document.getElementById('signatureFileInput');
        signatureFileInput.addEventListener('change', function (e) {
            const file = e.target.files[0];
            const wrap = document.getElementById('signaturePreviewWrap');
            const img = document.getElementById('signaturePreviewImg');
            if (!file) { wrap.style.display = 'none'; return; }

            const reader = new FileReader();
            reader.onload = function (ev) {
                img.src = ev.target.result;
                wrap.style.display = 'block';
            };
            reader.readAsDataURL(file);
        });

        // ─── Validate profileForm ก่อน submit ───
        document.getElementById('profileForm').addEventListener('submit', function (e) {
            const fn = document.getElementById('firstname').value.trim();
            const ln = document.getElementById('lastname').value.trim();
            const em = emailInput.value.trim();
            const ph = phoneInput.value.trim();

            if (!fn || !nameRegex.test(fn)) {
                e.preventDefault();
                document.getElementById('firstnameError').textContent = 'ชื่อต้องไม่ว่างและใช้ได้เฉพาะภาษาไทยและอังกฤษเท่านั้น';
                return;
            }
            if (!ln || !nameRegex.test(ln)) {
                e.preventDefault();
                document.getElementById('lastnameError').textContent = 'นามสกุลต้องไม่ว่างและใช้ได้เฉพาะภาษาไทยและอังกฤษเท่านั้น';
                return;
            }
            if (!em || !emailRegex.test(em)) {
                e.preventDefault();
                emailError.textContent = 'กรุณากรอกอีเมลให้ถูกต้อง เช่น name@example.com';
                return;
            }
            if (!ph || !phoneRegex.test(ph)) {
                e.preventDefault();
                phoneError.textContent = 'เบอร์โทรต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06 08 09 เท่านั้น';
                return;
            }
        });

        // ─── Validate passwordForm ก่อน submit ───
        document.getElementById('passwordForm').addEventListener('submit', function (e) {
            const cur = document.getElementById('currentPassword').value.trim();
            const np = newPw.value.trim();
            const cp = confirmPw.value.trim();

            if (!cur || !np || !cp) {
                e.preventDefault();
                showToast('กรุณากรอกรหัสผ่านให้ครบทุกช่อง', true);
                return;
            }
            if (np.length < 6) {
                e.preventDefault();
                document.getElementById('newPasswordError').textContent = 'รหัสผ่านใหม่ต้องมีอย่างน้อย 6 ตัวอักษร';
                return;
            }
            if (np !== cp) {
                e.preventDefault();
                confirmPwError.textContent = 'รหัสผ่านใหม่และยืนยันรหัสผ่านไม่ตรงกัน';
                return;
            }
        });