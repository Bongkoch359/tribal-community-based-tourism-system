
(function () {
    if (window.showAlertModal) return; // กันประกาศซ้ำถ้าโหลดไฟล์นี้มากกว่า 1 ครั้ง

    const style = document.createElement('style');
    style.textContent = `
        .ui-modal-icon.error   { background: linear-gradient(135deg, #ba1a1a, #e14b4b); box-shadow: 0 8px 24px rgba(186,26,26,.35); }
        .ui-modal-icon.warning { background: linear-gradient(135deg, #ff8e4d, #c9600f); box-shadow: 0 8px 24px rgba(255,142,77,.35); }
        .ui-modal-icon.success { background: linear-gradient(135deg, var(--green-dark,#006e2f), var(--green-mid,#22c55e)); box-shadow: 0 8px 24px rgba(0,110,47,.35); }
        .modal-btn-secondary {
            font-size: 13px; font-family: 'Sarabun', sans-serif; padding: 10px 22px;
            background: transparent; color: var(--text-muted, #3d4a3d);
            border: 1px solid var(--border, #e4e0d4); border-radius: 8px;
            cursor: pointer; font-weight: 700; transition: background .15s;
        }
        .modal-btn-secondary:hover { background: #faf7ef; }
        .modal-btn-danger { background: #ba1a1a !important; }
        .modal-btn-danger:hover { background: #931414 !important; }
        .ui-modal-desc { white-space: pre-line; }
    `;
    document.head.appendChild(style);

    const ICON_CLASS = { error: 'fa-circle-exclamation', warning: 'fa-triangle-exclamation', success: 'fa-check' };
    const DEFAULT_TITLE = { error: 'เกิดข้อผิดพลาด', warning: 'แจ้งเตือน', success: 'สำเร็จ' };

    function buildBackdrop() {
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop';
        backdrop.innerHTML =
            '<div class="modal-box">' +
            '  <div class="checkmark-wrap ui-modal-icon">' +
            '    <i class="fas checkmark-icon ui-modal-icon-glyph"></i>' +
            '  </div>' +
            '  <div class="modal-title ui-modal-title"></div>' +
            '  <div class="modal-desc ui-modal-desc"></div>' +
            '  <div class="modal-btn-row ui-modal-btn-row"></div>' +
            '</div>';
        document.body.appendChild(backdrop);
        // trigger reflow แล้วค่อยเปิด class .show เพื่อให้ transition เล่น
        requestAnimationFrame(() => requestAnimationFrame(() => backdrop.classList.add('show')));
        return backdrop;
    }

    function closeBackdrop(backdrop) {
        backdrop.classList.remove('show');
        setTimeout(() => backdrop.remove(), 300);
    }

    /**
     * แสดง modal แจ้งเตือนแทน alert()
     * @param {string} message ข้อความที่จะแสดง
     * @param {{type?: 'error'|'warning'|'success', title?: string}} [opts]
     * @returns {Promise<void>} resolve เมื่อผู้ใช้กดตกลง
     */
    function showAlertModal(message, opts = {}) {
        const type = opts.type || 'success';
        const title = opts.title || DEFAULT_TITLE[type];

        // แจ้งเตือนสำเร็จ: ไม่มีปุ่มตกลง ปิดอัตโนมัติหลังแสดง 1.5 วินาที
        if (type === 'success') {
            return new Promise((resolve) => {
                const backdrop = buildBackdrop();
                backdrop.querySelector('.ui-modal-icon').classList.add(type);
                backdrop.querySelector('.ui-modal-icon-glyph').classList.add(ICON_CLASS[type]);
                backdrop.querySelector('.ui-modal-title').textContent = title;
                backdrop.querySelector('.ui-modal-desc').textContent = message;
                // ไม่แสดงปุ่มตกลงสำหรับแจ้งเตือนสำเร็จ

                setTimeout(() => {
                    closeBackdrop(backdrop);
                    resolve();
                }, 1500);
            });
        }

        return new Promise((resolve) => {
            const backdrop = buildBackdrop();
            backdrop.querySelector('.ui-modal-icon').classList.add(type);
            backdrop.querySelector('.ui-modal-icon-glyph').classList.add(ICON_CLASS[type]);
            backdrop.querySelector('.ui-modal-title').textContent = title;
            backdrop.querySelector('.ui-modal-desc').textContent = message;

            const okBtn = document.createElement('button');
            okBtn.type = 'button';
            okBtn.className = 'modal-btn-primary';
            okBtn.innerHTML = '<i class="fas fa-check"></i> ตกลง';
            okBtn.onclick = () => { closeBackdrop(backdrop); resolve(); };
            backdrop.querySelector('.ui-modal-btn-row').appendChild(okBtn);
            okBtn.focus();
        });
    }

    /**
     * แสดง modal ยืนยันแทน confirm()
     * @param {string} message ข้อความที่จะแสดง
     * @param {{title?: string, confirmText?: string, cancelText?: string, danger?: boolean}} [opts]
     * @returns {Promise<boolean>} resolve(true) ถ้ากดยืนยัน, resolve(false) ถ้ายกเลิก
     */
    function showConfirmModal(message, opts = {}) {
        const danger = !!opts.danger;
        const title = opts.title || (danger ? 'ยืนยันการลบ' : 'ยืนยันการทำรายการ');
        const confirmText = opts.confirmText || 'ยืนยัน';
        const cancelText = opts.cancelText || 'ยกเลิก';

        return new Promise((resolve) => {
            const backdrop = buildBackdrop();
            backdrop.querySelector('.ui-modal-icon').classList.add(danger ? 'warning' : 'success');
            backdrop.querySelector('.ui-modal-icon-glyph').classList.add(danger ? ICON_CLASS.warning : ICON_CLASS.success);
            backdrop.querySelector('.ui-modal-title').textContent = title;
            backdrop.querySelector('.ui-modal-desc').textContent = message;

            const btnRow = backdrop.querySelector('.ui-modal-btn-row');

            const cancelBtn = document.createElement('button');
            cancelBtn.type = 'button';
            cancelBtn.className = 'modal-btn-secondary';
            cancelBtn.textContent = cancelText;
            cancelBtn.onclick = () => { closeBackdrop(backdrop); resolve(false); };

            const confirmBtn = document.createElement('button');
            confirmBtn.type = 'button';
            confirmBtn.className = 'modal-btn-primary' + (danger ? ' modal-btn-danger' : '');
            confirmBtn.innerHTML = '<i class="fas fa-check"></i> ' + confirmText;
            confirmBtn.onclick = () => { closeBackdrop(backdrop); resolve(true); };

            btnRow.appendChild(cancelBtn);
            btnRow.appendChild(confirmBtn);
            confirmBtn.focus();
        });
    }

    window.showAlertModal = showAlertModal;
    window.showConfirmModal = showConfirmModal;
})();

