  let editing = false;

        function setEditCards(on) {
            ['tourCard', 'guestCard', 'noteCard', 'insuranceCard'].forEach(function (id) {
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
                const card = document.getElementById('tourCard');
                if (card) card.scrollIntoView({ behavior: 'smooth', block: 'start' });
                recalcTotals();
            } else {
                cancelEdit();
            }
        }

        function cancelEdit() {
            // 1) คืนค่าทุกช่องเป็นค่าเดิมจากเซิร์ฟเวอร์
            const form = document.getElementById('editForm');
            if (form) form.reset();

            // 2) เอาแถวที่เพิ่มด้วย JS ออก แล้วเปิดแถวเดิมกลับมาทั้งหมด
            document.querySelectorAll('#guestList .guest-new').forEach(function (el) { el.remove(); });
            document.querySelectorAll('#guestList .guest-item').forEach(function (r) {
                delete r.dataset.removed;
                setGuestRowActive(r, true);
            });
            const hint = document.getElementById('guestCountHint');
            if (hint) hint.style.display = 'none';

            // 3) sync UI ประเภทจุดรับให้ตรงกับค่าเดิม (reset ไม่ได้คืน class/การซ่อนกล่อง)
            const checked = document.querySelector('input[name="pickuptype"]:checked');
            if (checked) selectPickupType(checked.value);

            // 4) คำนวณราคาใหม่จากค่าเดิม (ข้าม sync เพราะเพิ่งคืนแถวเรียบร้อยแล้ว)
            recalcTotals(true);

            exitEdit(document.getElementById('editToggleBtn'));
            clearAllFieldErrors();
        }

        function exitEdit(btn) {
            editing = false;
            setEditCards(false);
            if (btn) { btn.innerHTML = '✏️ แก้ไขการจอง'; btn.classList.remove('editing'); }
        }

        /* ════ Pickup Type Radio (เหมือนหน้าจองใหม่) ════ */
        function selectPickupType(type) {
            const centralOpt = document.getElementById('pickupOptCentral');
            const hotelOpt = document.getElementById('pickupOptHotel');
            const meetingBox = document.getElementById('meetingPointBox');
            const locInline = document.getElementById('pickupLocationInline');
            const locInput = document.getElementById('pickuplocation-input');

            const isCentral = type === 'จุดรับส่วนกลาง';

            document.querySelectorAll('input[name="pickuptype"]').forEach(function (r) {
                r.checked = (r.value === type);
            });

            if (centralOpt) centralOpt.classList.toggle('radio-selected', isCentral);
            if (hotelOpt) hotelOpt.classList.toggle('radio-selected', !isCentral);

            if (meetingBox) meetingBox.style.display = isCentral ? '' : 'none';
            if (locInline) locInline.style.display = isCentral ? 'none' : '';

            if (isCentral && locInput) locInput.value = '';

            clearFieldError(document.getElementById('pickupRadioGroup'), document.getElementById('pickupTypeError'));

            // ประเภทจุดรับเปลี่ยน อาจทำให้สถานะ error ของ "สถานที่รับ" เปลี่ยนตามไปด้วย
            if (locInput) validatePickupLocation(locInput);
        }

        /* ════ Live Price Recalculation (จำนวนคน x ราคา + ประกัน) ════ */
        function recalcTotals(skipSync) {
            const adultEl = document.getElementById('adult-input');
            const childrenEl = document.getElementById('children-input');
            if (!adultEl) return; // ไม่มี section ทัวร์ให้แก้ (detail == null)

            const adultQty = parseInt(adultEl.value, 10) || 0;
            const childQty = childrenEl ? (parseInt(childrenEl.value, 10) || 0) : 0;
            const adultPrice = parseFloat(adultEl.dataset.price) || 0;
            const childPrice = childrenEl ? (parseFloat(childrenEl.dataset.price) || 0) : 0;
            const totalGuests = adultQty + childQty;

            const tourSubtotal = (adultQty * adultPrice) + (childQty * childPrice);

            const insuranceYes = true; // ประกันฟิกเป็น true เสมอ
            const insuranceSection = document.getElementById('insuranceSection');
            const fee = insuranceSection ? (parseFloat(insuranceSection.dataset.fee) || 0) : 0;
            const insuranceSubtotal = insuranceYes ? (totalGuests * fee) : 0;

            const grandTotal = tourSubtotal + insuranceSubtotal;
            const fmt = function (n) { return Math.round(n).toLocaleString('th-TH'); };

            const guestDisplay = document.getElementById('totalGuestDisplay');
            if (guestDisplay) guestDisplay.textContent = totalGuests;

            const tourSubEl = document.getElementById('tourSubtotalDisplay');
            if (tourSubEl) tourSubEl.textContent = fmt(tourSubtotal);

            const insGuestCountEl = document.getElementById('insuranceGuestCountDisplay');
            if (insGuestCountEl) insGuestCountEl.textContent = totalGuests;

            const insSubEl = document.getElementById('insuranceSubtotalDisplay');
            if (insSubEl) insSubEl.textContent = fmt(insuranceSubtotal);

            const insuranceTotalRow = document.getElementById('insuranceTotalRowWrap');
            if (insuranceTotalRow) insuranceTotalRow.style.display = insuranceYes ? '' : 'none';

            const sidebarRow = document.getElementById('sidebarInsuranceRow');
            if (sidebarRow) sidebarRow.style.display = insuranceYes ? '' : 'none';

            const sidebarVal = document.getElementById('sidebarInsuranceValue');
            if (sidebarVal) sidebarVal.textContent = fmt(insuranceSubtotal);

            const sidebarTotalEl = document.getElementById('sidebarTotalDisplay');
            if (sidebarTotalEl) sidebarTotalEl.textContent = fmt(grandTotal);

            if (!skipSync) syncGuestRows();
        }

        /* ════ แถวรายชื่อผู้เดินทาง ════ */
        function setGuestRowActive(row, active) {
            row.style.display = active ? '' : 'none';
            // input ที่ disabled จะไม่ถูกส่งไปกับฟอร์ม → backend จะไม่เห็นคนที่ถูกลบ
            row.querySelectorAll('input').forEach(function (inp) { inp.disabled = !active; });
            const choice = row.querySelector('.guest-del-choice');
            if (choice) choice.classList.remove('show');
            if (!active) {
                row.querySelectorAll('.field-error').forEach(function (el) { el.classList.remove('field-error'); });
                row.querySelectorAll('.field-error-msg.show').forEach(function (el) { el.classList.remove('show'); });
            }
        }

        function createGuestRow() {
            const div = document.createElement('div');
            div.className = 'guest-item guest-new';
            div.dataset.booker = 'false';
            div.innerHTML = `
                <div class="guest-row">
                  <div class="guest-avatar">👤</div>
                  <div style="flex:1;">
                    <div class="inline-field">
                      <div class="guest-edit-head">
                        <button type="button" class="btn-guest-del" onclick="removeGuestRow(this)">🗑 ลบผู้เดินทางนี้</button>
                      </div>
                      <div class="guest-del-choice">
                        <span>ลบในฐานะ:</span>
                        <button type="button" onclick="chooseRemoveType(this,'adult')">ผู้ใหญ่</button>
                        <button type="button" onclick="chooseRemoveType(this,'child')">เด็ก</button>
                        <button type="button" onclick="hideDelChoice(this)">ยกเลิก</button>
                      </div>
                      <input type="hidden" name="guestId" form="editForm" value="">
                      <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
                        <div>
                          <div class="detail-label">ชื่อจริง</div>
                          <input class="inline-input guest-name-input" type="text" name="guestFirstname" form="editForm" placeholder="ชื่อ" required>
                          <div class="field-error-msg">กรุณากรอกชื่อ</div>
                        </div>
                        <div>
                          <div class="detail-label">นามสกุล</div>
                          <input class="inline-input guest-name-input" type="text" name="guestLastname" form="editForm" placeholder="นามสกุล" required>
                          <div class="field-error-msg">กรุณากรอกนามสกุล</div>
                        </div>
                      </div>
                      <div style="margin-top:8px;">
                        <div class="detail-label">เลขบัตรประชาชน (สำหรับทำประกัน) <span style="color:var(--red);">*</span></div>
                        <input class="inline-input guest-idcard-input" type="text" name="guestIdcard" form="editForm"
                               placeholder="กรอกเลข 13 หลัก" maxlength="13" inputmode="numeric" required>
                        <div class="field-error-msg">เลขบัตรประชาชนต้องเป็นตัวเลข 13 หลัก</div>
                      </div>
                    </div>
                  </div>
                </div>`;
            return div;
        }

        /* data-removed: 'auto'   = ซ่อนเพราะลดจำนวนคน (คืนได้ถ้าพิมพ์เพิ่มกลับ)
                         'manual' = ผู้ใช้กดปุ่มลบเอง (ไม่คืนอัตโนมัติ) */
        function syncGuestRows() {
            const list = document.getElementById('guestList');
            const adultEl = document.getElementById('adult-input');
            const childrenEl = document.getElementById('children-input');
            if (!list || !adultEl) return;
            if (adultEl.value.trim() === '') return; // กำลังพิมพ์ค้าง อย่าเพิ่งตัดแถว

            const total = Math.max(0,
                (parseInt(adultEl.value, 10) || 0) + (childrenEl ? (parseInt(childrenEl.value, 10) || 0) : 0));

            const rows = Array.from(list.querySelectorAll('.guest-item'));
            const bookerCount = rows.filter(function (r) { return r.dataset.booker === 'true'; }).length;
            const slots = Math.max(0, total - bookerCount);

            const active = rows.filter(function (r) {
                return r.dataset.booker !== 'true' && !r.dataset.removed;
            });

            // เกิน → ซ่อนจากท้ายรายการ
            while (active.length > slots) {
                const row = active.pop();
                setGuestRowActive(row, false);
                row.dataset.removed = 'auto';
            }

            // ขาด → คืนแถวที่ถูกซ่อนอัตโนมัติก่อน แล้วค่อยสร้างแถวว่าง
            while (active.length < slots) {
                const back = rows.find(function (r) {
                    return r.dataset.removed === 'auto' && r.dataset.booker !== 'true';
                });
                if (back) {
                    delete back.dataset.removed;
                    setGuestRowActive(back, true);
                    active.push(back);
                } else {
                    const row = createGuestRow();
                    list.appendChild(row);
                    rows.push(row);
                    active.push(row);
                }
            }

            const removed = list.querySelectorAll('.guest-item[data-removed]:not(.guest-new)').length;
            const added = list.querySelectorAll('.guest-new:not([data-removed])').length;
            const hint = document.getElementById('guestCountHint');
            if (hint) {
                if (removed > 0) {
                    hint.textContent = '⚠️ ผู้เดินทาง ' + removed + ' ท่านจะถูกลบออกจากการจองเมื่อกดบันทึก';
                    hint.style.display = '';
                } else if (added > 0) {
                    hint.textContent = '➕ เพิ่มผู้เดินทาง ' + added + ' ท่าน กรุณากรอกชื่อและเลขบัตรประชาชนให้ครบ';
                    hint.style.display = '';
                } else {
                    hint.style.display = 'none';
                }
            }
        }

        /* ════ ปุ่มลบผู้เดินทางรายคน ════ */
        function removeGuestRow(btn) {
            const row = btn.closest('.guest-item');
            const adultEl = document.getElementById('adult-input');
            const childrenEl = document.getElementById('children-input');
            if (!row || !adultEl) return;

            const adults = parseInt(adultEl.value, 10) || 0;
            const kids = childrenEl ? (parseInt(childrenEl.value, 10) || 0) : 0;
            const canAdult = adults > 1;   // ผู้ใหญ่ต้องเหลืออย่างน้อย 1
            const canChild = kids > 0;

            if (!canAdult && !canChild) {
                alert('ต้องมีผู้ใหญ่อย่างน้อย 1 ท่าน จึงไม่สามารถลบผู้เดินทางคนนี้ได้');
                return;
            }
            if (canAdult && canChild) {
                // ไม่รู้ว่าคนนี้เป็นผู้ใหญ่หรือเด็ก (กระทบราคา) ให้ผู้ใช้เลือก
                row.querySelector('.guest-del-choice').classList.add('show');
                return;
            }
            applyRemoveGuest(row, canAdult ? 'adult' : 'child');
        }

        function chooseRemoveType(btn, type) {
            const row = btn.closest('.guest-item');
            if (row) applyRemoveGuest(row, type);
        }

        function hideDelChoice(btn) {
            btn.closest('.guest-del-choice').classList.remove('show');
        }

        function applyRemoveGuest(row, type) {
            const el = document.getElementById(type === 'adult' ? 'adult-input' : 'children-input');
            if (!el) return;
            el.value = Math.max(0, (parseInt(el.value, 10) || 0) - 1);
            if (type === 'adult') validateAdult(el); else validateChildren(el);

            if (row.classList.contains('guest-new')) {
                row.remove();                       // แถวที่เติมเอง ไม่มีใน DB ลบทิ้งได้เลย
            } else {
                setGuestRowActive(row, false);      // แถวเดิม: disable เพื่อไม่ให้ส่งไป backend
                row.dataset.removed = 'manual';
            }
            validateTotalSeats();
            recalcTotals();
        }

        function findErrorMsgEl(inputEl) {
            // หา .field-error-msg ที่อยู่ถัดจาก input ใน DOM 
            let sib = inputEl.nextElementSibling;
            while (sib) {
                if (sib.classList && sib.classList.contains('field-error-msg')) return sib;
                sib = sib.nextElementSibling;
            }
            return null;
        }

        function showFieldError(inputOrGroupEl, msgEl, text) {
            if (inputOrGroupEl) inputOrGroupEl.classList.add('field-error');
            if (msgEl) {
                if (text) msgEl.textContent = text;
                msgEl.classList.add('show');
            }
        }

        function clearFieldError(inputOrGroupEl, msgEl) {
            if (inputOrGroupEl) inputOrGroupEl.classList.remove('field-error');
            if (msgEl) msgEl.classList.remove('show');
        }

        function clearAllFieldErrors() {
            document.querySelectorAll('.field-error').forEach(function (el) { el.classList.remove('field-error'); });
            document.querySelectorAll('.field-error-msg.show').forEach(function (el) { el.classList.remove('show'); });
        }

        /* วันออกเดินทาง */
        function validateTourDate(el) {
            const msgEl = findErrorMsgEl(el);
            if (!el.value) { showFieldError(el, msgEl); return false; }
            clearFieldError(el, msgEl);
            return true;
        }

        /* จำนวนผู้ใหญ่ ต้อง >= 1 */
        function validateAdult(el) {
            const msgEl = findErrorMsgEl(el);
            const v = parseInt(el.value, 10);
            if (el.value === '' || isNaN(v) || v < 1) {
                showFieldError(el, msgEl);
                return false;
            }
            clearFieldError(el, msgEl);
            return true;
        }

        /* จำนวนเด็ก ต้อง >= 0 (ห้ามติดลบ / ปล่อยว่างได้ ระบบถือเป็น 0) */
        function validateChildren(el) {
            const msgEl = findErrorMsgEl(el);
            if (el.value === '') { clearFieldError(el, msgEl); return true; }
            const v = parseInt(el.value, 10);
            if (isNaN(v) || v < 0) {
                showFieldError(el, msgEl);
                return false;
            }
            clearFieldError(el, msgEl);
            return true;
        }

        /* ชื่อ/นามสกุลผู้เดินทาง (เฉพาะที่ไม่ใช่ผู้จอง) */
        function validateGuestName(el) {
            const msgEl = findErrorMsgEl(el);
            if (!el.value.trim()) {
                showFieldError(el, msgEl);
                return false;
            }
            clearFieldError(el, msgEl);
            return true;
        }

        /* เลขบัตรประชาชน — ประกันบังคับทุกการจอง ต้องกรอกทุกคน */
        function validateIdcard(el) {
            const msgEl = findErrorMsgEl(el);
            const v = el.value.trim();
            if (!v) {
                showFieldError(el, msgEl, 'กรุณากรอกเลขบัตรประชาชน');
                return false;
            }
            if (!/^[0-9]{13}$/.test(v)) {
                showFieldError(el, msgEl, 'เลขบัตรประชาชนต้องเป็นตัวเลข 13 หลัก');
                return false;
            }
            clearFieldError(el, msgEl);
            return true;
        }

        /* สถานที่รับ — บังคับเมื่อเลือก "โรงแรม/ที่พัก" และต้องมีคำว่า "เชียงใหม่" */
        function validatePickupLocation(el) {
            const msgEl = findErrorMsgEl(el);
            const isHotel = !!document.querySelector('input[name="pickuptype"][value="โรงแรม/ที่พัก"]:checked');

            if (!isHotel) { clearFieldError(el, msgEl); return true; }

            const v = el.value.trim();
            if (!v) {
                showFieldError(el, msgEl, 'กรุณาระบุสถานที่รับ');
                return false;
            }
            if (!v.includes('เชียงใหม่')) {
                showFieldError(el, msgEl, 'บริการรับที่พักรองรับเฉพาะในเขตจังหวัดเชียงใหม่ กรุณาระบุคำว่า "เชียงใหม่"');
                return false;
            }
            clearFieldError(el, msgEl);
            return true;
        }

        /* ประเภทจุดรับ — ต้องเลือกอย่างใดอย่างหนึ่งเสมอ */
        function validatePickupType() {
            const group = document.getElementById('pickupRadioGroup');
            const msgEl = document.getElementById('pickupTypeError');
            if (!group) return true;

            const checked = document.querySelector('input[name="pickuptype"]:checked');
            if (!checked) {
                showFieldError(group, msgEl);
                return false;
            }
            clearFieldError(group, msgEl);
            return true;
        }

        let currentSeats = (typeof AVAILABLE_SEATS !== 'undefined' && AVAILABLE_SEATS !== null)
    ? AVAILABLE_SEATS : Infinity;

function validateTotalSeats() {
    const adultEl = document.getElementById('adult-input');
    const childEl = document.getElementById('children-input');
    if (!adultEl || !childEl || currentSeats === Infinity) return true;

    const total = (parseInt(adultEl.value, 10) || 0) + (parseInt(childEl.value, 10) || 0);
    const over = total > currentSeats;

    [adultEl, childEl].forEach(function (el) {
        const msgEl = findErrorMsgEl(el);
        if (msgEl && !msgEl.dataset.def) msgEl.dataset.def = msgEl.textContent;
        if (over) {
            showFieldError(el, msgEl, 'ที่นั่งเหลือเพียง ' + currentSeats + ' ที่ (รวมผู้ใหญ่และเด็ก)');
        } else {
            if (msgEl) msgEl.textContent = msgEl.dataset.def;
            if (el === adultEl) validateAdult(el); else validateChildren(el);
        }
    });
    return !over;
}
        /* ════ ผูก live-validation ตอนพิมพ์/เปลี่ยนค่า (ไม่ต้องรอกดบันทึก) ════ */
        document.addEventListener('input', function (e) {
    if (e.target.id === 'adult-input')    { validateAdult(e.target);    validateTotalSeats(); recalcTotals(); }
    if (e.target.id === 'children-input') { validateChildren(e.target); validateTotalSeats(); recalcTotals(); }
    if (e.target.classList.contains('guest-name-input')) validateGuestName(e.target);
    if (e.target.classList.contains('guest-idcard-input')) validateIdcard(e.target);
    if (e.target.id === 'pickuplocation-input') validatePickupLocation(e.target);
});

document.addEventListener('change', function (e) {
    if (e.target.id === 'tourdate-input') {
        validateTourDate(e.target);
        const opt = e.target.selectedOptions[0];
        const sid = opt && opt.dataset.scheduleid;
        if (sid && typeof TOUR_ID !== 'undefined') {
            fetch('/booking/tour/' + TOUR_ID + '/seats?scheduleid=' + encodeURIComponent(sid)
                  + '&bookingid=' + encodeURIComponent(BOOKING_ID))
                .then(function (r) { return r.json(); })
                .then(function (d) { currentSeats = d.availableSeats; validateTotalSeats(); })
                .catch(function () {});
        }
    }
    if (e.target.name === 'pickuptype') validatePickupType();
});
        /* ════ เช็คซ้ำอีกครั้งตอนกดบันทึก กันเคสที่ผู้ใช้ไม่เคยแตะช่องนั้นเลย ════ */
        document.getElementById('editForm')?.addEventListener('submit', function (e) {
            let ok = true;

            const tourdateEl = document.getElementById('tourdate-input');
            if (tourdateEl && !validateTourDate(tourdateEl)) ok = false;

            const adultEl = document.getElementById('adult-input');
            if (adultEl && !validateAdult(adultEl)) ok = false;

            const childrenEl = document.getElementById('children-input');
            if (childrenEl && !validateChildren(childrenEl)) ok = false;
            
             if (!validateTotalSeats()) ok = false;
             
            if (childrenEl && childrenEl.value.trim() === '') {
                childrenEl.value = '0';
            }

           
           
            document.querySelectorAll('.guest-name-input:not(:disabled)').forEach(function (el) {
                if (!validateGuestName(el)) ok = false;
            });

            document.querySelectorAll('.guest-idcard-input:not(:disabled)').forEach(function (el) {
                if (!validateIdcard(el)) ok = false;
            });

            if (!validatePickupType()) ok = false;

            const locEl = document.getElementById('pickuplocation-input');
            if (locEl && !validatePickupLocation(locEl)) ok = false;

            if (!ok) {
                e.preventDefault();
                const firstError = document.querySelector('.field-error, .radio-group.field-error');
                if (firstError) firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });

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
                if (lbl) lbl.textContent = ratingLabels[val] ? (ratingLabels[val] + ' — ' + val + '/5') : 'ให้คะแนนทริปนี้';
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

        /* ════ Cancel (Modal) ════ */
        function confirmCancel() {
            document.getElementById('cancelModal').classList.add('open');
        }

        function closeCancelModal() {
            document.getElementById('cancelModal').classList.remove('open');
        }
        function submitCancel() {
            document.getElementById('cancelForm').submit();
        }

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