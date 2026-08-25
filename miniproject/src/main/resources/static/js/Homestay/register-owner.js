// ============================================================
//  register-owner.js
// ============================================================

'use strict';

const homestayImages = {};

// ============================================================
//  LEAFLET + OPENSTREETMAP — ช่วยหาที่อยู่โฮมสเตย์ (แบบเดียวกับหน้าเพิ่มทัวร์)
//  ฟรี ไม่ต้องมี API Key ไม่เก็บ lat/lng ลง backend
//  ใช้ Nominatim (OSM) สำหรับค้นหาสถานที่ + reverse geocode
// ============================================================
const DEFAULT_MAP_CENTER = [18.7883, 98.9853]; // ศูนย์กลางเชียงใหม่ (ปรับตามพื้นที่ให้บริการจริงได้)
const NOMINATIM_BASE = 'https://nominatim.openstreetmap.org';
const homestayMaps = {}; // { idx: { map, marker } }

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
        .then(data => {
            if (data && data.display_name) callback(data.display_name);
        })
        .catch(() => {});
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

/* ── ผูก search box + suggestion dropdown ให้แต่ละช่อง input ── */
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
                        onSelect(parseFloat(place.lat), parseFloat(place.lon), place.display_name);
                    });
                    suggestBoxEl.appendChild(item);
                });
                suggestBoxEl.style.display = 'block';
            });
        }, 500); // debounce 500ms ตามนโยบายการใช้งาน Nominatim
    });

    document.addEventListener('click', (e) => {
        if (!inputEl.contains(e.target) && !suggestBoxEl.contains(e.target)) {
            suggestBoxEl.style.display = 'none';
        }
    });
}

/* ── หาตำแหน่งปัจจุบันของผู้ใช้ ── */
function getCurrentPositionOrDefault(onLocated, onFallback) {
    if (!navigator.geolocation) { onFallback(); return; }
    navigator.geolocation.getCurrentPosition(
        (pos) => onLocated([pos.coords.latitude, pos.coords.longitude]),
        () => onFallback(),
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
    );
}

/* ── สร้าง/แสดงแผนที่สำหรับโฮมสเตย์แต่ละแห่ง (เรียกซ้ำได้ปลอดภัย — ครั้งต่อไปแค่ invalidateSize) ── */
function initHomestayMap(idx) {
    const mapDivId = `hs_map_${idx}`;
    const mapDiv = document.getElementById(mapDivId);
    if (!mapDiv) return;

    if (homestayMaps[idx]) {
        setTimeout(() => homestayMaps[idx].map.invalidateSize(), 100);
        return;
    }

    const addressInput = document.getElementById(`hs_address_${idx}`);
    const map = createLeafletMap(mapDivId);
    const marker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(map);
    homestayMaps[idx] = { map, marker };

    function fillAddress(address) {
        addressInput.value = address;
        if (addressInput.classList.contains('is-invalid')) validateHsAddress(idx);
    }

    marker.on('dragend', () => {
        const pos = marker.getLatLng();
        reverseGeocode(pos.lat, pos.lng, fillAddress);
    });

    map.on('click', (e) => {
        marker.setLatLng(e.latlng);
        reverseGeocode(e.latlng.lat, e.latlng.lng, fillAddress);
    });

    setTimeout(() => map.invalidateSize(), 200);

    // ✅ ปักหมุดที่ตำแหน่งปัจจุบันของผู้ใช้อัตโนมัติ ถ้ายังไม่มีที่อยู่กรอกไว้
    if (!addressInput.value.trim()) {
        getCurrentPositionOrDefault(
            ([lat, lng]) => {
                map.setView([lat, lng], 16);
                marker.setLatLng([lat, lng]);
                reverseGeocode(lat, lng, fillAddress);
            },
            () => { /* ใช้ DEFAULT_MAP_CENTER ตามเดิม */ }
        );
    }

    attachPlaceSearch(
        addressInput,
        document.getElementById(`hs_addressSuggest_${idx}`),
        (lat, lng) => {
            map.setView([lat, lng], 16);
            marker.setLatLng([lat, lng]);
            if (addressInput.classList.contains('is-invalid')) validateHsAddress(idx);
        },
        { id: null }
    );
}

