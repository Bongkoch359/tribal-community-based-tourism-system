// ── รูปภาพ base64 ──
        const fileInput = document.getElementById('fileInput');
        const previewGrid = document.getElementById('previewGrid');
        const imagesInput = document.getElementById('imagesInput');
        const dropZone = document.getElementById('dropZone');

        let base64List = []; // เก็บ base64 ทุกรูป

        fileInput.addEventListener('change', function () {
            const files = Array.from(this.files);
            files.forEach(file => readAndAdd(file));
            this.value = ''; // reset เพื่อให้เลือกซ้ำได้
        });

        function readAndAdd(file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                const b64 = e.target.result; // data:image/...;base64,...
                base64List.push(b64);
                renderPreviews();
                updateHiddenInput();
            };
            reader.readAsDataURL(file);
        }

        function renderPreviews() {
            previewGrid.innerHTML = '';
            // ถ้ามีรูปให้ซ่อน dropZone placeholder text
            dropZone.querySelector('svg').style.opacity = base64List.length ? '0' : '0.4';
            dropZone.querySelector('span').style.display = base64List.length ? 'none' : '';

            base64List.forEach((b64, idx) => {
                const wrap = document.createElement('div');
                wrap.className = 'preview-thumb';

                const img = document.createElement('img');
                img.src = b64;

                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'remove-btn';
                btn.innerHTML = '✕';
                btn.onclick = () => {
                    base64List.splice(idx, 1);
                    renderPreviews();
                    updateHiddenInput();
                };

                wrap.appendChild(img);
                wrap.appendChild(btn);
                previewGrid.appendChild(wrap);
            });
        }

        function updateHiddenInput() {
            // เก็บหลายรูปโดยคั่นด้วย || 
            imagesInput.value = base64List.join('||');
        }

        // Validate ก่อน submit
        document.getElementById('postForm').addEventListener('submit', function (e) {
            if (base64List.length === 0) {
                // อนุญาตให้บันทึกได้แม้ไม่มีรูป (optional)
                imagesInput.value = '';
            }
        });

        // ── แสดง/ซ่อนฟิลด์สถานที่ ตามการเลือกทัวร์ ──
        // เลือกทัวร์ -> แสดงฟิลด์สถานที่ (บังคับกรอก)
        // ไม่ระบุทัวร์ (ข่าวสารทั่วไป) -> ซ่อนฟิลด์สถานที่ (ไม่บังคับกรอก)
        const tourSelect = document.getElementById('tourSelect');
        const locationGroup = document.getElementById('locationGroup');
        const locationInput = document.getElementById('locationInput');

        function toggleLocationField() {
            if (tourSelect.value) {
                locationGroup.style.display = '';
                locationInput.setAttribute('required', 'required');
            } else {
                locationGroup.style.display = 'none';
                locationInput.removeAttribute('required');
                locationInput.value = '';
            }
        }
        tourSelect.addEventListener('change', toggleLocationField);
        toggleLocationField(); // เผื่อกรณีแก้ไขโพสต์ที่มีทัวร์อยู่แล้ว

        // ─── Auto-show modal ถ้ามี successMessage ───
        (function () {
            const el = document.getElementById('serverMsg');
            const msg = el ? el.dataset.success : null;
            if (!msg || msg === 'null' || msg.trim() === '') return;

            const modal = document.getElementById('successModal');
            const desc = document.getElementById('modalDesc');
            const fill = document.getElementById('progressFill');

            desc.textContent = msg;
            modal.classList.add('show');

            fill.style.animation = 'none';
            fill.offsetHeight;
            fill.style.animation = 'progress-drain 2s linear forwards';

            // ปิด modal แล้ว redirect ไปหน้า list
            setTimeout(() => {
                modal.classList.remove('show');
                window.location.href = '/manager/posts';
            }, 2000);
        })();