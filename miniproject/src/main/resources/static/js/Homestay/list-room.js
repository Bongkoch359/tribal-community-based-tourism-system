function blockAddRoom(e) {
    e.preventDefault();
    alert('กรุณากรอกข้อมูลบัญชีธนาคารให้ครบก่อน จึงจะสามารถเพิ่มห้องพักได้');
    return false;
}

// ใช้ map สถานะ -> key (ภาษาอังกฤษ) เพื่ออ้างอิง id/class ให้ปลอดภัย
const statusKeyMap = {
    'เปิดจอง': 'available',
    'เต็ม': 'full',
    'ปิดปรับปรุง': 'maintenance',
};

// นับจำนวนห้องแต่ละสถานะจากแถวในตาราง แล้วเติมตัวเลขลง badge
function computeStatusCounts() {
    const rows = document.querySelectorAll('#roomBody tr[data-status]');
    const counts = {};
    rows.forEach(row => {
        const s = row.getAttribute('data-status');
        counts[s] = (counts[s] || 0) + 1;
    });

    Object.keys(statusKeyMap).forEach(status => {
        const key = statusKeyMap[status];
        const badge = document.getElementById('count-' + key);
        if (badge) badge.textContent = counts[status] || 0;
    });
}

function filterRooms(status, btn) {
    document.querySelectorAll('.filter-btn').forEach(b => {
        b.classList.remove('active', 'active-available', 'active-full', 'active-maintenance');
    });
    btn.classList.add('active', 'active-' + statusKeyMap[status]);
    filterByStatus(status);
}

function filterByStatus(status) {
    const rows = document.querySelectorAll('#roomBody tr[data-status]');
    let visibleCount = 0;
    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        const match = rowStatus === status;
        row.style.display = match ? '' : 'none';
        if (match) visibleCount++;
    });

    const oldNotice = document.getElementById('noStatusRow');
    if (oldNotice) oldNotice.remove();

    // ถ้าไม่มีห้องพักในสถานะที่เลือก ให้ขึ้นข้อความแจ้งเตือน
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

document.addEventListener('DOMContentLoaded', function () {
    computeStatusCounts();

    // เปิดสถานะแรกเป็นค่า default (ตามปุ่มแรกใน HTML)
    const firstBtn = document.querySelector('#filterBar .filter-btn');
    if (firstBtn) filterByStatus(firstBtn.dataset.status);
});