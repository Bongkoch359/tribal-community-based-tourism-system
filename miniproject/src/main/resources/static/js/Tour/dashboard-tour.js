function dismissBankWarning() {
    const b = document.getElementById('bankWarningBanner');
    if (!b) return;
    b.style.transition = 'opacity .3s, max-height .4s';
    b.style.opacity = '0'; b.style.overflow = 'hidden';
    b.style.maxHeight = b.offsetHeight + 'px';
    setTimeout(() => { b.style.maxHeight = '0'; b.style.padding = '0'; }, 10);
    setTimeout(() => { b.style.display = 'none'; }, 420);
}
function dismissSignatureWarning() {
    const b = document.getElementById('signatureWarningBanner');
    if (!b) return;
    b.style.transition = 'opacity .3s, max-height .4s';
    b.style.opacity = '0'; b.style.overflow = 'hidden';
    b.style.maxHeight = b.offsetHeight + 'px';
    setTimeout(() => { b.style.maxHeight = '0'; b.style.padding = '0'; }, 10);
    setTimeout(() => { b.style.display = 'none'; }, 420);
}
function toggleCustomRange() {
    const form = document.getElementById('customRangeForm');
    form.style.display = form.style.display === 'none' ? 'flex' : 'none';
}
function switchTrendTab(target) {
    document.getElementById('trendPanel-revenue').style.display = target === 'revenue' ? 'flex' : 'none';
    document.getElementById('trendPanel-bookingCount').style.display = target === 'bookingCount' ? 'flex' : 'none';
    document.querySelectorAll('.trend-tab').forEach(btn => {
        btn.classList.toggle('trend-tab-active', btn.dataset.target === target);
    });
}
const bookingStatusClassMap = {
    'WAITING_APPROVAL': 'status-waiting',
    'CONFIRMED': 'status-confirmed',
    'COMPLETED': 'status-completed',
    'CANCEL': 'status-cancel'
};

function toggleBookingDropdown() {
    document.getElementById('bookingStatusDropdown').classList.toggle('open');
}

// ปิด Dropdown เมื่อคลิกนอกพื้นที่
window.addEventListener('click', function (e) {
    const dropdown = document.getElementById('bookingStatusDropdown');
    if (dropdown && !dropdown.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});

// ฟังก์ชันเลือกฟิลเตอร์
function selectBookingFilter(statusKey, itemEl) {
    const triggerBtn = document.getElementById('bookingDropdownTriggerBtn');
    const selectedTextContainer = triggerBtn.querySelector('.selected-text');

    const iconClass = itemEl.querySelector('i').className;
    const label = itemEl.querySelector('.label-text').textContent;
    const badgeCount = itemEl.querySelector('.count-badge').textContent;

    selectedTextContainer.innerHTML = `
            <i class="${iconClass}"></i>
            <span>${label}</span>
            <span class="count-badge">${badgeCount}</span>
        `;

    triggerBtn.className = 'filter-dropdown-trigger ' + bookingStatusClassMap[statusKey];

    document.querySelectorAll('#bookingDropdownMenu .dropdown-item').forEach(el => el.classList.remove('active'));
    itemEl.classList.add('active');

    document.getElementById('bookingStatusDropdown').classList.remove('open');

    filterBookings(statusKey);
}

// กรองแถวในตาราง
function filterBookings(statusKey) {
    const rows = document.querySelectorAll('#tourBookingsTbody tr[data-status]');
    const noResults = document.getElementById('noFilterResults');
    let visibleCount = 0;

    rows.forEach(r => {
        const match = (r.getAttribute('data-status') === statusKey);
        r.style.display = match ? '' : 'none';
        if (match) visibleCount++;
    });

    if (noResults) {
        noResults.style.display = (visibleCount === 0 && rows.length > 0) ? 'block' : 'none';
    }
}

// กำหนดสถานะเริ่มต้นเมื่อโหลดหน้า
document.addEventListener('DOMContentLoaded', () => {
    const rows = document.querySelectorAll('#tourBookingsTbody tr[data-status]');
    if (rows.length === 0) return;

    const priorityOrder = ['WAITING_APPROVAL', 'CONFIRMED', 'COMPLETED', 'CANCEL'];
    let selectedStatus = 'WAITING_APPROVAL';

    for (const status of priorityOrder) {
        const hasAny = Array.from(rows).some(r => r.getAttribute('data-status') === status);
        if (hasAny) {
            selectedStatus = status;
            break;
        }
    }

    const targetItem = document.querySelector(`#bookingDropdownMenu .dropdown-item[data-status="${selectedStatus}"]`);
    if (targetItem) {
        selectBookingFilter(selectedStatus, targetItem);
    } else {
        filterBookings(selectedStatus);
    }
});