// หมายเหตุ: สไตล์ของกล่องแผนที่ (.hs-map-box, .hs-search-input-wrap ฯลฯ)
// อยู่ใน register-owner.css แล้ว (รวมถึงตัวแก้ปัญหาแผนที่ลอยทับ navbar ตอนเลื่อนจอ
// ด้วย isolation/z-index) จึงไม่ต้อง inject style ผ่าน JS อีกต่อไป

// ============================================================
//  HELPER
// ============================================================
function showError(input, spanId, msg) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
    const span = document.getElementById(spanId);
    if (span) { span.textContent = '⚠ ' + msg; span.style.display = 'block'; }
}
function showValid(input, spanId) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
    const span = document.getElementById(spanId);
    if (span) { span.style.display = 'none'; }
}
function showSpanError(spanId, msg) {
    const span = document.getElementById(spanId);
    if (span) { span.textContent = '⚠ ' + msg; span.style.display = 'block'; }
}
function hideSpan(spanId) {
    const span = document.getElementById(spanId);
    if (span) { span.style.display = 'none'; }
}

// ============================================================
//  STEP 1 — VALIDATORS
// ============================================================
function validateFirstname() {
    const input = document.getElementById('firstname');
    const val = input.value.trim();
    if (val === '') { showError(input, 'firstnameError', 'กรุณากรอกชื่อ'); return false; }
    if (!/^[ก-๙a-zA-Z]+$/.test(val)) { showError(input, 'firstnameError', 'ชื่อใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษเท่านั้น'); return false; }
    if (val.length < 2 || val.length > 20) { showError(input, 'firstnameError', 'ชื่อต้องมีความยาว 2–20 ตัวอักษร'); return false; }
    showValid(input, 'firstnameError'); return true;
}
function validateLastname() {
    const input = document.getElementById('lastname');
    const val = input.value.trim();
    if (val === '') { showError(input, 'lastnameError', 'กรุณากรอกนามสกุล'); return false; }
    if (!/^[ก-๙a-zA-Z]+$/.test(val)) { showError(input, 'lastnameError', 'นามสกุลใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษเท่านั้น'); return false; }
    if (val.length < 2 || val.length > 20) { showError(input, 'lastnameError', 'นามสกุลต้องมีความยาว 2–20 ตัวอักษร'); return false; }
    showValid(input, 'lastnameError'); return true;
}
// เปลี่ยน validateEmail เป็น async และเพิ่มการเช็คซ้ำกับ backend
async function validateEmail() {
    const input = document.getElementById('email');
    const val = input.value;
    if (val === '') { showError(input, 'emailError', 'อีเมลต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'emailError', 'อีเมลต้องไม่มีช่องว่างระหว่างตัวอักษร'); return false; }
    if (!/^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/.test(val)) { showError(input, 'emailError', 'รูปแบบอีเมลไม่ถูกต้อง (เช่น example@email.com)'); return false; }
    const localPart = val.split('@')[0];
    if (localPart.length < 5 || localPart.length > 20) { showError(input, 'emailError', 'อีเมล (ก่อน @) ต้องมีความยาว 5–20 ตัวอักษร'); return false; }

    // เช็คอีเมลซ้ำกับระบบ
    input.classList.add('is-checking'); // ใส่ style spinner เองได้ถ้าต้องการ
    try {
        const res = await fetch(`/owner/check-email?email=${encodeURIComponent(val)}`);
        const data = await res.json();
        if (data.exists) {
            showError(input, 'emailError', 'อีเมลนี้มีผู้ใช้งานแล้ว กรุณาใช้อีเมลอื่น');
            return false;
        }
    } catch (err) {
        console.error('ตรวจสอบอีเมลไม่สำเร็จ', err);
        // เช็คไม่ได้ (เช่น เน็ตหลุด) ปล่อยผ่านไปก่อน แล้วให้ backend เช็คซ้ำตอน submit จริง
    } finally {
        input.classList.remove('is-checking');
    }

    showValid(input, 'emailError');
    return true;
}
function validatePhone() {
    const input = document.getElementById('phone');
    const val = input.value;
    if (val === '') { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องไม่มีช่องว่างระหว่างตัวเลข'); return false; }
    if (!/^\d+$/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องเป็นตัวเลข [ 0-9 ] เท่านั้น'); return false; }
    if (val.length !== 10) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องมี 10 หลักเท่านั้น'); return false; }
    if (!/^0[689]/.test(val)) { showError(input, 'phoneError', 'หมายเลขโทรศัพท์ต้องขึ้นต้นด้วย 06, 08 หรือ 09 เท่านั้น'); return false; }
    showValid(input, 'phoneError'); return true;
}
function validatePassword() {
    const input = document.getElementById('password');
    const val = input.value;
    if (val === '') { showError(input, 'passwordError', 'รหัสผ่านต้องไม่เป็นค่าว่าง'); return false; }
    if (/\s/.test(val)) { showError(input, 'passwordError', 'รหัสผ่านต้องไม่มีช่องว่าง'); return false; }
    if (!/^[a-zA-Z0-9!#_.]+$/.test(val)) { showError(input, 'passwordError', 'รหัสผ่านใช้ได้เฉพาะตัวอักษร ตัวเลข และ [ ! # _ . ]'); return false; }
    if (val.length < 8 || val.length > 16) { showError(input, 'passwordError', 'รหัสผ่านต้องมีความยาว 8–16 ตัวอักษร'); return false; }
    showValid(input, 'passwordError'); return true;
}
function validateConfirmPassword() {
    const input = document.getElementById('confirmPassword');
    const val = input.value;
    const pw  = document.getElementById('password').value;
    if (val === '') { showError(input, 'confirmPasswordError', 'กรุณายืนยันรหัสผ่าน'); return false; }
    if (val !== pw) { showError(input, 'confirmPasswordError', 'รหัสผ่านไม่ตรงกัน กรุณากรอกใหม่'); return false; }
    showValid(input, 'confirmPasswordError'); return true;
}
function validateAgree() {
    const cb = document.getElementById('agree');
    if (!cb.checked) { showSpanError('agreeError', 'กรุณายอมรับเงื่อนไขการใช้งาน'); return false; }
    hideSpan('agreeError'); return true;
}

// หมายเหตุ: validateStep1 (เวอร์ชัน async ที่รอเช็คอีเมลซ้ำด้วย) ถูกย้ายไปประกาศ
// ที่เดียวในส่วน "STEP 1 — LIVE VALIDATION" ท้ายไฟล์ เพื่อไม่ให้มีการประกาศซ้ำชื่อ

// ============================================================
//  TERMS & CONDITIONS MODAL
// ============================================================
(function () {
    const termsLink   = document.getElementById('termsLink');
    const termsBody   = document.getElementById('termsBody');
    const acceptBtn   = document.getElementById('acceptTermsBtn');
    const agreeCheckbox = document.getElementById('agree');

    if (!termsLink || !termsBody || !acceptBtn || !agreeCheckbox) return;

    // กันไม่ให้ href="#" เลื่อนหน้า และกัน event ทะลุไปโดน label/checkbox
    termsLink.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
    });

    // บังคับเลื่อนอ่านจนสุดก่อนถึงจะกดปุ่ม "ยอมรับเงื่อนไข" ได้
    termsBody.addEventListener('scroll', () => {
        const scrolledToBottom =
            termsBody.scrollTop + termsBody.clientHeight >= termsBody.scrollHeight - 5;
        if (scrolledToBottom) acceptBtn.disabled = false;
    });

    // เผื่อเนื้อหาสั้นจนไม่มี scrollbar เลย -> ปลดล็อกปุ่มให้อัตโนมัติ
    document.getElementById('termsModal')?.addEventListener('shown.bs.modal', () => {
        if (termsBody.scrollHeight <= termsBody.clientHeight) acceptBtn.disabled = false;
    });

    // กดยอมรับใน modal -> ติ๊ก checkbox ให้ และปลดล็อกให้กดเองได้ต่อไป
    acceptBtn.addEventListener('click', () => {
        agreeCheckbox.checked = true;
        agreeCheckbox.disabled = false;
        validateAgree(); // เคลียร์ error message ถ้ามี
    });
})();

// ============================================================
//  STEP 2 — DYNAMIC HOMESTAY + IMAGE UPLOAD
// ============================================================
let homestayCount = 0;

function addHomestay() {
    homestayCount++;
    const idx = homestayCount;
    homestayImages[idx] = [];

    const list  = document.getElementById('homestay-list');
    const block = document.createElement('div');
    block.className = 'homestay-block border rounded p-3 mb-3';
    block.id = `homestay-block-${idx}`;
    block.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-2">
            <strong class="text-success"><i class="fas fa-house me-1"></i> โฮมสเตย์ที่ ${idx}</strong>
            ${idx > 1 ? `<button type="button" class="btn btn-sm btn-outline-danger"
                onclick="removeHomestay(${idx})">
                <i class="fas fa-trash"></i> ลบ</button>` : ''}
        </div>

        <div class="mb-2">
            <label class="form-label required-label">ชื่อโฮมสเตย์</label>
            <input type="text" class="form-control" id="hs_name_${idx}" placeholder="ชื่อโฮมสเตย์">
            <span class="field-error" id="hs_nameError_${idx}"></span>
        </div>

        <div class="mb-2">
            <label class="form-label required-label">ที่อยู่โฮมสเตย์</label>
            <div class="hs-search-input-wrap">
                <input type="text" class="form-control" id="hs_address_${idx}" autocomplete="off"
                       placeholder="บ้านเลขที่ / หมู่ / ตำบล / อำเภอ">
                <div class="hs-search-suggest-box" id="hs_addressSuggest_${idx}"></div>
            </div>
            <span class="field-error" id="hs_addressError_${idx}"></span>
            <div class="hs-map-box" id="hs_map_${idx}"></div>
            <small style="color:#888; font-size:0.78rem;">พิมพ์ค้นหาชื่อสถานที่ในช่องด้านบน
                หรือคลิก/ลากหมุดบนแผนที่เพื่อให้ระบบเติมที่อยู่ให้อัตโนมัติ</small>
        </div>

        <div class="mb-2">
            <label class="form-label required-label">รายละเอียดโฮมสเตย์</label>
            <textarea class="form-control" id="hs_desc_${idx}" rows="3"
                      placeholder="อธิบายจุดเด่น บรรยากาศ หรือสิ่งอำนวยความสะดวก..."></textarea>
            <span class="field-error" id="hs_descError_${idx}"></span>
        </div>

        <div class="mb-2">
            <label class="form-label required-label">รูปภาพโฮมสเตย์</label>
            <div class="hs-upload-zone" id="hs_zone_${idx}"
                 onclick="document.getElementById('hs_img_${idx}').click()"
                 ondragover="event.preventDefault(); this.style.borderColor='#2d6a2d'"
                 ondragleave="this.style.borderColor='#b5d5b7'"
                 ondrop="handleHsDrop(event, ${idx})">
                <input type="file" id="hs_img_${idx}" accept="image/jpeg,image/png,image/webp"
                       multiple style="display:none"
                       onchange="handleHsImages(${idx}, this.files); this.value=''">
                <i class="fas fa-cloud-arrow-up" style="font-size:1.8rem; color:#ccc; display:block; margin-bottom:6px;"></i>
                <p style="color:#aaa; font-size:0.85rem; margin:0;">คลิกหรือลากรูปมาวางที่นี่</p>
                <small style="color:#bbb; font-size:0.78rem;">JPG, PNG, WEBP — อย่างน้อย 1 รูป (สูงสุด 5MB/รูป)</small>
            </div>
            <span class="field-error" id="hs_imagesError_${idx}"></span>
            <div id="hs_preview_${idx}"
                 style="display:none; grid-template-columns:repeat(3,1fr); gap:6px; margin-top:8px;"></div>
        </div>
    `;
    list.appendChild(block);

    if (!document.getElementById('hs-upload-style')) {
        const style = document.createElement('style');
        style.id = 'hs-upload-style';
        style.textContent = `
            .hs-upload-zone {
                border: 2px dashed #b5d5b7; border-radius: 8px;
                padding: 1.2rem; text-align: center; cursor: pointer;
                background: #f9fdf9; transition: border-color 0.2s;
            }
            .hs-upload-zone:hover { border-color: #2d6a2d; }
            .hs-upload-zone.is-invalid { border-color: #dc3545; background: #fff5f5; }
            .hs-preview-card {
                position: relative; border-radius: 6px;
                overflow: hidden; aspect-ratio: 4/3;
            }
            .hs-preview-card img { width:100%; height:100%; object-fit:cover; }
            .hs-remove-btn {
                position: absolute; top: 3px; right: 3px;
                background: rgba(220,53,69,0.85); color: #fff;
                border: none; border-radius: 50%;
                width: 20px; height: 20px; font-size: 0.7rem;
                cursor: pointer; display: flex;
                align-items: center; justify-content: center;
            }
            .hs-img-num {
                position: absolute; bottom: 3px; left: 3px;
                background: rgba(0,0,0,0.5); color: #fff;
                font-size: 0.7rem; padding: 1px 5px; border-radius: 3px;
            }
        `;
        document.head.appendChild(style);
    }

    bindHomestayListeners(idx);

    // ถ้าหน้า Step 2 กำลังแสดงอยู่ (เช่น กดเพิ่มโฮมสเตย์อีกแห่งระหว่างอยู่หน้านี้) ให้สร้างแผนที่ทันที
    // ถ้ายังไม่แสดง (เช่น ตอนโหลดหน้าครั้งแรก) จะไปสร้างตอน goToPage(2) แทน เพราะ Leaflet ต้องการ
    // container ที่มองเห็นอยู่จริงถึงจะคำนวณขนาดแผนที่ได้ถูกต้อง
    const page2 = document.getElementById('page-2');
    if (page2 && !page2.classList.contains('d-none')) {
        initHomestayMap(idx);
    }
}

function removeHomestay(idx) {
    document.getElementById(`homestay-block-${idx}`)?.remove();
    delete homestayImages[idx];
    if (homestayMaps[idx]) {
        homestayMaps[idx].map.remove();
        delete homestayMaps[idx];
    }
}

function handleHsImages(idx, fileList) {
    const MAX = 5 * 1024 * 1024;
    Array.from(fileList).forEach(file => {
        if (!['image/jpeg','image/png','image/webp'].includes(file.type)) {
            alert(`"${file.name}" ไม่รองรับ — ใช้ได้เฉพาะ JPG, PNG, WebP`); return;
        }
        if (file.size > MAX) { alert(`"${file.name}" มีขนาดเกิน 5MB`); return; }
        const dup = homestayImages[idx]?.some(f => f.name === file.name && f.size === file.size);
        if (!dup) homestayImages[idx].push(file);
    });
    renderHsPreviews(idx);
    validateHsImages(idx);
}

function handleHsDrop(e, idx) {
    e.preventDefault();
    document.getElementById(`hs_zone_${idx}`).style.borderColor = '#b5d5b7';
    handleHsImages(idx, e.dataTransfer.files);
}

function renderHsPreviews(idx) {
    const grid = document.getElementById(`hs_preview_${idx}`);
    grid.innerHTML = '';
    if (!homestayImages[idx] || homestayImages[idx].length === 0) {
        grid.style.display = 'none'; return;
    }
    grid.style.display = 'grid';
    homestayImages[idx].forEach((file, i) => {
        const blobUrl = URL.createObjectURL(file);
        const card = document.createElement('div');
        card.className = 'hs-preview-card';
        card.innerHTML = `
            <img src="${blobUrl}" alt="">
            <button type="button" class="hs-remove-btn" onclick="removeHsImage(${idx}, ${i})">
                <i class="fas fa-xmark"></i>
            </button>
            <span class="hs-img-num">${i + 1}</span>
        `;
        grid.appendChild(card);
    });
}

function removeHsImage(idx, imgIdx) {
    homestayImages[idx].splice(imgIdx, 1);
    renderHsPreviews(idx);
    validateHsImages(idx);
}

// ============================================================
//  SUBMIT ALL
// ============================================================
async function submitAll() {
    if (!validateAllHomestays()) return;

    const btn = document.querySelector('.btn-submit');
    if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>กำลังส่ง...'; }

    const fd = new FormData();

    fd.append('firstname', document.getElementById('firstname').value.trim());
    fd.append('lastname',  document.getElementById('lastname').value.trim());
    fd.append('email',     document.getElementById('email').value.trim());
    fd.append('phone',     document.getElementById('phone').value.trim());
    fd.append('password',  document.getElementById('password').value.trim());

    const blocks = document.querySelectorAll('.homestay-block');
    let hsIndex  = 0;

    blocks.forEach(block => {
        const idx = block.id.split('-').pop();
        fd.append(`homestays[${hsIndex}].homestayname`, document.getElementById(`hs_name_${idx}`).value.trim());
        fd.append(`homestays[${hsIndex}].address`,      document.getElementById(`hs_address_${idx}`).value.trim());
        fd.append(`homestays[${hsIndex}].description`,  document.getElementById(`hs_desc_${idx}`).value.trim());
        (homestayImages[idx] || []).forEach(file => {
            fd.append(`homestays[${hsIndex}].images`, file);
        });
        hsIndex++;
    });

    try {
        const res  = await fetch('/owner/register', { method: 'POST', body: fd });
        const data = await res.json();
        if (data.success) {
            goToPage(3);
        } else {
            alert('เกิดข้อผิดพลาด: ' + data.message);
            if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-paper-plane me-2"></i>ส่งคำขอสมัคร'; }
        }
    } catch {
        alert('เกิดข้อผิดพลาด กรุณาลองใหม่');
        if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-paper-plane me-2"></i>ส่งคำขอสมัคร'; }
    }
}

// ============================================================
//  VALIDATORS HOMESTAY
// ============================================================
function bindHomestayListeners(idx) {
    const nameInput    = document.getElementById(`hs_name_${idx}`);
    const addressInput = document.getElementById(`hs_address_${idx}`);
    const descInput    = document.getElementById(`hs_desc_${idx}`);
    nameInput.addEventListener('blur',    () => validateHsName(idx));
    addressInput.addEventListener('blur', () => validateHsAddress(idx));
    descInput.addEventListener('blur',    () => validateHsDesc(idx));
    nameInput.addEventListener('input',    () => { if (nameInput.classList.contains('is-invalid'))    validateHsName(idx); });
    addressInput.addEventListener('input', () => { if (addressInput.classList.contains('is-invalid')) validateHsAddress(idx); });
    descInput.addEventListener('input',    () => { if (descInput.classList.contains('is-invalid'))    validateHsDesc(idx); });
}
function validateHsName(idx) {
    const input = document.getElementById(`hs_name_${idx}`);
    const val = input.value.trim();
    if (val === '') { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ต้องไม่เป็นค่าว่าง'); return false; }
    if (!/^[ก-๙a-zA-Z\s]+$/.test(val)) { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ใช้ได้เฉพาะตัวอักษรภาษาไทยหรืออังกฤษ'); return false; }
    if (val.length < 3 || val.length > 255) { showError(input, `hs_nameError_${idx}`, 'ชื่อโฮมสเตย์ต้องมีความยาว 3–255 ตัวอักษร'); return false; }
    showValid(input, `hs_nameError_${idx}`); return true;
}
function validateHsAddress(idx) {
    const input = document.getElementById(`hs_address_${idx}`);
    const val = input.value.trim();
    if (val === '') { showError(input, `hs_addressError_${idx}`, 'ที่อยู่โฮมสเตย์ต้องไม่เป็นค่าว่าง'); return false; }
    if (!/^[ก-๙a-zA-Z0-9\s\/,\-]+$/.test(val)) { showError(input, `hs_addressError_${idx}`, 'ที่อยู่ใช้ได้เฉพาะตัวอักษรไทย อังกฤษ ตัวเลข และ / , -'); return false; }
    if (val.length > 255) { showError(input, `hs_addressError_${idx}`, 'ที่อยู่โฮมสเตย์ต้องไม่เกิน 255 ตัวอักษร'); return false; }
    showValid(input, `hs_addressError_${idx}`); return true;
}
function validateHsDesc(idx) {
    const input = document.getElementById(`hs_desc_${idx}`);
    const val = input.value.trim();
    if (val === '') { showError(input, `hs_descError_${idx}`, 'กรุณากรอกรายละเอียดโฮมสเตย์'); return false; }
    if (val.length < 3 || val.length > 5000) { showError(input, `hs_descError_${idx}`, 'รายละเอียดโฮมสเตย์ต้องมีความยาว 3–5000 ตัวอักษร'); return false; }
    showValid(input, `hs_descError_${idx}`); return true;
}
function validateHsImages(idx) {
    const zone = document.getElementById(`hs_zone_${idx}`);
    const count = (homestayImages[idx] || []).length;
    if (count === 0) {
        zone.classList.add('is-invalid');
        showSpanError(`hs_imagesError_${idx}`, 'กรุณาอัปโหลดรูปภาพโฮมสเตย์อย่างน้อย 1 รูป');
        return false;
    }
    zone.classList.remove('is-invalid');
    hideSpan(`hs_imagesError_${idx}`);
    return true;
}
function validateAllHomestays() {
    const blocks = document.querySelectorAll('.homestay-block');
    if (blocks.length === 0) { alert('กรุณาเพิ่มข้อมูลโฮมสเตย์อย่างน้อย 1 แห่ง'); return false; }
    let allOk = true;
    blocks.forEach(block => {
        const idx = block.id.split('-').pop();
        if (!validateHsName(idx) || !validateHsAddress(idx) || !validateHsDesc(idx) || !validateHsImages(idx)) allOk = false;
    });
    if (!allOk) {
        const first = document.querySelector('#page-2 .is-invalid');
        if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); first.focus(); }
    }
    return allOk;
}

// ============================================================
//  NAVIGATION
// ============================================================
function goToPage(n) {
    [1, 2, 3].forEach(i => {
        document.getElementById(`page-${i}`).classList.add('d-none');
        document.getElementById(`step-dot-${i}`)?.classList.remove('active', 'done');
    });
    document.getElementById(`page-${n}`).classList.remove('d-none');

    // Step 2 เพิ่งถูกแสดง -> สร้าง/รีเฟรชขนาดแผนที่ของทุกโฮมสเตย์ที่มีอยู่ตอนนี้
    // ใช้ requestAnimationFrame เพื่อรอให้ browser คำนวณ layout ของ container ที่เพิ่ง
    // เอา d-none ออกให้เสร็จก่อน ไม่งั้น Leaflet อาจอ่านขนาด container ผิด (เช่น 0 หรือค้างจาก viewport)
    // ทำให้แผนที่แสดงผลเลยขอบการ์ด
    if (n === 2) {
        requestAnimationFrame(() => {
            document.querySelectorAll('.homestay-block').forEach(block => {
                const idx = block.id.split('-').pop();
                initHomestayMap(idx);
            });
        });
    }

    for (let i = 1; i < n; i++) {
        document.getElementById(`step-dot-${i}`)?.classList.add('done');
        document.getElementById(`step-line-${i}`)?.classList.add('done');
    }
    document.getElementById(`step-dot-${n}`)?.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
function goBack() { goToPage(1); }

// ============================================================
//  PASSWORD TOGGLE
// ============================================================
document.querySelectorAll('.toggle-pw').forEach(icon => {
    icon.addEventListener('click', () => {
        const input = document.getElementById(icon.dataset.target);
        if (!input) return;
        if (input.type === 'password') { input.type = 'text'; icon.classList.replace('fa-eye', 'fa-eye-slash'); }
        else { input.type = 'password'; icon.classList.replace('fa-eye-slash', 'fa-eye'); }
    });
});

// ============================================================
//  STEP 1 — LIVE VALIDATION
// ============================================================
const step1Fields = [
    { id: 'firstname',       fn: validateFirstname       },
    { id: 'lastname',        fn: validateLastname        },
    { id: 'email',           fn: validateEmail           }, // async ก็ใช้ตรงนี้ได้ปกติ
    { id: 'phone',           fn: validatePhone           },
    { id: 'password',        fn: validatePassword        },
    { id: 'confirmPassword', fn: validateConfirmPassword },
];
step1Fields.forEach(({ id, fn }) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener('blur', fn);
    el.addEventListener('input', () => { if (el.classList.contains('is-invalid')) fn(); });
});

document.getElementById('password')?.addEventListener('input', () => {
    const cp = document.getElementById('confirmPassword');
    if (cp && cp.value !== '') validateConfirmPassword();
});

async function validateStep1() {
    const results = await Promise.all([
        validateFirstname(), validateLastname(), validateEmail(), validatePhone(),
        validatePassword(), validateConfirmPassword(), validateAgree()
    ]);
    const ok = results.every(Boolean);
    if (ok) {
        goToPage(2);
    } else {
        const first = document.querySelector('#page-1 .is-invalid');
        if (first) { first.scrollIntoView({ behavior: 'smooth', block: 'center' }); first.focus(); }
    }
}

// ============================================================
//  INIT
// ============================================================
addHomestay();