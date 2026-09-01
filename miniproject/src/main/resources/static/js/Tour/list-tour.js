/* ═══════════════════════════════════════════
   โหลดรูป thumbnail จาก data-src ผ่าน JS
   หลีกเลี่ยง HTML attribute ยาวเกิน limit
═══════════════════════════════════════════ */
document.querySelectorAll('.lazy-thumb').forEach(function (img) {
    const raw = img.getAttribute('data-src') || '';
    if (!raw) return;

    // ใช้รูปแรกจาก filename1.jpg||filename2.jpg
    const firstName = raw.split('||')[0].trim();
    img.src = '/uploads/tours/' + firstName;

    img.removeAttribute('data-src');
});

/* ═══════════════════════════════════════════
   นับจำนวนทัวร์แต่ละสถานะ แล้วเติมลง badge
═══════════════════════════════════════════ */
function computeStatusCounts() {
    const rows = document.querySelectorAll('.tour-row');
    const counts = {};
    rows.forEach(row => {
        const s = row.getAttribute('data-status') || '';
        counts[s] = (counts[s] || 0) + 1;
    });

    const badgeMap = {
        'เปิดรับจอง': 'count-open',
        'เต็ม': 'count-full',
        'ปิด': 'count-closed',
    };

    Object.keys(badgeMap).forEach(status => {
        const el = document.getElementById(badgeMap[status]);
        if (el) el.textContent = counts[status] || 0;
    });
}

/* ═══════════════════════════════════════════
   FILTER TABS — กรองตามสถานะ
═══════════════════════════════════════════ */
function filterTours(status, btn) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const rows = document.querySelectorAll('.tour-row');
    let visible = 0;

    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status') || '';
        if (rowStatus === status) {
            row.style.display = '';
            visible++;
        } else {
            row.style.display = 'none';
        }
    });

    const emptyState = document.getElementById('emptyState');
    if (emptyState) {
        emptyState.style.display = visible === 0 ? 'block' : 'none';
    }
}

// โหลดครั้งเดียว: นับจำนวนสถานะทั้งหมด และเปิดแท็บ "เปิดรับจอง" เป็นค่าเริ่มต้น
document.addEventListener('DOMContentLoaded', function () {
    computeStatusCounts();
    filterTours('เปิดรับจอง', document.getElementById('tab-open'));
});
document.addEventListener('DOMContentLoaded', function () {
    updateCounts();
    filterTours('เปิดรับจอง'); // กรองค่าเริ่มต้นเป็น "เปิดรับจอง"
});

// เปิด/ปิด Dropdown
function toggleStatusDropdown() {
    const dropdown = document.getElementById('statusFilterDropdown');
    dropdown.classList.toggle('open');
}

// ปิด Dropdown เมื่อคลิกนอกพื้นที่
window.addEventListener('click', function (e) {
    const dropdown = document.getElementById('statusFilterDropdown');
    if (dropdown && !dropdown.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});

// เลือกสถานะและกรองข้อมูล
function selectStatusFilter(status, itemElement) {
    const triggerBtn = document.getElementById('dropdownTriggerBtn');
    const dropdown = document.getElementById('statusFilterDropdown');

    // อัปเดตคลาส active ที่เมนูย่อย
    document.querySelectorAll('.dropdown-item').forEach(el => el.classList.remove('active'));
    if (itemElement) itemElement.classList.add('active');

    // เปลี่ยนสีและข้อความของปุ่มกดหลัก
    triggerBtn.className = 'filter-dropdown-trigger';
    let iconClass = 'fa-circle-check';
    let countId = 'count-open';

    if (status === 'เปิดรับจอง') {
        triggerBtn.classList.add('status-available');
        iconClass = 'fa-circle-check';
        countId = 'count-open-menu';
    } else if (status === 'เต็ม') {
        triggerBtn.classList.add('status-full');
        iconClass = 'fa-users';
        countId = 'count-full-menu';
    } else if (status === 'ปิด') {
        triggerBtn.classList.add('status-closed');
        iconClass = 'fa-ban';
        countId = 'count-closed-menu';
    }

    const currentCount = document.getElementById(countId) ? document.getElementById(countId).innerText : '0';
    triggerBtn.querySelector('.selected-text').innerHTML = `
        <i class="fas ${iconClass}"></i>
        <span>${status}</span>
        <span class="count-badge">${currentCount}</span>
    `;

    dropdown.classList.remove('open');
    filterTours(status);
}

// ฟังก์ชันซ่อน/แสดงแถวตาราง
function filterTours(status) {
    const rows = document.querySelectorAll('#tourTable tbody tr.tour-row');
    let visibleCount = 0;

    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        if (rowStatus === status) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });

    const emptyState = document.getElementById('emptyState');
    if (emptyState) {
        emptyState.style.display = visibleCount === 0 ? 'block' : 'none';
    }
}

// คำนวณจำนวนในแต่ละสถานะ
function updateCounts() {
    const rows = document.querySelectorAll('#tourTable tbody tr.tour-row');
    let openCount = 0;
    let fullCount = 0;
    let closedCount = 0;

    rows.forEach(row => {
        const status = row.getAttribute('data-status');
        if (status === 'เปิดรับจอง') openCount++;
        else if (status === 'เต็ม') fullCount++;
        else if (status === 'ปิด') closedCount++;
    });

    // อัปเดตตัวเลขในเมนู Dropdown
    if (document.getElementById('count-open-menu')) document.getElementById('count-open-menu').innerText = openCount;
    if (document.getElementById('count-full-menu')) document.getElementById('count-full-menu').innerText = fullCount;
    if (document.getElementById('count-closed-menu')) document.getElementById('count-closed-menu').innerText = closedCount;

    // อัปเดตตัวเลขที่ปุ่มหลักเริ่มต้น
    if (document.getElementById('count-open')) document.getElementById('count-open').innerText = openCount;
}