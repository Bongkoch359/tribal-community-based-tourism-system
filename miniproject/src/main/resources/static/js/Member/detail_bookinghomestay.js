
        /* ════ คำนวณจำนวนคืน ════ */
        function calcNights() {
            const ciEl = document.getElementById('js-checkin');
            const coEl = document.getElementById('js-checkout');
            const editCi = document.getElementById('edit-checkin');
            const editCo = document.getElementById('edit-checkout');
            const isEditing = document.getElementById('stayCard')?.classList.contains('editing-mode');
            let ciStr = isEditing && editCi ? editCi.value : (ciEl ? ciEl.dataset.date : null);
            let coStr = isEditing && editCo ? editCo.value : (coEl ? coEl.dataset.date : null);
            if (ciStr && coStr) {
                const ci = new Date(ciStr), co = new Date(coStr);
                if (!isNaN(ci) && !isNaN(co)) {
                    const nights = Math.round((co - ci) / 86400000);
                    const el = document.getElementById('nights-display');
                    if (el) el.textContent = (nights > 0 ? nights : 0) + ' คืน';
                }
            }
        }
        window.addEventListener('DOMContentLoaded', calcNights);
        const editCiInput = document.getElementById('edit-checkin');
        const editCoInput = document.getElementById('edit-checkout');
        if (editCiInput) editCiInput.addEventListener('change', calcNights);
        if (editCoInput) editCoInput.addEventListener('change', calcNights);

        /* ════ Inline Edit Toggle ════ */
        let editing = false;
        function setEditCards(on) {
            /* ⭐ FIX: เอา 'noteCard' ออกจาก array เพราะการ์ดนี้ถูกลบไปแล้ว
               (หมายเหตุถูกย้ายไปรวมกับ stayCard) เหลือแค่ 2 การ์ดที่ต้องสลับโหมดแก้ไข */
            ['stayCard', 'guestCard'].forEach(function (id) {
                const el = document.getElementById(id);
                if (!el) return;
                el.classList.toggle('editing-mode', on);
                el.classList.toggle('is-editable', on);
            });
        }
        function toggleEdit() {
            editing = !editing;
            const btn = document.getElementById('editToggleBtn');
            if (editing) {
                setEditCards(true);
                if (btn) { btn.innerHTML = '✕ ปิดแก้ไข'; btn.classList.add('editing'); }
                const stay = document.getElementById('stayCard');
                if (stay) stay.scrollIntoView({ behavior: 'smooth', block: 'start' });
            } else {
                exitEdit(btn);
            }
        }
        function cancelEdit() { exitEdit(document.getElementById('editToggleBtn')); }
        function exitEdit(btn) {
            editing = false;
            setEditCards(false);
            if (btn) { btn.innerHTML = '✏️ แก้ไขการจอง'; btn.classList.remove('editing'); }
            calcNights();
        }

        /* ════ FIELD VALIDATION (แดงใต้ช่อง) ════ */
        function showFieldError(input, errEl, msg) {
            if (!errEl) return;
            if (msg) {
                errEl.textContent = '⚠ ' + msg;
                errEl.classList.add('show');
                input.classList.add('input-error');
            } else {
                errEl.textContent = '';
                errEl.classList.remove('show');
                input.classList.remove('input-error');
            }
        }

        function getErrEl(input) {
            let el = input.nextElementSibling;
            if (el && el.classList.contains('field-error')) return el;
            let parent = input.closest('div');
            return parent ? parent.querySelector('.field-error') : null;
        }

        function validateNumberField(input) {
            const errEl = getErrEl(input);
            const label = input.dataset.label || 'ค่านี้';
            const val = input.value.trim();
            const min = input.min !== '' ? Number(input.min) : null;
            const max = input.max !== '' ? Number(input.max) : null;

            if (input.hasAttribute('required') && val === '') {
                showFieldError(input, errEl, `กรุณากรอก${label}`);
                return false;
            }
            if (val === '') { showFieldError(input, errEl, ''); return true; }

            const num = Number(val);
            if (isNaN(num) || !Number.isInteger(num)) {
                showFieldError(input, errEl, `${label}ต้องเป็นตัวเลขจำนวนเต็ม`);
                return false;
            }
            if (min !== null && num < min) {
                showFieldError(input, errEl, `${label}ต้องไม่น้อยกว่า ${min}`);
                return false;
            }
            if (max !== null && num > max) {
                showFieldError(input, errEl, `${label}ต้องไม่เกิน ${max}`);
                return false;
            }
            showFieldError(input, errEl, '');
            return true;
        }

        function validateTextField(input) {
            const errEl = getErrEl(input);
            const label = input.dataset.label || 'ค่านี้';
            const val = input.value.trim();
            if (input.hasAttribute('required') && val === '') {
                showFieldError(input, errEl, `กรุณากรอก${label}`);
                return false;
            }
            showFieldError(input, errEl, '');
            return true;
        }

        function validateDates() {
            const ciInput = document.getElementById('edit-checkin');
            const coInput = document.getElementById('edit-checkout');
            if (!ciInput || !coInput) return true;

            const ciErr = document.getElementById('err-checkin');
            const coErr = document.getElementById('err-checkout');
            let ok = true;

            if (!ciInput.value) { showFieldError(ciInput, ciErr, 'กรุณาเลือกวันเช็คอิน'); ok = false; }
            else showFieldError(ciInput, ciErr, '');

            if (!coInput.value) { showFieldError(coInput, coErr, 'กรุณาเลือกวันเช็คเอาท์'); ok = false; }
            else showFieldError(coInput, coErr, '');

            if (ciInput.value && coInput.value) {
                const ci = new Date(ciInput.value), co = new Date(coInput.value);
                if (co <= ci) {
                    showFieldError(coInput, coErr, 'วันเช็คเอาท์ต้องอยู่หลังวันเช็คอิน');
                    ok = false;
                }
            }
            return ok;
        }

        /* ผูก event แบบ real-time */
        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('#editForm input[type="number"]').forEach(function (input) {
                input.addEventListener('input', function () { validateNumberField(input); });
                input.addEventListener('blur', function () { validateNumberField(input); });
            });

            document.querySelectorAll('.guest-firstname, .guest-lastname').forEach(function (input) {
                input.addEventListener('input', function () { validateTextField(input); });
                input.addEventListener('blur', function () { validateTextField(input); });
            });

            const ci = document.getElementById('edit-checkin');
            const co = document.getElementById('edit-checkout');
            if (ci) ci.addEventListener('change', validateDates);
            if (co) co.addEventListener('change', validateDates);
        });

        /* กันไม่ให้ submit ถ้ายังมี error */
        const editFormEl = document.getElementById('editForm');
        if (editFormEl) {
            editFormEl.addEventListener('submit', function (e) {
                let allValid = true;

                if (!validateDates()) allValid = false;

                editFormEl.querySelectorAll('input[type="number"]').forEach(function (input) {
                    if (!validateNumberField(input)) allValid = false;
                });

                document.querySelectorAll('.guest-firstname, .guest-lastname').forEach(function (input) {
                    if (!validateTextField(input)) allValid = false;
                });

                if (!allValid) {
                    e.preventDefault();
                    const firstError = document.querySelector('.input-error');
                    if (firstError) firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            });
        }

        /* ════ Review Modal ════ */
        function openReviewModal() { document.getElementById('reviewModal')?.classList.add('open'); }
        function closeReviewModal() { document.getElementById('reviewModal')?.classList.remove('open'); }

        const ratingLabels = ['', 'แย่มาก', 'พอใช้', 'ปานกลาง', 'ดี', 'ดีมาก'];
        document.querySelectorAll('.star-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var val = parseInt(this.dataset.val);
                document.getElementById('ratingInput').value = val;
                checkReviewReady();
                document.querySelectorAll('.star-btn').forEach(function (b, i) { b.classList.toggle('active', i < val); });
                var lbl = document.getElementById('ratingLabel');
                if (lbl) lbl.textContent = ratingLabels[val] ? (ratingLabels[val] + ' — ' + val + '/5') : 'ให้คะแนนที่พักนี้';
            });
        });

        document.querySelectorAll('.review-tag').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var ta = document.getElementById('reviewComment');
                ta.value += this.dataset.txt;
                ta.dispatchEvent(new Event('input'));
                ta.focus();
            });
        });

        /* เก็บไฟล์รูปทั้งหมดไว้ใน array แล้วค่อย sync กลับเข้า input ตอน submit */
        let reviewImageFiles = [];
        function handleReviewImages(event) {
            const files = Array.from(event.target.files);
            files.forEach(function (f) { reviewImageFiles.push(f); });
            renderImgPreview();
        }
        function removeReviewImage(idx) {
            reviewImageFiles.splice(idx, 1);
            renderImgPreview();
        }
        function renderImgPreview() {
            const grid = document.getElementById('imgPreviewGrid');
            grid.innerHTML = '';
            reviewImageFiles.forEach(function (file, idx) {
                const div = document.createElement('div');
                div.className = 'img-thumb';
                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                const rm = document.createElement('button');
                rm.type = 'button'; rm.className = 'rm-btn'; rm.innerHTML = '✕';
                rm.onclick = function () { removeReviewImage(idx); };
                div.appendChild(img); div.appendChild(rm);
                grid.appendChild(div);
            });
            const dt = new DataTransfer();
            reviewImageFiles.forEach(function (f) { dt.items.add(f); });
            document.getElementById('reviewImgInput').files = dt.files;
            checkReviewReady();
        }
        function checkReviewReady() {
            const hasRating = !!document.getElementById('ratingInput').value;
            document.getElementById('submitReviewBtn').disabled = !hasRating;
        }

        function confirmCancel() {
            document.getElementById('cancelModal').classList.add('open');
        }

        function closeCancelModal() {
            document.getElementById('cancelModal').classList.remove('open');
        }

        function submitCancel() {
            document.getElementById('cancelForm').submit();
        }

        /* ปิด modal เมื่อคลิกนอกกล่อง */
        document.querySelectorAll('.modal-overlay').forEach(function (el) {
            el.addEventListener('click', function (e) {
                if (e.target === el) el.classList.remove('open');
            });
        });

        /* ════ User Menu ════ */
        function toggleUserMenu() {
            document.getElementById('userMenuWrapper').classList.toggle('open');
        }
        document.addEventListener('click', function (e) {
            const w = document.getElementById('userMenuWrapper');
            if (w && !w.contains(e.target)) w.classList.remove('open');
        });

        /* ════ Auto Print เมื่อมาจากปุ่ม "ดูใบเสร็จ" ════ */
        window.addEventListener('load', function () {
            const params = new URLSearchParams(window.location.search);
            if (params.get('print') === 'true') {
                setTimeout(function () {
                    window.print();
                }, 300);
            }
        });