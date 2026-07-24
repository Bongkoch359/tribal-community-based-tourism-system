const MAX_IMAGES = 10;
        let imageDataList = []; // { src: 'data:...' หรือ '/uploads/tours/...' , base64: '...' หรือ null, name: '...' }
        let primaryIndex = 0;

        const imgGrid = document.getElementById('imgGrid');
        const imgCountNote = document.getElementById('imgCountNote');
        const imagesInput = document.getElementById('imagesInput');
        const uploadZone = document.getElementById('uploadZone');

        // ── โหลดรูปเดิมจาก DB ──
        (function loadExisting() {
            const el = document.getElementById('imgData');
            if (!el || !el.value) return;
            const names = el.value.split('||').map(s => s.trim()).filter(Boolean);
            names.forEach(name => {
                imageDataList.push({ src: '/uploads/tours/' + name, base64: null, name: name });
            });
            renderGrid();
        })();

        // ── Drag & Drop ──
        function handleDragOver(e) { e.preventDefault(); uploadZone.classList.add('drag-over'); }
        function handleDragLeave(e) { uploadZone.classList.remove('drag-over'); }
        function handleDrop(e) {
            e.preventDefault();
            uploadZone.classList.remove('drag-over');
            addFiles(Array.from(e.dataTransfer.files));
        }

        // ── เพิ่มไฟล์ใหม่ ──
        function addFiles(files) {
            const allowed = MAX_IMAGES - imageDataList.length;
            files = files.slice(0, allowed);
            if (files.length === 0) return;
            let loaded = 0;
            files.forEach(file => {
                if (!file.type.match(/^image\/(jpeg|png|webp)$/)) { loaded++; return; }
                const reader = new FileReader();
                reader.onload = e => {
                    imageDataList.push({ src: e.target.result, base64: e.target.result, name: file.name });
                    loaded++;
                    if (loaded === files.length) renderGrid();
                };
                reader.readAsDataURL(file);
            });
        }

        // ── Render grid ──
        function renderGrid() {
            imgGrid.innerHTML = '';
            imageDataList.forEach((img, i) => {
                const thumb = document.createElement('div');
                thumb.className = 'img-thumb' + (i === primaryIndex ? ' primary' : '');
                thumb.innerHTML = `
            <img src="${img.src}" alt="${img.name}"/>
            <div class="thumb-overlay">
                <button type="button" class="thumb-btn star-btn" onclick="setPrimary(${i})" title="ตั้งรูปหลัก"><i class="fas fa-star"></i></button>
                <button type="button" class="thumb-btn del-btn"  onclick="removeImage(${i})"  title="ลบรูป"><i class="fas fa-trash"></i></button>
            </div>
            ${i === primaryIndex ? '<div class="primary-badge">หลัก</div>' : ''}
        `;
                imgGrid.appendChild(thumb);
            });

            if (imageDataList.length > 0) {
                imgCountNote.style.display = 'block';
                imgCountNote.textContent = `เลือกแล้ว ${imageDataList.length}/${MAX_IMAGES} รูป`;
            } else {
                imgCountNote.style.display = 'none';
            }

            packImages();
        }

        // ── pack ส่งไป Controller ──
        function packImages() {
            if (imageDataList.length === 0) {
                imagesInput.value = '__KEEP__';
                return;
            }
            // ถ้ามีรูปใหม่ (base64 != null) ให้ส่ง JSON array
            const hasNew = imageDataList.some(i => i.base64 !== null);
            if (hasNew) {
                // รูปเดิมที่ไม่ได้เปลี่ยน ให้ส่ง base64 ว่างเป็น marker __OLD__:filename
                const arr = imageDataList.map((img, i) => ({
                    base64: img.base64 !== null ? img.base64 : '__OLD__:' + img.name,
                    primary: i === primaryIndex
                }));
                imagesInput.value = JSON.stringify(arr);
            } else {
                // ทุกรูปเป็นรูปเดิม — ส่งแค่ชื่อไฟล์
                const names = imageDataList.map(i => i.name).join('||');
                imagesInput.value = '__FILENAMES__:' + names;
            }
        }

        function setPrimary(index) { primaryIndex = index; renderGrid(); }
        function removeImage(index) {
            imageDataList.splice(index, 1);
            if (primaryIndex >= imageDataList.length) primaryIndex = 0;
            renderGrid();
        }

        /* ═══════════════════════════════════════════
           TOUR TYPE (เหมือนฟอร์มเพิ่มทัวร์)
           - "ทัวร์รายวัน" เท่านั้น → ล็อกวัน=1 คืน=0
           - ประเภทอื่นทั้งหมด → ให้กรอกวัน/คืนเอง
           - tourtype ตอนนี้เป็น entity (TourType) → อ่านชื่อผ่าน tour.tourtype.typename
        ═══════════════════════════════════════════ */
        const tourtypeSelect = document.getElementById('tourtypeSelect');
        const daysInput = document.getElementById('numberOfDays');
        const nightsInput = document.getElementById('numberOfNights');
        const customTypeGroup = document.getElementById('customTypeGroup');
        const tourtypeCustom = document.getElementById('tourtypeCustom');

        const KNOWN_TYPES = ['ทัวร์รายวัน', 'ทัวร์วัฒนธรรมชนเผ่า', 'ทัวร์วิถีชีวิต'];

        function applyTourTypeRules() {
            const type = tourtypeSelect.value;
            if (type === 'ทัวร์รายวัน') {
                customTypeGroup.style.display = 'none';
                daysInput.readOnly = true;
                nightsInput.readOnly = true;
                daysInput.value = 1;
                nightsInput.value = 0;
            } else {
                customTypeGroup.style.display = (type === '__custom__') ? 'block' : 'none';
                daysInput.readOnly = false;
                nightsInput.readOnly = false;
            }
        }
        tourtypeSelect.addEventListener('change', applyTourTypeRules);

        // ตั้งค่าเริ่มต้นจากข้อมูลเดิมใน DB ตอนโหลดหน้า
        // ✅ tourtype เป็น entity แล้ว ต้องอ่านชื่อผ่าน .typename และกัน null (ทัวร์ที่ยังไม่ผูกประเภท)
        (function initTourType() {
            const savedType = /*[[${tour.tourtype != null ? tour.tourtype.typename : ''}]]*/ '';
            if (!savedType) return;
            if (KNOWN_TYPES.includes(savedType)) {
                tourtypeSelect.value = savedType;
            } else {
                tourtypeSelect.value = '__custom__';
                tourtypeCustom.value = savedType;
            }
            // เก็บวัน/คืนเดิมไว้ก่อนเรียก applyTourTypeRules (เผื่อโดนล้างถ้าเป็นทัวร์รายวัน)
            const d = daysInput.value, n = nightsInput.value;
            applyTourTypeRules();
            if (tourtypeSelect.value !== 'ทัวร์รายวัน') {
                daysInput.value = d;
                nightsInput.value = n;
            }
        })();

        // ── Validate ──
        document.getElementById('editForm').addEventListener('submit', function (e) {
            const min = parseInt(document.querySelector('[name="minSeatstour"]').value);
            const max = parseInt(document.querySelector('[name="maxSeatstour"]').value);
            if (min > max) {
                e.preventDefault();
                alert('จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าหรือเท่ากับสูงสุด');
                return;
            }

            if (tourtypeSelect.value === '') {
                e.preventDefault();
                alert('กรุณาเลือกประเภททัวร์');
                return;
            }
            if (tourtypeSelect.value === '__custom__' && tourtypeCustom.value.trim() === '') {
                e.preventDefault();
                alert('กรุณาระบุชื่อประเภททัวร์');
                return;
            }

            // ส่งค่า tourtype จริงไปเป็น hidden field เพื่อให้ backend อ่านได้ (select ใช้ name="tourtypeUi" เฉยๆ)
            const finalTourType = tourtypeSelect.value === '__custom__'
                ? tourtypeCustom.value.trim()
                : tourtypeSelect.value;
            const hidden = document.createElement('input');
            hidden.type = 'hidden';
            hidden.name = 'tourtype';
            hidden.value = finalTourType;
            this.appendChild(hidden);
        });

         /* ═══════════════════════════════════════════
           ปฏิทินรอบทัวร์ — คลิก/ลากเลือกช่วงวันที่ แล้ว
           เปิด/ปิดรับจอง, สร้างรอบใหม่, หรือลบรอบ ได้ทีเดียวทั้งช่วง
           ไม่ต้องไล่กดทีละแถวเหมือนตารางแบบเดิมอีกต่อไป
        ═══════════════════════════════════════════ */
        (function () {
            // ── ตรวจสอบ element ทั้งหมดตั้งแต่ต้น ถ้าตัวไหนหาไม่เจอให้ log ชัดๆ ──
            const REQUIRED_IDS = ['calGrid', 'calMonthLabel', 'calPrevBtn', 'calNextBtn', 'calToolbar',
                'calSelLabel', 'calSelNote', 'calOpenBtn', 'calCloseBtn', 'calCreateBtn', 'calDeleteBtn', 'calClearBtn'];
            const missing = REQUIRED_IDS.filter(id => !document.getElementById(id));
            if (missing.length > 0) {
                console.error('❌ ปฏิทินรอบทัวร์: หา element ไม่เจอ ->', missing);
            }
            console.log('📅 window.scheduleData ตอนโหลดหน้า:', window.scheduleData);
            if (!Array.isArray(window.scheduleData)) {
                console.error('❌ window.scheduleData ไม่ใช่ array! อาจเกิดจาก syntax error ใน script ก่อนหน้า (ตัว th:inline="javascript")');
            }
            const THAI_MONTHS = ['มกราคม', 'กุมภาพันธ์', 'มีนาคม', 'เมษายน', 'พฤษภาคม', 'มิถุนายน',
                'กรกฎาคม', 'สิงหาคม', 'กันยายน', 'ตุลาคม', 'พฤศจิกายน', 'ธันวาคม'];

            const calGrid = document.getElementById('calGrid');
            const calMonthLabel = document.getElementById('calMonthLabel');
            const calPrevBtn = document.getElementById('calPrevBtn');
            const calNextBtn = document.getElementById('calNextBtn');
            const calToolbar = document.getElementById('calToolbar');
            const calSelLabel = document.getElementById('calSelLabel');
            const calSelNote = document.getElementById('calSelNote');
            const calOpenBtn = document.getElementById('calOpenBtn');
            const calCloseBtn = document.getElementById('calCloseBtn');
            const calCreateBtn = document.getElementById('calCreateBtn');
            const calDeleteBtn = document.getElementById('calDeleteBtn');
            const calClearBtn = document.getElementById('calClearBtn');

            const todayISO = new Date().toISOString().slice(0, 10);
            const today = new Date();
            let currentYear = today.getFullYear();
            let currentMonth = today.getMonth(); // 0-indexed

            let scheduleByDate = {};
            let selStart = null, selEnd = null;
            let isDragging = false, dragAnchorISO = null;

            function pad2(n) { return String(n).padStart(2, '0'); }
            function isValidISODate(iso) {
                if (typeof iso !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(iso)) return false;
                const [y, m, d] = iso.split('-').map(Number);
                const dt = new Date(Date.UTC(y, m - 1, d));
                return dt.getUTCFullYear() === y && dt.getUTCMonth() === m - 1 && dt.getUTCDate() === d;
            }
            function addDaysISO(iso, days) {
                if (!isValidISODate(iso)) return null;
                const [y, m, d] = iso.split('-').map(Number);
                const dt = new Date(Date.UTC(y, m - 1, d));
                dt.setUTCDate(dt.getUTCDate() + days);
                return dt.toISOString().slice(0, 10);
            }
            function formatDMY(iso) {
                if (!isValidISODate(iso)) return '-';
                const [y, m, d] = iso.split('-');
                return `${d}/${m}/${y}`;
            }
            function daysBetweenInclusive(a, b) {
                if (!isValidISODate(a) || !isValidISODate(b)) return 0;
                return Math.round((new Date(b + 'T00:00:00Z') - new Date(a + 'T00:00:00Z')) / 86400000) + 1;
            }
            function rangesOverlap(aStart, aEnd, bStart, bEnd) {
                return aStart <= bEnd && bStart <= aEnd;
            }
            function findConflict(startISO, endISO) {
                return window.scheduleData.find(s =>
                    isValidISODate(s.opendate) && isValidISODate(s.enddate) &&
                    rangesOverlap(startISO, endISO, s.opendate, s.enddate));
            }

            // ── สร้าง map วันที่ → schedule (ครอบคลุมทุกวันตั้งแต่ opendate ถึง enddate) ──
            // ข้ามรอบทัวร์ที่มีวันที่ผิดปกติ/ว่าง เพื่อไม่ให้ทั้งปฏิทินพังจากข้อมูลแค่แถวเดียว
            function buildScheduleByDate() {
                scheduleByDate = {};
                window.scheduleData.forEach(s => {
                    if (!isValidISODate(s.opendate) || !isValidISODate(s.enddate) || s.opendate > s.enddate) {
                        console.warn('ข้ามรอบทัวร์ที่มีวันที่ไม่ถูกต้อง:', s);
                        return;
                    }
                    let cursor = s.opendate;
                    let guard = 0; // กันลูปไม่รู้จบเผื่อกรณีข้อมูลผิดปกติ
                    while (cursor !== null && cursor <= s.enddate && guard < 3660) {
                        scheduleByDate[cursor] = s;
                        cursor = addDaysISO(cursor, 1);
                        guard++;
                    }
                });
            }

            function statusClass(status) {
                if (status === 'เปิดรับจอง') return 'st-open';
                if (status === 'เต็ม') return 'st-full';
                if (status === 'ปิด') return 'st-closed';
                return '';
            }

            // ── วาดปฏิทินของเดือนที่กำลังดูอยู่ ──
            function renderCalendar() {
                calMonthLabel.textContent = `${THAI_MONTHS[currentMonth]} ${currentYear + 543}`;
                calGrid.innerHTML = '';

                const firstWeekday = new Date(currentYear, currentMonth, 1).getDay();
                const totalDays = new Date(currentYear, currentMonth + 1, 0).getDate();

                for (let i = 0; i < firstWeekday; i++) {
                    const blank = document.createElement('div');
                    blank.className = 'cal-cell cal-empty';
                    calGrid.appendChild(blank);
                }

                for (let d = 1; d <= totalDays; d++) {
                    const dateISO = `${currentYear}-${pad2(currentMonth + 1)}-${pad2(d)}`;
                    const schedule = scheduleByDate[dateISO];

                    const cell = document.createElement('div');
                    cell.className = 'cal-cell' + (schedule ? ' ' + statusClass(schedule.status) : '');
                    if (dateISO === todayISO) cell.classList.add('cal-today');
                    cell.dataset.date = dateISO;

                    let sub = '';
                    if (schedule) {
                        sub = `${schedule.booked}/${tourMaxSeats}`;
                    }
                    cell.innerHTML = `<div class="cal-day-num">${d}</div>` +
                        (sub ? `<div class="cal-day-sub">${sub}</div>` : '');
                    calGrid.appendChild(cell);
                }

                // คงการเลือกช่วงเดิมไว้ (ถ้ามี) หลัง re-render กริดใหม่
                if (selStart && selEnd) applySelectionHighlight();
            }

            function applySelectionHighlight() {
                document.querySelectorAll('.cal-cell[data-date]').forEach(cell => {
                    const d = cell.dataset.date;
                    cell.classList.toggle('cal-selected', d >= selStart && d <= selEnd);
                });
            }

            function updateSelectionRange(aISO, bISO) {
                selStart = aISO <= bISO ? aISO : bISO;
                selEnd = aISO <= bISO ? bISO : aISO;
                applySelectionHighlight();
                finalizeSelection();
            }

            function finalizeSelection() {
                try {
                    if (!selStart || !selEnd) { calToolbar.style.display = 'none'; return; }
                    const inRange = window.scheduleData.filter(s => s.opendate >= selStart && s.opendate <= selEnd);
                    const total = daysBetweenInclusive(selStart, selEnd);
                    const existing = inRange.length;
                    const missing = total - existing;

                    calSelLabel.textContent = selStart === selEnd
                        ? formatDMY(selStart) + ` (1 วัน)`
                        : `${formatDMY(selStart)} - ${formatDMY(selEnd)} (${total} วัน)`;
                    calSelNote.textContent = `มีรอบทัวร์อยู่แล้ว ${existing} วัน · ยังไม่มีรอบ ${missing} วัน`;

                    calOpenBtn.disabled = existing === 0;
                    calCloseBtn.disabled = existing === 0;
                    calCreateBtn.disabled = missing === 0;
                    calDeleteBtn.disabled = existing === 0;

                    calToolbar.style.display = 'block';
                } catch (err) {
                    console.error('❌ finalizeSelection() error:', err);
                }
            }

            function clearSelection() {
                selStart = null; selEnd = null;
                document.querySelectorAll('.cal-cell.cal-selected').forEach(c => c.classList.remove('cal-selected'));
                calToolbar.style.display = 'none';
            }

            function setToolbarBusy(busy) {
                [calOpenBtn, calCloseBtn, calCreateBtn, calDeleteBtn, calClearBtn].forEach(b => b.disabled = busy);
            }

            // ── ดึงข้อมูลรอบทัวร์ล่าสุดจากหน้านี้เอง (ไม่ reload ทั้งหน้า
            //    เพื่อไม่ให้ข้อมูลฟอร์มแก้ไขทัวร์ที่ยังไม่ได้กดบันทึกหายไป) ──
            async function refreshScheduleData() {
                try {
                    const url = window.location.href.split('#')[0].split('?')[0] + '?_ts=' + Date.now();
                    const res = await fetch(url, { method: 'GET', cache: 'no-store' });
                    const html = await res.text();
                    const match = html.match(/window\.scheduleData\s*=\s*(\[[\s\S]*?\]);/);
                    if (match) {
                        const cleanJson = match[1].replace(/,(\s*)\]/g, '$1]');
                        window.scheduleData = JSON.parse(cleanJson);
                        buildScheduleByDate();
                    }
                } catch (e) {
                    console.error('รีเฟรชข้อมูลรอบทัวร์ไม่สำเร็จ:', e);
                }
                renderCalendar();
                if (selStart && selEnd) finalizeSelection();
            }

            // ── เรียก endpoint เดิมของ TourScheduleController ──
            async function bulkStatusRequest(startISO, endISO, status) {
                const res = await fetch(scheduleBulkStatusUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ startDate: startISO, endDate: endISO, status })
                });
                let body = null;
                try { body = await res.json(); } catch (e) { /* response อาจไม่ใช่ JSON ก็ได้ */ }
                return { httpOk: res.ok, message: body && body.message };
            }
            async function createScheduleRequest(opendate, enddate) {
                const res = await fetch(scheduleAddUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ opendate, enddate })
                });
                return res.ok;
            }
            async function deleteScheduleRequest(scheduleid) {
                const url = scheduleDeleteUrlTemplate.replace('__SID__', scheduleid);
                const res = await fetch(url, { method: 'POST' });
                return res.ok;
            }

            // ── ปุ่ม: เปิด/ปิดรับจองทั้งช่วง (bulk-status endpoint เดิม) ──
            async function runBulkStatus(status) {
                setToolbarBusy(true);
                const result = await bulkStatusRequest(selStart, selEnd, status);
                setToolbarBusy(false);
                if (!result.httpOk) {
                    alert(result.message || 'เกิดข้อผิดพลาด กรุณาลองใหม่');
                }
                await refreshScheduleData();
            }
            calOpenBtn.addEventListener('click', () => runBulkStatus('เปิดรับจอง'));
            calCloseBtn.addEventListener('click', () => runBulkStatus('ปิด'));

            // ── ปุ่ม: สร้างรอบทัวร์เติมเฉพาะวันที่ยังไม่มีในช่วงที่เลือก ──
            calCreateBtn.addEventListener('click', async () => {
                const tourLenDays = tourNumberOfDays || 1;
                let cursor = selStart;
                const toCreate = [];
                while (cursor !== null && cursor <= selEnd) {
                    const opendate = cursor;
                    const enddate = tourLenDays === 1 ? opendate : addDaysISO(opendate, tourLenDays - 1);
                    if (enddate && !findConflict(opendate, enddate)) toCreate.push({ opendate, enddate });
                    cursor = addDaysISO(cursor, 1);
                }
                if (toCreate.length === 0) {
                    alert('ทุกวันในช่วงนี้มีรอบทัวร์อยู่แล้ว');
                    return;
                }
                setToolbarBusy(true);
                let ok = 0, fail = 0;
                for (const item of toCreate) {
                    const success = await createScheduleRequest(item.opendate, item.enddate);
                    if (success) ok++; else fail++;
                }
                setToolbarBusy(false);
                alert(`สร้างรอบทัวร์สำเร็จ ${ok} วัน` + (fail > 0 ? ` (ล้มเหลว ${fail} วัน กรุณาตรวจสอบอีกครั้ง)` : ''));
                await refreshScheduleData();
            });

            // ── ปุ่ม: ลบรอบทัวร์ทั้งหมดในช่วงที่เลือก (ข้ามรอบที่มีคนจองแล้ว) ──
            calDeleteBtn.addEventListener('click', async () => {
                const inRange = window.scheduleData.filter(s => s.opendate >= selStart && s.opendate <= selEnd);
                if (inRange.length === 0) { alert('ไม่มีรอบทัวร์ในช่วงที่เลือก'); return; }

                const deletable = inRange.filter(s => (s.booked || 0) === 0);
                const blocked = inRange.length - deletable.length;
                if (deletable.length === 0) {
                    alert('รอบทัวร์ทั้งหมดในช่วงนี้มีคนจองแล้ว ไม่สามารถลบได้');
                    return;
                }
                const confirmed = confirm(
                    `ยืนยันลบรอบทัวร์ ${deletable.length} รอบในช่วงนี้?` +
                    (blocked > 0 ? `\n(ข้าม ${blocked} รอบที่มีคนจองแล้ว ไม่สามารถลบได้)` : '')
                );
                if (!confirmed) return;

                setToolbarBusy(true);
                let ok = 0, fail = 0;
                for (const s of deletable) {
                    const success = await deleteScheduleRequest(s.scheduleid);
                    if (success) ok++; else fail++;
                }
                setToolbarBusy(false);
                alert(`ลบสำเร็จ ${ok} รอบ` + (fail > 0 ? ` (ล้มเหลว ${fail} รอบ)` : '') +
                    (blocked > 0 ? ` · ข้าม ${blocked} รอบที่มีคนจองแล้ว` : ''));
                await refreshScheduleData();
            });

            calClearBtn.addEventListener('click', clearSelection);

            // ── ลาก/คลิกเลือกช่วงวันที่บนปฏิทิน (รองรับทั้งเมาส์และทัช ผ่าน Pointer Events) ──
            calGrid.addEventListener('pointerdown', (e) => {
                const cell = e.target.closest('.cal-cell[data-date]');
                if (!cell) return;
                isDragging = true;
                dragAnchorISO = cell.dataset.date;
                updateSelectionRange(dragAnchorISO, dragAnchorISO);
                e.preventDefault();
            });
            document.addEventListener('pointermove', (e) => {
                if (!isDragging) return;
                const el = document.elementFromPoint(e.clientX, e.clientY);
                const cell = el && el.closest('.cal-cell[data-date]');
                if (cell) updateSelectionRange(dragAnchorISO, cell.dataset.date);
            });
            document.addEventListener('pointerup', () => { isDragging = false; });

            // ── เปลี่ยนเดือน ──
            calPrevBtn.addEventListener('click', () => {
                currentMonth--;
                if (currentMonth < 0) { currentMonth = 11; currentYear--; }
                clearSelection();
                renderCalendar();
            });
            calNextBtn.addEventListener('click', () => {
                currentMonth++;
                if (currentMonth > 11) { currentMonth = 0; currentYear++; }
                clearSelection();
                renderCalendar();
            });

            // ── เริ่มต้น ──
            buildScheduleByDate();
            renderCalendar();
        })();