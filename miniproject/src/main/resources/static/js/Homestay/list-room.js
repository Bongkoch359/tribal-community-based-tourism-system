function blockAddRoom(e) {
    e.preventDefault();
    alert('กรุณากรอกข้อมูลบัญชีธนาคารให้ครบก่อน จึงจะสามารถเพิ่มห้องพักได้');
    return false;
}

const statusKeyMap = {
    'เปิดจอง': 'available',
    'เต็ม': 'full',
    'ปิดปรับปรุง': 'maintenance',
};

// นับจำนวนห้องแต่ละสถานะ แล้วอัปเดต Badge
function computeStatusCounts() {
    const rows = document.querySelectorAll('#roomBody tr[data-status]');
    const counts = {};
    rows.forEach(row => {
        const s = row.getAttribute('data-status');
        counts[s] = (counts[s] || 0) + 1;
    });

    Object.keys(statusKeyMap).forEach(status => {
        const key = statusKeyMap[status];
        const count = counts[status] || 0;

        const badgeTrigger = document.getElementById('count-' + key);
        const badgeMenu = document.getElementById('count-' + key + '-menu');

        if (badgeTrigger) badgeTrigger.textContent = count;
        if (badgeMenu) badgeMenu.textContent = count;
    });
}

function filterByStatus(status) {
    const rows = document.querySelectorAll('#roomBody tr[data-status]');
    let visibleCount = 0;

    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        const match = (rowStatus === status);
        row.style.display = match ? '' : 'none';
        if (match) visibleCount++;
    });

    const oldNotice = document.getElementById('noStatusRow');
    if (oldNotice) oldNotice.remove();

    if (visibleCount === 0) {
        const tbody = document.getElementById('roomBody');
        const tr = document.createElement('tr');
        tr.id = 'noStatusRow';
        tr.innerHTML = `
            <td colspan="7">
                <div class="empty-state">
                    <i class="fas fa-door-open"></i>
                    <p>ไม่มีห้องพักในสถานะ "${status}"</p>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    }
}

function toggleStatusDropdown() {
    document.getElementById('statusFilterDropdown').classList.toggle('open');
}

window.addEventListener('click', function (e) {
    const dropdown = document.getElementById('statusFilterDropdown');
    if (dropdown && !dropdown.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});

function selectStatusFilter(status, itemEl) {
    const triggerBtn = document.getElementById('dropdownTriggerBtn');
    const selectedTextContainer = triggerBtn.querySelector('.selected-text');

    const iconClass = itemEl.querySelector('i').className;
    const badgeText = itemEl.querySelector('.count-badge').textContent;

    // อัปเดตข้อความ + ไอคอน + ตัวเลขบนปุ่ม
    selectedTextContainer.innerHTML = `
        <i class="${iconClass}"></i>
        <span>${status}</span>
        <span class="count-badge" id="count-${statusKeyMap[status]}">${badgeText}</span>
    `;

    // อัปเดตสีกรอบและพื้นหลังปุ่ม
    triggerBtn.className = 'filter-dropdown-trigger status-' + statusKeyMap[status];

    // เปลี่ยน Active class
    document.querySelectorAll('.dropdown-item').forEach(el => el.classList.remove('active'));
    itemEl.classList.add('active');

    // ปิดเมนูและกรองข้อมูล
    document.getElementById('statusFilterDropdown').classList.remove('open');
    filterByStatus(status);
}

document.addEventListener('DOMContentLoaded', function () {
    computeStatusCounts();
    // เริ่มต้นแสดงสถานะ "เปิดจอง"
    filterByStatus('เปิดจอง');
});