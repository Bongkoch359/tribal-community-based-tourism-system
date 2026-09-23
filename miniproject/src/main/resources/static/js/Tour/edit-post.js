
document.querySelectorAll('textarea.form-control').forEach(function (ta) {
    const autoResize = () => {
        ta.style.height = 'auto';
        ta.style.height = (ta.scrollHeight + 2) + 'px';
    };
    ta.addEventListener('input', autoResize);

    autoResize();
});

const fileInput = document.getElementById('fileInput');
const mainImg = document.getElementById('mainImg');
const noImgEl = document.getElementById('noImagePlaceholder');
const counter = document.getElementById('imgCounter');
const imgTotal = document.getElementById('imgTotal');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const thumbStrip = document.getElementById('thumbStrip');
const imgCountNote = document.getElementById('imgCountNote');
const existingImagesDataEl = document.getElementById('existingImagesData');
const keepImagesInput = document.getElementById('keepImagesInput');

const MAX_IMAGES = 5;

// รูปเดิมจาก DB (คั่นด้วย ||) — ยังเก็บไว้จนกว่าผู้ใช้จะกดลบ
let existingImages = existingImagesDataEl && existingImagesDataEl.value
    ? existingImagesDataEl.value.split('||').filter(Boolean)
    : [];

let newFileList = [];
let images = existingImages.slice();
let current = 0;

// ส่งรายชื่อรูปเดิมที่ยังเหลืออยู่ (หลังลบ) กลับไปให้ backend ผ่าน hidden input
function syncKeepImagesInput() {
    keepImagesInput.value = existingImages.join('||');
}


function syncFileInput() {
    const dt = new DataTransfer();
    newFileList.forEach(file => dt.items.add(file));
    fileInput.files = dt.files;
}

function refreshUI() {
    const total = images.length;
    if (total === 0) {
        mainImg.style.display = 'none';
        noImgEl.style.display = '';
        counter.style.display = 'none';
        prevBtn.style.display = 'none';
        nextBtn.style.display = 'none';
    } else {
        mainImg.style.display = '';
        noImgEl.style.display = 'none';
        if (current >= total) current = total - 1;
        mainImg.src = images[current];
        const showNav = total > 1;
        counter.style.display = showNav ? '' : 'none';
        prevBtn.style.display = showNav ? '' : 'none';
        nextBtn.style.display = showNav ? '' : 'none';
        if (imgTotal) imgTotal.textContent = total;
        counter.childNodes[0].textContent = (current + 1) + ' / ';
    }
    thumbStrip.querySelectorAll('img').forEach((t, i) => t.classList.toggle('active', i === current));

    if (imgCountNote) {
        imgCountNote.style.display = total > 0 ? '' : 'none';
        imgCountNote.textContent = `รูปภาพ ${total} / ${MAX_IMAGES}`;
        imgCountNote.classList.toggle('limit-reached', total >= MAX_IMAGES);
    }
    const uploadLabel = document.querySelector('.upload-label');
    if (uploadLabel) {
        if (total >= MAX_IMAGES) {
            uploadLabel.style.opacity = '.5';
            uploadLabel.style.pointerEvents = 'none';
            fileInput.disabled = true;
        } else {
            uploadLabel.style.opacity = '';
            uploadLabel.style.pointerEvents = '';
            fileInput.disabled = false;
        }
    }
}

function goToImg(index) {
    if (images.length === 0) return;
    current = (index + images.length) % images.length;
    refreshUI();
}
function nextImg() { goToImg(current + 1); }
function prevImg() { goToImg(current - 1); }

function buildExistingThumb(url) {
    const wrap = document.createElement('div');
    wrap.className = 'thumb-wrap existing-thumb';
    wrap.dataset.src = url;

    const img = document.createElement('img');
    img.src = url;
    img.alt = 'รูปโพสต์ปัจจุบัน';
    img.onclick = () => goToImg(images.indexOf(url));

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'thumb-remove';
    removeBtn.textContent = '✕';
    removeBtn.onclick = () => removeExistingThumb(wrap, url);

    wrap.appendChild(img);
    wrap.appendChild(removeBtn);
    return wrap;
}

function removeExistingThumb(wrap, url) {
    const eIdx = existingImages.indexOf(url);
    if (eIdx > -1) existingImages.splice(eIdx, 1);
    const gIdx = images.indexOf(url);
    if (gIdx > -1) images.splice(gIdx, 1);
    wrap.remove();
    if (current >= images.length) current = Math.max(0, images.length - 1);
    syncKeepImagesInput();
    refreshUI();
}

function buildNewThumb(file, objectUrl) {
    const wrap = document.createElement('div');
    wrap.className = 'thumb-wrap new-thumb';

    const img = document.createElement('img');
    img.src = objectUrl;
    img.alt = 'รูปใหม่';
    img.onclick = () => goToImg(images.indexOf(objectUrl));

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'thumb-remove';
    removeBtn.textContent = '✕';
    removeBtn.onclick = () => removeNewThumb(wrap, file, objectUrl);

    wrap.appendChild(img);
    wrap.appendChild(removeBtn);
    return wrap;
}

function removeNewThumb(wrap, file, objectUrl) {
    const fIdx = newFileList.indexOf(file);
    if (fIdx > -1) newFileList.splice(fIdx, 1);
    const gIdx = images.indexOf(objectUrl);
    if (gIdx > -1) images.splice(gIdx, 1);
    wrap.remove();
    if (current >= images.length) current = Math.max(0, images.length - 1);
    syncFileInput();
    refreshUI();
}

function addFiles(files) {
    files.forEach(file => {
        if (!file || !file.type.startsWith('image/')) return;
        if (images.length >= MAX_IMAGES) return;
        const objectUrl = URL.createObjectURL(file);
        newFileList.push(file);
        images.push(objectUrl);
        thumbStrip.appendChild(buildNewThumb(file, objectUrl));
    });
    syncFileInput();
    refreshUI();
}

fileInput.addEventListener('change', function () {
    const files = Array.from(this.files);
    this.value = '';
    addFiles(files);
});


existingImages.forEach(url => thumbStrip.appendChild(buildExistingThumb(url)));
syncKeepImagesInput();
refreshUI();

// ── แสดง/ซ่อนฟิลด์สถานที่ ตามการเลือกทัวร์ ──
const tourSelect = document.getElementById('tourSelect');
const locationGroup = document.getElementById('locationGroup');
const locationInput = document.getElementById('locationInput');

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
    if (locationMap) return;

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

    setTimeout(() => locationMap.invalidateSize(), 200);

    const existingAddress = locationInput.value.trim();
    if (existingAddress) {
        // โหมดแก้ไข: มีที่อยู่เดิมอยู่แล้ว -> ค้นหาพิกัดจากข้อความแล้วปักหมุดตำแหน่งเดิม
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

            })
            .catch(() => { });
    } else {
        // ยังไม่มีที่อยู่ ใช้ตำแหน่งปัจจุบัน
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
tourSelect.addEventListener('change', toggleLocationField);
toggleLocationField();

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