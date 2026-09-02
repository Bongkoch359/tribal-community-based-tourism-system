// ── รูปภาพ: รูปเดิม (จาก DB) + รูปใหม่ (ไฟล์จริง) รวมอยู่ใน grid เดียวกัน
        //    ลบได้ทีละรูป ไม่ว่าจะเป็นรูปเดิมหรือรูปใหม่ ── รูปแบบเดียวกับหน้าแก้ไขทัวร์ ──
        const fileInput = document.getElementById('fileInput');
        const imgGrid = document.getElementById('imgGrid');
        const imgCountNote = document.getElementById('imgCountNote');
        const uploadZone = document.getElementById('uploadZone');
        const existingImagesDataEl = document.getElementById('existingImagesData');
        const keepImagesInput = document.getElementById('keepImagesInput');

        const MAX_IMAGES = 5;

        // รูปเดิมจาก DB (คั่นด้วย ||) — ยังเก็บไว้จนกว่าผู้ใช้จะกดลบ
        let existingImages = existingImagesDataEl && existingImagesDataEl.value
            ? existingImagesDataEl.value.split('||').filter(Boolean)
            : [];

        let fileList = []; // เก็บ File object จริง ๆ ของรูปที่เลือกใหม่

        fileInput.addEventListener('change', function () {
            // ต้อง reset value "ก่อน" เรียก addFiles() เสมอ
            // เพราะ addFiles() -> syncFileInput() จะ set this.files ใหม่ตามรายการที่เลือกไว้
            // ถ้า reset (this.value = '') ทีหลัง จะไปล้าง FileList ที่เพิ่ง set ไปทิ้งทั้งหมด
            // ทำให้ preview ขึ้นแต่ตอน submit จริงไฟล์รูปกลับว่างเปล่า (รูปไม่บันทึกลง DB)
            const files = Array.from(this.files);
            this.value = '';
            addFiles(files);
        });

        function addFiles(files) {
            files.forEach(file => {
                if (!file || !file.type.startsWith('image/')) return;
                if (existingImages.length + fileList.length >= MAX_IMAGES) return;
                fileList.push(file);
            });
            syncFileInput();
            renderGrid();
        }

        // อัปเดต input.files ให้ตรงกับ fileList (จำเป็นเวลาลบรูปออกจาก preview)
        function syncFileInput() {
            const dt = new DataTransfer();
            fileList.forEach(file => dt.items.add(file));
            fileInput.files = dt.files;
        }

        // ส่งรายชื่อรูปเดิมที่ยังเหลืออยู่ (หลังลบ) กลับไปให้ backend ผ่าน hidden input
        function syncKeepImagesInput() {
            keepImagesInput.value = existingImages.join('||');
        }

        function renderGrid() {
            imgGrid.innerHTML = '';

            // รูปเดิมจาก DB
            existingImages.forEach((url, idx) => {
                const thumb = document.createElement('div');
                thumb.className = 'img-thumb';

                const img = document.createElement('img');
                img.src = url;
                img.alt = 'รูปโพสต์ปัจจุบัน';

                const badge = document.createElement('span');
                badge.className = 'existing-badge';
                badge.textContent = 'ปัจจุบัน';

                const overlay = document.createElement('div');
                overlay.className = 'thumb-overlay';

                const delBtn = document.createElement('button');
                delBtn.type = 'button';
                delBtn.className = 'thumb-btn del-btn';
                delBtn.innerHTML = '<i class="fas fa-xmark"></i>';
                delBtn.onclick = () => {
                    existingImages.splice(idx, 1);
                    syncKeepImagesInput();
                    renderGrid();
                };

                overlay.appendChild(delBtn);
                thumb.appendChild(img);
                thumb.appendChild(badge);
                thumb.appendChild(overlay);
                imgGrid.appendChild(thumb);
            });

            // รูปใหม่ที่เพิ่งเลือก/ลากมาวาง
            fileList.forEach((file, idx) => {
                const thumb = document.createElement('div');
                thumb.className = 'img-thumb';

                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                img.onload = () => URL.revokeObjectURL(img.src);

                const overlay = document.createElement('div');
                overlay.className = 'thumb-overlay';

                const delBtn = document.createElement('button');
                delBtn.type = 'button';
                delBtn.className = 'thumb-btn del-btn';
                delBtn.innerHTML = '<i class="fas fa-xmark"></i>';
                delBtn.onclick = () => {
                    fileList.splice(idx, 1);
                    syncFileInput();
                    renderGrid();
                };

                overlay.appendChild(delBtn);
                thumb.appendChild(img);
                thumb.appendChild(overlay);
                imgGrid.appendChild(thumb);
            });

            const total = existingImages.length + fileList.length;
            if (total > 0) {
                imgCountNote.style.display = '';
                imgCountNote.textContent = `ทั้งหมด ${total}/${MAX_IMAGES} รูป`;
            } else {
                imgCountNote.style.display = 'none';
            }
        }

        syncKeepImagesInput();
        renderGrid();

        // ── Drag & drop ──
        function handleDragOver(e) {
            e.preventDefault();
            uploadZone.classList.add('drag-over');
        }

        function handleDragLeave(e) {
            e.preventDefault();
            uploadZone.classList.remove('drag-over');
        }

        function handleDrop(e) {
            e.preventDefault();
            uploadZone.classList.remove('drag-over');
            const files = Array.from(e.dataTransfer.files || []);
            addFiles(files);
        }

        // ── แสดง/ซ่อนฟิลด์สถานที่ ตามการเลือกทัวร์ ──
        const tourSelect = document.getElementById('tourSelect');
        const locationGroup = document.getElementById('locationGroup');
        const locationInput = document.getElementById('locationInput');

        // ✅ FIX #1: เหลือฟังก์ชันนี้เพียงตัวเดียว (เดิมมีประกาศซ้ำด้านล่างและไปทับตัวนี้
        // ทำให้ initLocationMap() ไม่เคยถูกเรียก แผนที่เลยไม่ขึ้น)
        function toggleLocationField() {
            if (tourSelect.value) {
                locationGroup.style.display = '';
                locationInput.setAttribute('required', 'required');
                initLocationMap();
            } else {
                locationGroup.style.display = 'none';
                locationInput.removeAttribute('required');
                locationInput.value = '';
            }
        }
        // ✅ FIX #2: ย้ายการเรียก tourSelect.addEventListener(...) และ toggleLocationField()
        // ไปไว้ท้ายสุดของสคริปต์แทน (ดูด้านล่างสุด) เพราะเดิมเรียกตรงนี้ทันที
        // ซึ่งไปเรียก initLocationMap() ที่อ้างถึงตัวแปร `let locationMap` ที่ยังไม่ถูกประกาศ
        // (อยู่หลังจุดนี้ในไฟล์) ทำให้เกิด ReferenceError: Cannot access 'locationMap'
        // before initialization เพราะ let/const ไม่ถูก hoist แบบมีค่าเหมือน function

        // ═══ LEAFLET + NOMINATIM สำหรับฟิลด์ "สถานที่" ═══
        const DEFAULT_MAP_CENTER = [18.7883, 98.9853]; // เชียงใหม่
        const NOMINATIM_BASE = 'https://nominatim.openstreetmap.org';
        let locationMap, locationMarker;

        function createLeafletMap(divId) {
            const map = L.map(divId, { center: DEFAULT_MAP_CENTER, zoom: 13 });
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);
            return map;
        }

        function reverseGeocode(lat, lng, callback) {
            fetch(`${NOMINATIM_BASE}/reverse?format=jsonv2&lat=${lat}&lon=${lng}&accept-language=th`)
                .then(res => res.json())
                .then(data => { if (data && data.display_name) callback(data.display_name); })
                .catch(() => { });
        }

        function searchPlaces(query, callback) {
            if (!query || query.trim().length < 3) { callback([]); return; }
            const params = new URLSearchParams({
                format: 'jsonv2', q: query, countrycodes: 'th',
                'accept-language': 'th', limit: '5'
            });
            fetch(`${NOMINATIM_BASE}/search?${params.toString()}`)
                .then(res => res.json())
                .then(data => callback(data || []))
                .catch(() => callback([]));
        }

        function attachPlaceSearch(inputEl, suggestBoxEl, onSelect, timerRef) {
            inputEl.addEventListener('input', () => {
                clearTimeout(timerRef.id);
                const query = inputEl.value;
                timerRef.id = setTimeout(() => {
                    suggestBoxEl.innerHTML = '<div class="search-suggest-loading">กำลังค้นหา...</div>';
                    suggestBoxEl.style.display = 'block';
                    searchPlaces(query, (results) => {
                        if (results.length === 0) { suggestBoxEl.style.display = 'none'; return; }
                        suggestBoxEl.innerHTML = '';
                        results.forEach(place => {
                            const item = document.createElement('div');
                            item.className = 'search-suggest-item';
                            item.textContent = place.display_name;
                            item.addEventListener('click', () => {
                                inputEl.value = place.display_name;
                                suggestBoxEl.style.display = 'none';
                                onSelect(parseFloat(place.lat), parseFloat(place.lon), place.display_name);
                            });
                            suggestBoxEl.appendChild(item);
                        });
                        suggestBoxEl.style.display = 'block';
                    });
                }, 500);
            });
            document.addEventListener('click', (e) => {
                if (!inputEl.contains(e.target) && !suggestBoxEl.contains(e.target)) {
                    suggestBoxEl.style.display = 'none';
                }
            });
        }

        function getCurrentPositionOrDefault(onLocated, onFallback) {
            if (!navigator.geolocation) { onFallback(); return; }
            navigator.geolocation.getCurrentPosition(
                (pos) => onLocated([pos.coords.latitude, pos.coords.longitude]),
                () => onFallback(),
                { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
            );
        }

        function initLocationMap() {
            if (locationMap) return; // สร้างแผนที่แค่ครั้งเดียว

            locationMap = createLeafletMap('locationMap');
            locationMarker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(locationMap);

            locationMarker.on('dragend', () => {
                const pos = locationMarker.getLatLng();
                reverseGeocode(pos.lat, pos.lng, (address) => { locationInput.value = address; });
            });

            locationMap.on('click', (e) => {
                locationMarker.setLatLng(e.latlng);
                reverseGeocode(e.latlng.lat, e.latlng.lng, (address) => { locationInput.value = address; });
            });

            setTimeout(() => locationMap.invalidateSize(), 200); // กันแผนที่เบี้ยวตอนเพิ่งโผล่จาก display:none

            const existingAddress = locationInput.value.trim();
            if (existingAddress) {
                // ✅ โหมดแก้ไข: มีที่อยู่เดิมอยู่แล้ว -> ค้นหาพิกัดจากข้อความแล้วปักหมุดตำแหน่งเดิม
                const params = new URLSearchParams({
                    format: 'jsonv2', q: existingAddress, countrycodes: 'th',
                    'accept-language': 'th', limit: '1'
                });
                fetch(`${NOMINATIM_BASE}/search?${params.toString()}`)
                    .then(res => res.json())
                    .then(results => {
                        if (results && results.length > 0) {
                            const lat = parseFloat(results[0].lat);
                            const lng = parseFloat(results[0].lon);
                            locationMap.setView([lat, lng], 16);
                            locationMarker.setLatLng([lat, lng]);
                        }
                        // ถ้าหาไม่เจอ ก็ปล่อยหมุดไว้ที่ศูนย์กลางเชียงใหม่ตามเดิม ผู้ใช้ลากเองได้
                    })
                    .catch(() => { });
            } else {
                // ยังไม่มีที่อยู่ -> ใช้ตำแหน่งปัจจุบันเหมือนหน้า add
                getCurrentPositionOrDefault(
                    ([lat, lng]) => {
                        locationMap.setView([lat, lng], 16);
                        locationMarker.setLatLng([lat, lng]);
                        reverseGeocode(lat, lng, (address) => { locationInput.value = address; });
                    },
                    () => { /* หาไม่ได้ ใช้ศูนย์กลางเชียงใหม่ */ }
                );
            }

            attachPlaceSearch(
                locationInput,
                document.getElementById('locationSuggest'),
                (lat, lng) => {
                    locationMap.setView([lat, lng], 16);
                    locationMarker.setLatLng([lat, lng]);
                },
                { id: null }
            );
        }

        // ⚠️ ลบฟังก์ชัน toggleLocationField() ตัวที่สองซึ่งเคยอยู่ตรงนี้ออกไปแล้ว
        // (ของเดิมไม่มีการเรียก initLocationMap() และมันทับฟังก์ชันตัวแรกด้านบน
        // เพราะ function declaration ซ้ำชื่อกันจะถูก hoist แล้วตัวหลังชนะเสมอ)

        // ✅ FIX #2 (ต่อ): เรียก addEventListener + toggleLocationField() ตรงนี้แทน
        // ซึ่งอยู่หลังจากที่ `let locationMap, locationMarker;` และ initLocationMap()
        // ถูกประกาศไปแล้วทั้งหมด จึงไม่มีปัญหา TDZ อีกต่อไป
        tourSelect.addEventListener('change', toggleLocationField);
        toggleLocationField(); // ถ้าตอนโหลดหน้ามีทัวร์ถูกเลือกอยู่แล้ว จะสร้างแผนที่ทันที

        // ─── Auto-show modal ถ้ามี successMessage แล้ว redirect ไปหน้า list ───
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

            // ปิด modal แล้ว redirect ไปหน้า listPost
            setTimeout(() => {
                modal.classList.remove('show');
                window.location.href = '/manager/posts';
            }, 2000);
        })();