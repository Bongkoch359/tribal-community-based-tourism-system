// ── รูปภาพ: อัปโหลดเป็นไฟล์จริง (ไม่ใช้ base64) + drag & drop เหมือนหน้าแก้ไขทัวร์ ──
const fileInput = document.getElementById('fileInput');
const previewGrid = document.getElementById('previewGrid');
const uploadZone = document.getElementById('uploadZone');

let fileList = []; // เก็บ File object จริง ๆ

fileInput.addEventListener('change', function () {
    addFiles(Array.from(this.files));
    this.value = ''; // reset เพื่อให้เลือกซ้ำได้
});

function addFiles(files) {
    files.forEach(file => {
        if (file && file.type.startsWith('image/')) {
            fileList.push(file);
        }
    });
    syncFileInput();
    renderPreviews();
}

// อัปเดต input.files ให้ตรงกับ fileList (จำเป็นเวลาลบรูปออกจาก preview)
function syncFileInput() {
    const dt = new DataTransfer();
    fileList.forEach(file => dt.items.add(file));
    fileInput.files = dt.files;
}

function renderPreviews() {
    previewGrid.innerHTML = '';

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
            renderPreviews();
        };

        overlay.appendChild(delBtn);
        thumb.appendChild(img);
        thumb.appendChild(overlay);
        previewGrid.appendChild(thumb);
    });
}

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
// เลือกทัวร์ -> แสดงฟิลด์สถานที่ (บังคับกรอก)
// ไม่ระบุทัวร์ (ข่าวสารทั่วไป) -> ซ่อนฟิลด์สถานที่ (ไม่บังคับกรอก)
const tourSelect = document.getElementById('tourSelect');
const locationGroup = document.getElementById('locationGroup');
const locationInput = document.getElementById('locationInput');

function toggleLocationField() {
    if (tourSelect.value) {
        locationGroup.style.display = '';
        locationInput.setAttribute('required', 'required');
        initLocationMap(); // ✅ สร้างแผนที่ตอนช่องสถานที่เพิ่งถูกเปิดใช้งาน
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
/* ═══════════════════════════════════════════
LEAFLET + OPENSTREETMAP — ช่วยหาที่อยู่สำหรับฟิลด์ "สถานที่"
(โค้ดชุดเดียวกับหน้า addtour: Nominatim search + reverse geocode + geolocation)
═══════════════════════════════════════════ */
const DEFAULT_MAP_CENTER = [18.7883, 98.9853]; // ศูนย์กลางเชียงใหม่
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
    if (!navigator.geolocation) {
        onFallback();
        return;
    }
    navigator.geolocation.getCurrentPosition(
        (pos) => onLocated([pos.coords.latitude, pos.coords.longitude]),
        () => onFallback(),
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
    );
}

function initLocationMap() {
    if (locationMap) return; // สร้างครั้งเดียวพอ

    locationMap = createLeafletMap('locationMap');
    locationMarker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(locationMap);

    locationMarker.on('dragend', () => {
        const pos = locationMarker.getLatLng();
        reverseGeocode(pos.lat, pos.lng, (address) => {
            locationInput.value = address;
        });
    });

    locationMap.on('click', (e) => {
        locationMarker.setLatLng(e.latlng);
        reverseGeocode(e.latlng.lat, e.latlng.lng, (address) => {
            locationInput.value = address;
        });
    });

    setTimeout(() => locationMap.invalidateSize(), 200); // กัน bug แผนที่เบี้ยวตอนเพิ่งโผล่จาก display:none

    // ✅ ปักหมุดตามตำแหน่งปัจจุบันอัตโนมัติ ถ้าช่องยังว่างอยู่ (เช่น ยังไม่มีค่าจาก DB)
    if (!locationInput.value.trim()) {
        getCurrentPositionOrDefault(
            ([lat, lng]) => {
                locationMap.setView([lat, lng], 16);
                locationMarker.setLatLng([lat, lng]);
                reverseGeocode(lat, lng, (address) => {
                    locationInput.value = address;
                });
            },
            () => { /* หาไม่ได้ ใช้ศูนย์กลางเชียงใหม่ตามเดิม */ }
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