// ✅ ปรับให้ตรงกับ requirement เดียวกับฟอร์มเพิ่มทัวร์ (สูงสุด 5 รูป)
//    เดิมฟอร์มนี้ตั้งไว้ 10 รูป ทำให้ไม่ตรงกับฟอร์มเพิ่มทัวร์ที่จำกัดไว้ 5 รูป
const MAX_IMAGES = 5;
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
// ✅ แก้บั๊ก: เดิม slice() ตัดไฟล์ตามโควตาก่อนเช็คชนิดไฟล์ ทำให้ถ้ามีไฟล์ผิดชนิดปนมาก่อน
//    รูปที่ถูกต้องแต่อยู่ลำดับหลังอาจถูกตัดทิ้งทั้งที่ยังมีโควตาว่างพอ
//    แก้โดยกรองชนิดไฟล์ที่ถูกต้องก่อน แล้วค่อย slice ตามโควตาที่เหลือ
function addFiles(files) {
    const allowed = MAX_IMAGES - imageDataList.length;
    const validFiles = files
        .filter(file => file.type.match(/^image\/(jpeg|png|webp)$/))
        .slice(0, allowed);

    // แจ้งผู้ใช้เมื่อมีไฟล์ถูกตัดทิ้ง (ผิดชนิด หรือเกินโควตา)
    if (validFiles.length < files.length) {
        const invalidCount = files.filter(f => !f.type.match(/^image\/(jpeg|png|webp)$/)).length;
        if (invalidCount > 0) {
            showAlertModal('มีไฟล์บางไฟล์ไม่ใช่รูปภาพชนิด jpg/png/webp จึงถูกข้ามไป', { type: 'warning' });
        } else if (allowed <= 0 || files.length > allowed) {
            showAlertModal(`เลือกรูปได้สูงสุด ${MAX_IMAGES} รูป มีบางไฟล์ถูกข้ามไปเนื่องจากเกินจำนวนที่กำหนด`, { type: 'warning' });
        }
    }

    if (validFiles.length === 0) return;

    let loaded = 0;
    validFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = e => {
            imageDataList.push({ src: e.target.result, base64: e.target.result, name: file.name });
            loaded++;
            if (loaded === validFiles.length) renderGrid();
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

// ✅ แก้บั๊ก: เดิมเช็คแค่กรณี primaryIndex เกินขอบเขตหลังลบ (>= length)
//    แต่ถ้าลบรูปที่ index ก่อนหน้ารูปหลัก จะทำให้ primaryIndex ชี้ไปผิดรูป (เลื่อนตำแหน่งไม่ทัน)
//    แก้โดยปรับ primaryIndex ตามตำแหน่งที่ถูกลบ (เหมือนกับที่แก้ในฟอร์มเพิ่มทัวร์)
function removeImage(index) {
    imageDataList.splice(index, 1);
    if (index < primaryIndex) {
        primaryIndex--;
    } else if (index === primaryIndex) {
        primaryIndex = 0;
    }
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

/* ═══════════════════════════════════════════
   จุดรับ / นัดพบ — เปิด/ปิดช่องกรอกรายละเอียดตาม checkbox
═══════════════════════════════════════════ */
const allowMeetingPointChk = document.getElementById('allowMeetingPointChk');
const meetingPointDetailGroup = document.getElementById('meetingPointDetailGroup');
const meetingPointDetailInput = document.getElementById('meetingPointDetail');
const allowHotelPickupChk = document.getElementById('allowHotelPickupChk');
const hotelPickupAreaGroup = document.getElementById('hotelPickupAreaGroup');
const hotelPickupAreaInput = document.getElementById('hotelPickupArea');
const pickupOptionErr = document.getElementById('err-pickupOption');

function applyPickupRules() {
    meetingPointDetailGroup.style.display = allowMeetingPointChk.checked ? 'block' : 'none';
    hotelPickupAreaGroup.style.display = allowHotelPickupChk.checked ? 'block' : 'none';
    if (pickupOptionErr) pickupOptionErr.style.display = 'none';

    // ✅ เพิ่มใหม่: สร้างแผนที่ตอนเปิดใช้งาน checkbox (Leaflet ไม่ต้องรอ API โหลด)
    if (allowMeetingPointChk.checked) initMeetingMap();
    if (allowHotelPickupChk.checked) initHotelMap();
}
allowMeetingPointChk.addEventListener('change', applyPickupRules);
allowHotelPickupChk.addEventListener('change', applyPickupRules);
window.addEventListener('DOMContentLoaded', applyPickupRules);

/* ═══════════════════════════════════════════
   LEAFLET + OPENSTREETMAP — ช่วยหาที่อยู่ (จุดรวมพล / เขตรับที่โรงแรม)
   ฟรี ไม่ต้องมี API Key ไม่เก็บ lat/lng ลง backend
   ใช้ Nominatim (OSM) สำหรับค้นหาสถานที่ + reverse geocode
═══════════════════════════════════════════ */
const DEFAULT_MAP_CENTER = [18.7883, 98.9853]; // ศูนย์กลางเชียงใหม่
const NOMINATIM_BASE = 'https://nominatim.openstreetmap.org';

let meetingMap, meetingMarker;
let hotelMap, hotelMarker;

function createLeafletMap(divId) {
    const map = L.map(divId, { center: DEFAULT_MAP_CENTER, zoom: 13 });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);
    return map;
}

// ✅ ถ้าฟอร์มมีที่อยู่เดิม (โหมดแก้ไขทัวร์) ให้พยายาม geocode หาพิกัดมาปักหมุดตั้งต้นให้เลย
function geocodeAddressToLatLng(address, callback) {
    if (!address || address.trim() === '') { callback(null); return; }
    const params = new URLSearchParams({
        format: 'jsonv2',
        q: address,
        countrycodes: 'th',
        'accept-language': 'th',
        limit: '1'
    });
    fetch(`${NOMINATIM_BASE}/search?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            if (data && data[0]) {
                callback([parseFloat(data[0].lat), parseFloat(data[0].lon)]);
            } else {
                callback(null);
            }
        })
        .catch(() => callback(null));
}

function reverseGeocode(lat, lng, callback) {
    fetch(`${NOMINATIM_BASE}/reverse?format=jsonv2&lat=${lat}&lon=${lng}&accept-language=th`)
        .then(res => res.json())
        .then(data => {
            if (data && data.display_name) callback(data.display_name);
        })
        .catch(() => { });
}

function searchPlaces(query, callback) {
    if (!query || query.trim().length < 3) { callback([]); return; }
    const params = new URLSearchParams({
        format: 'jsonv2',
        q: query,
        countrycodes: 'th',
        'accept-language': 'th',
        limit: '5'
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
                if (results.length === 0) {
                    suggestBoxEl.style.display = 'none';
                    return;
                }
                suggestBoxEl.innerHTML = '';
                results.forEach(place => {
                    const item = document.createElement('div');
                    item.className = 'search-suggest-item';
                    item.textContent = place.display_name;
                    item.addEventListener('click', () => {
                        inputEl.value = place.display_name;
                        suggestBoxEl.style.display = 'none';
                        onSelect(parseFloat(place.lat), parseFloat(place.lon));
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

/* ── จุดรวมพล ── */
function initMeetingMap() {
    if (meetingMap) return; // สร้างครั้งเดียวพอ

    meetingMap = createLeafletMap('meetingPointMap');
    meetingMarker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(meetingMap);

    // ✅ โหมดแก้ไข: ถ้ามีที่อยู่เดิมอยู่แล้ว ให้ลอง geocode หาพิกัดมาปักหมุดตั้งต้นให้ตรงจุดจริง
    if (meetingPointDetailInput.value.trim() !== '') {
        geocodeAddressToLatLng(meetingPointDetailInput.value, (latlng) => {
            if (latlng) {
                meetingMap.setView(latlng, 16);
                meetingMarker.setLatLng(latlng);
            }
        });
    }

    meetingMarker.on('dragend', () => {
        const pos = meetingMarker.getLatLng();
        reverseGeocode(pos.lat, pos.lng, (address) => {
            meetingPointDetailInput.value = address;
        });
    });

    meetingMap.on('click', (e) => {
        meetingMarker.setLatLng(e.latlng);
        reverseGeocode(e.latlng.lat, e.latlng.lng, (address) => {
            meetingPointDetailInput.value = address;
        });
    });

    setTimeout(() => meetingMap.invalidateSize(), 200); // กัน bug แผนที่เบี้ยวตอนเพิ่งโผล่จาก display:none

    attachPlaceSearch(
        meetingPointDetailInput,
        document.getElementById('meetingPointSuggest'),
        (lat, lng) => {
            meetingMap.setView([lat, lng], 16);
            meetingMarker.setLatLng([lat, lng]);
        },
        { id: null }
    );
}

/* ── เขตรับที่โรงแรม ── */
function initHotelMap() {
    if (hotelMap) return;

    hotelMap = createLeafletMap('hotelPickupMap');
    hotelMap.setZoom(12);
    hotelMarker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(hotelMap);

    if (hotelPickupAreaInput.value.trim() !== '') {
        geocodeAddressToLatLng(hotelPickupAreaInput.value, (latlng) => {
            if (latlng) {
                hotelMap.setView(latlng, 14);
                hotelMarker.setLatLng(latlng);
            }
        });
    }

    hotelMarker.on('dragend', () => {
        const pos = hotelMarker.getLatLng();
        reverseGeocode(pos.lat, pos.lng, (address) => {
            hotelPickupAreaInput.value = address;
        });
    });

    hotelMap.on('click', (e) => {
        hotelMarker.setLatLng(e.latlng);
        reverseGeocode(e.latlng.lat, e.latlng.lng, (address) => {
            hotelPickupAreaInput.value = address;
        });
    });

    setTimeout(() => hotelMap.invalidateSize(), 200);

    attachPlaceSearch(
        hotelPickupAreaInput,
        document.getElementById('hotelPickupSuggest'),
        (lat, lng) => {
            hotelMap.setView([lat, lng], 14);
            hotelMarker.setLatLng([lat, lng]);
        },
        { id: null }
    );
}

// ตั้งค่าเริ่มต้นจากข้อมูลเดิมใน DB ตอนโหลดหน้า
// ✅ tourtype เป็น entity แล้ว ต้องอ่านชื่อผ่าน .typename และกัน null (ทัวร์ที่ยังไม่ผูกประเภท)
(function initTourType() {
    const savedType = (typeof tourSavedType !== 'undefined') ? tourSavedType : '';
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

/* ═══════════════════════════════════════════
   SUCCESS MODAL + REDIRECT
═══════════════════════════════════════════ */
function showSuccessModal(redirectUrl) {
    const modal = document.getElementById('successModal');
    const fill = document.getElementById('progressFill');

    modal.classList.add('show');

    fill.style.animation = 'none';
    fill.offsetHeight; // trigger reflow
    fill.style.animation = 'progress-drain 2s linear forwards';

    setTimeout(() => {
        window.location.href = redirectUrl || '/manager/tours';
    }, 2000);
}

// ── Validate & Submit ผ่าน Fetch ──
document.getElementById('editForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const min = parseInt(document.querySelector('[name="minSeatstour"]').value);
    const max = parseInt(document.querySelector('[name="maxSeatstour"]').value);
    if (min > max) {
        showAlertModal('จำนวนที่นั่งขั้นต่ำต้องน้อยกว่าหรือเท่ากับสูงสุด', { type: 'error' });
        return;
    }

    if (tourtypeSelect.value === '') {
        showAlertModal('กรุณาเลือกประเภททัวร์', { type: 'error' });
        return;
    }
    if (tourtypeSelect.value === '__custom__' && tourtypeCustom.value.trim() === '') {
        showAlertModal('กรุณาระบุชื่อประเภททัวร์', { type: 'error' });
        return;
    }

    const d = parseInt(daysInput.value);
    const n = parseInt(nightsInput.value);
    if (tourtypeSelect.value === 'ทัวร์รายวัน') {
        if (d !== 1 || n !== 0) {
            showAlertModal('ทัวร์รายวันต้องเป็น 1 วัน 0 คืน', { type: 'error' });
            return;
        }
    } else {
        if (isNaN(d) || d <= 1) {
            showAlertModal('ทัวร์หลายวันต้องมากกว่า 1 วัน', { type: 'error' });
            return;
        }
        if (isNaN(n) || n >= d) {
            showAlertModal('จำนวนคืนต้องน้อยกว่าจำนวนวัน', { type: 'error' });
            return;
        }
    }

    if (!allowMeetingPointChk.checked && !allowHotelPickupChk.checked) {
        if (pickupOptionErr) pickupOptionErr.style.display = 'block';
        showAlertModal('กรุณาเปิดอย่างน้อย 1 ช่องทางรับ-ส่ง (จุดรวมพล หรือ รับที่โรงแรม)', { type: 'error' });
        return;
    }
    if (allowMeetingPointChk.checked && meetingPointDetailInput.value.trim() === '') {
        showAlertModal('กรุณาระบุสถานที่จุดรวมพล', { type: 'error' });
        return;
    }
    if (allowHotelPickupChk.checked && hotelPickupAreaInput.value.trim() === '') {
        showAlertModal('กรุณาระบุเขตพื้นที่ที่รับได้', { type: 'error' });
        return;
    }
    const meetingTimeInput = document.getElementById('meetingTime');
    if (!meetingTimeInput || meetingTimeInput.value.trim() === '') {
        showAlertModal('กรุณาระบุเวลานัดพบ', { type: 'error' });
        return;
    }

    // อัปเดตข้อมูลภาพล่าสุด
    packImages();

    const formData = new FormData(this);
    const finalTourType = tourtypeSelect.value === '__custom__'
        ? tourtypeCustom.value.trim()
        : tourtypeSelect.value;
    formData.set('tourtype', finalTourType);

    fetch(this.action, {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(() => {
                throw new Error('เกิดข้อผิดพลาดจากเซิร์ฟเวอร์ (status ' + response.status + ')');
            });
        }
        return response.url;
    })
    .then((redirectUrl) => {
        showSuccessModal(redirectUrl);
    })
    .catch(err => {
        console.error('บันทึกการแก้ไขไม่สำเร็จ:', err);
        showAlertModal('เกิดข้อผิดพลาด: ' + err.message, { type: 'error' });
    });
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
        console.error(' ปฏิทินรอบทัวร์: หา element ไม่เจอ ->', missing);
    }
    console.log(' window.scheduleData ตอนโหลดหน้า:', window.scheduleData);
    if (!Array.isArray(window.scheduleData)) {
        console.error(' window.scheduleData ไม่ใช่ array! อาจเกิดจาก syntax error ใน script ก่อนหน้า (ตัว th:inline="javascript")');
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

    function statusClass(status, dateISO) {
        // วันที่ผ่านไปแล้ว (ก่อนวันนี้) ให้ถือเป็น "ปิด/ผ่านไปแล้ว" เสมอ ไม่ว่า status ใน DB จะเป็นอะไร
        if (dateISO < todayISO) return 'st-past';

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
            cell.className = 'cal-cell' + (schedule ? ' ' + statusClass(schedule.status, dateISO) : (dateISO < todayISO ? ' st-past' : ''));
            if (dateISO === todayISO) cell.classList.add('cal-today');
            cell.dataset.date = dateISO;

            let sub = '';
            // ให้แสดงยอดที่นั่งเฉพาะ "วันเริ่มต้นรอบทัวร์" (opendate) เท่านั้น
            if (schedule && schedule.opendate === dateISO) {
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
            await showAlertModal(result.message || 'เกิดข้อผิดพลาด กรุณาลองใหม่', { type: 'error' });
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
            await showAlertModal('ทุกวันในช่วงนี้มีรอบทัวร์อยู่แล้ว', { type: 'warning' });
            return;
        }
        setToolbarBusy(true);
        let ok = 0, fail = 0;
        for (const item of toCreate) {
            const success = await createScheduleRequest(item.opendate, item.enddate);
            if (success) ok++; else fail++;
        }
        setToolbarBusy(false);
        await showAlertModal(`สร้างรอบทัวร์สำเร็จ ${ok} วัน` + (fail > 0 ? ` (ล้มเหลว ${fail} วัน กรุณาตรวจสอบอีกครั้ง)` : ''), { type: 'success' });
        await refreshScheduleData();
    });

    // ── ปุ่ม: ลบรอบทัวร์ทั้งหมดในช่วงที่เลือก (ข้ามรอบที่มีคนจองแล้ว) ──
    calDeleteBtn.addEventListener('click', async () => {
        const inRange = window.scheduleData.filter(s => s.opendate >= selStart && s.opendate <= selEnd);
        if (inRange.length === 0) { await showAlertModal('ไม่มีรอบทัวร์ในช่วงที่เลือก', { type: 'warning' }); return; }

        const deletable = inRange.filter(s => (s.booked || 0) === 0);
        const blocked = inRange.length - deletable.length;
        if (deletable.length === 0) {
            await showAlertModal('รอบทัวร์ทั้งหมดในช่วงนี้มีคนจองแล้ว ไม่สามารถลบได้', { type: 'warning' });
            return;
        }
        const confirmed = await showConfirmModal(
            `ยืนยันลบรอบทัวร์ ${deletable.length} รอบในช่วงนี้?` +
            (blocked > 0 ? `\n(ข้าม ${blocked} รอบที่มีคนจองแล้ว ไม่สามารถลบได้)` : ''),
            { danger: true, confirmText: 'ลบรอบทัวร์' }
        );
        if (!confirmed) return;

        setToolbarBusy(true);
        let ok = 0, fail = 0;
        for (const s of deletable) {
            const success = await deleteScheduleRequest(s.scheduleid);
            if (success) ok++; else fail++;
        }
        setToolbarBusy(false);
        await showAlertModal(`ลบสำเร็จ ${ok} รอบ` + (fail > 0 ? ` (ล้มเหลว ${fail} รอบ)` : '') +
            (blocked > 0 ? ` · ข้าม ${blocked} รอบที่มีคนจองแล้ว` : ''), { type: 'success' });
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