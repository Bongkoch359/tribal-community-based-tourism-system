 // ─── สิ่งอำนวยความสะดวก: ไอคอน (อ้างอิงชุดเดียวกับหน้าแก้ไข) ──────────────
        const FAC_ICONS = {
            'wifi': '📶',
            'wi-fi': '📶',

            'แอร์': '❄️',
            'เครื่องปรับอากาศ': '❄️',

            'น้ำอุ่น': '🚿',
            'ฝักบัวน้ำอุ่น': '🚿',

            'พัดลม': '🌀',

            'ที่จอดรถ': '🚗',
            'จอดรถ': '🚗',

            'ทีวี': '📺',
            'tv': '📺',

            'อาหารเช้า': '🍳',

            'ชุดชนเผ่าสำหรับถ่ายรูป': '👘'
        };
        function getFacIcon(name) {
            const key = (name || '').toLowerCase().trim();
            for (const [k, v] of Object.entries(FAC_ICONS)) {
                if (key.includes(k)) return v;
            }
            return '✨';
        }
        document.querySelectorAll('.fac-icon[data-name]').forEach(el => {
            el.textContent = getFacIcon(el.dataset.name);
        });


        // ─── Drag & Drop / Click upload ──────────────────────────────────────────
        function handleDragOver(e) { e.preventDefault(); document.getElementById('dropZone').classList.add('dragover'); }
        function handleDragLeave() { document.getElementById('dropZone').classList.remove('dragover'); }
        function handleDrop(e) { e.preventDefault(); handleDragLeave(); handleFileSelect(e.dataTransfer.files); }

        // ─── ไฟล์รูปที่เลือกไว้ (DataTransfer list) ───────────────────────────────
        let selectedFiles = [];
        const MAX_IMAGES = 5;

        function handleFileSelect(files) {
            const MAX_SIZE = 5 * 1024 * 1024; // 5 MB
            const currentCount = selectedFiles.filter(f => f !== null).length;

            if (currentCount >= MAX_IMAGES) {
                alert('อัปโหลดได้สูงสุด ' + MAX_IMAGES + ' รูปเท่านั้น');
                document.getElementById('roomImages').value = '';
                return;
            }

            let addedCount = 0;
            Array.from(files).forEach(file => {
                if (currentCount + addedCount >= MAX_IMAGES) {
                    alert('อัปโหลดได้สูงสุด ' + MAX_IMAGES + ' รูปเท่านั้น (บางไฟล์ไม่ถูกเพิ่ม)');
                    return;
                }
                if (file.size > MAX_SIZE) { alert('ไฟล์ ' + file.name + ' ใหญ่เกิน 5 MB'); return; }
                if (!file.type.startsWith('image/')) return;

                selectedFiles.push(file);
                addPreviewCard(file, selectedFiles.length - 1);
                addedCount++;
            });

            document.getElementById('roomImages').value = ''; // เคลียร์ input เพื่อให้เลือกไฟล์ซ้ำได้ถ้าลบออก
            updateUI();
        }

        function addPreviewCard(file, index) {
            const grid = document.getElementById('previewGrid');
            const reader = new FileReader();
            reader.onload = e => {
                const card = document.createElement('div');
                card.className = 'preview-card';
                card.dataset.index = index;
                card.innerHTML =
                    `<img src="${e.target.result}" alt="preview">
             <button type="button" class="remove-btn" onclick="removePreview(${index})">✕</button>
             <span class="file-size-label">${(file.size / 1024).toFixed(0)} KB</span>`;
                grid.appendChild(card);
            };
            reader.readAsDataURL(file);
        }

        function removePreview(index) {
            selectedFiles[index] = null;
            const card = document.querySelector(`.preview-card[data-index="${index}"]`);
            if (card) card.remove();
            updateUI();
        }

        function updateUI() {
            const count = selectedFiles.filter(f => f !== null).length;
            const badge = document.getElementById('imageCountBadge');
            const grid = document.getElementById('previewGrid');
            if (count > 0) {
                badge.style.display = 'inline';
                badge.textContent = count + ' รูป';
                grid.style.display = 'flex';
            } else {
                badge.style.display = 'none';
                grid.style.display = 'none';
            }
        }

        // ─── Real-time validation ตัวเลขห้ามติดลบ/ห้ามเป็น 0 (แบบเดียวกับ addTour) ────
        const NUMERIC_POSITIVE_FIELDS = [
            { id: 'pricepernight', errId: 'priceError', label: 'ราคาต่อคืน' },
            { id: 'maxguest', errId: 'maxguestError', label: 'จำนวนผู้เข้าพัก' },
            { id: 'totalrooms', errId: 'totalroomsError', label: 'จำนวนห้อง' },
        ];

        function showFieldErr(id, errId, msg) {
            const el = document.getElementById(id);
            const err = document.getElementById(errId);
            if (el) el.classList.add('error');
            if (err) err.textContent = msg;
        }

        function clearFieldErr(id, errId) {
            const el = document.getElementById(id);
            const err = document.getElementById(errId);
            if (el) el.classList.remove('error');
            if (err) err.textContent = '';
        }

        NUMERIC_POSITIVE_FIELDS.forEach(f => {
            const el = document.getElementById(f.id);
            if (!el) return;
            el.addEventListener('input', () => {
                const val = el.value;
                if (val === '') {
                    clearFieldErr(f.id, f.errId); // ปล่อยให้ submit เช็คเรื่องกรอกไม่ครบเอง
                    return;
                }
                const num = parseFloat(val);
                if (isNaN(num) || num <= 0) {
                    showFieldErr(f.id, f.errId, f.label + 'ต้องมากกว่า 0 (ห้ามติดลบหรือเป็น 0)');
                } else {
                    clearFieldErr(f.id, f.errId);
                }
            });
        });
        // ─── Validate ────────────────────────────────────────────────────────────
        function validate() {
            const fields = [
                { id: 'typename', errId: 'typenameError', label: 'ประเภทที่พัก' },
                { id: 'bedtype', errId: 'bedtypeError', label: 'ประเภทเตียง' },
                { id: 'pricepernight', errId: 'priceError', label: 'ราคาต่อคืน' },
                { id: 'maxguest', errId: 'maxguestError', label: 'จำนวนผู้เข้าพัก' },
                { id: 'totalrooms', errId: 'totalroomsError', label: 'จำนวนห้อง' },
                { id: 'description', errId: 'descriptionError', label: 'คำอธิบาย' },
                { id: 'roomcondition', errId: 'roomconditionError', label: 'เงื่อนไขการเข้าพัก' },
                { id: 'status', errId: 'statusError', label: 'สถานะ' },
            ];

            // ฟิลด์ตัวเลขที่ต้อง > 0 (ห้ามติดลบ ห้ามเป็น 0)
            const numericFields = ['pricepernight', 'maxguest', 'totalrooms'];

            let isValid = true;
            let firstInvalidEl = null;

            for (const f of fields) {
                const el = document.getElementById(f.id);
                const errEl = document.getElementById(f.errId);
                let fieldValid = true;
                let msg = '';

                if (!el || !el.value || el.value.trim() === '') {
                    msg = 'กรุณากรอก' + f.label;
                    fieldValid = false;
                } else if (numericFields.includes(f.id)) {
                    const num = parseFloat(el.value);
                    if (isNaN(num)) {
                        msg = 'กรุณากรอกตัวเลขที่ถูกต้อง';
                        fieldValid = false;
                    } else if (num <= 0) {
                        msg = f.label + 'ต้องมากกว่า 0';
                        fieldValid = false;
                    }
                }

                if (errEl) errEl.textContent = fieldValid ? '' : msg;
                if (!fieldValid) {
                    isValid = false;
                    if (!firstInvalidEl) firstInvalidEl = el;
                }
            }

            if (firstInvalidEl) firstInvalidEl.focus();
            return isValid;
        }

        // ─── Submit → multipart/form-data ────────────────────────────────────────
        async function submitForm() {
            if (!validate()) return;

            const fd = new FormData();
            fd.append('homestayid', document.querySelector('input[name="homestayid"]').value);
            fd.append('typename', document.getElementById('typename').value);
            fd.append('bedtype', document.getElementById('bedtype').value);
            fd.append('pricepernight', document.getElementById('pricepernight').value);
            fd.append('maxguest', document.getElementById('maxguest').value);
            fd.append('totalrooms', document.getElementById('totalrooms').value);
            fd.append('description', document.getElementById('description').value);
            fd.append('roomcondition', document.getElementById('roomcondition').value);
            fd.append('status', document.getElementById('status').value);

            document.querySelectorAll('input[name="facilitiesIds"]:checked').forEach(cb => {
                fd.append('facilitiesIds', cb.value);
            });
            selectedFiles.filter(f => f !== null).forEach(file => {
                fd.append('images', file);
            });

            const btn = document.querySelector('.btn-save');
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> กำลังบันทึก...';

            try {
                const res = await fetch('/addroom', { method: 'POST', body: fd });
                const data = await res.json();

                if (data.success) {
                    showSuccessModal();
                } else {
                    alert('เกิดข้อผิดพลาด: ' + (data.message || ''));
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fas fa-floppy-disk"></i> บันทึกประเภทที่พัก';
                }
            } catch (err) {
                alert('ไม่สามารถเชื่อมต่อ server');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-floppy-disk"></i> บันทึกประเภทที่พัก';
            }
        }

        function goToList() {
            const homestayid = document.querySelector('input[name="homestayid"]').value;
            window.location.href = '/owner/rooms?homestayid=' + homestayid;
        }

        function showSuccessModal() {
            const modal = document.getElementById('successModal');
            const fill = document.getElementById('progressFill');

            modal.classList.add('show');


            fill.style.animation = 'none';
            fill.offsetHeight;
            fill.style.animation = 'progress-drain 2s linear forwards';

            setTimeout(() => {
                goToList();
            }, 2000);
        }