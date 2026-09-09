
// คำค้นหาปัจจุบัน (ตัวพิมพ์เล็กเสมอ เพื่อให้ค้นหาแบบไม่สนตัวพิมพ์เล็ก-ใหญ่)
let currentBookingSearchTerm = '';

// หน่วงเวลาก่อนค้นหา (debounce) กันเรียกฟังก์ชันถี่เกินไปตอนพิมพ์เร็วๆ
let bookingSearchDebounceTimer = null;
const BOOKING_SEARCH_DEBOUNCE_MS = 200;

/**
 * เรียกเมื่อผู้ใช้พิมพ์ในช่องค้นหา (oninput)
 */
function onBookingSearchInput() {
    const input = document.getElementById('bookingSearchInput');
    if (!input) return;

    clearTimeout(bookingSearchDebounceTimer);
    bookingSearchDebounceTimer = setTimeout(() => {
        currentBookingSearchTerm = input.value.trim().toLowerCase();
        toggleSearchClearButton();
        applyBookingFilters();
    }, BOOKING_SEARCH_DEBOUNCE_MS);
}

/**
 * ล้างคำค้นหา (ปุ่ม X ในช่องค้นหา)
 */
function clearBookingSearch() {
    const input = document.getElementById('bookingSearchInput');
    if (input) input.value = '';

    currentBookingSearchTerm = '';
    toggleSearchClearButton();
    applyBookingFilters();

    if (input) input.focus();
}

/**
 * แสดง/ซ่อนปุ่ม X ล้างคำค้นหา ตามว่ามีคำค้นหาอยู่หรือไม่
 */
function toggleSearchClearButton() {
    const clearBtn = document.getElementById('bookingSearchClear');
    if (clearBtn) {
        clearBtn.style.display = currentBookingSearchTerm ? 'flex' : 'none';
    }
}

/**
 * ฟังก์ชันกลาง: กรองแถวในตารางโดยรวมทั้ง 2 เงื่อนไขเข้าด้วยกัน
 *   1) สถานะที่เลือกจาก dropdown (currentBookingStatus ที่ประกาศในหน้า listBooking.html)
 *   2) คำค้นหาจากช่อง search (currentBookingSearchTerm)
 *
 * ฟังก์ชันนี้ถูกออกแบบให้ filterBookings() ในหน้า listBooking.html
 * เรียกใช้แทนการกรองสถานะอย่างเดียว
 */
function applyBookingFilters() {
    const rows = document.querySelectorAll('#bookingsTbody tr[data-status]');
    const noResults = document.getElementById('noFilterResults');
    let visibleCount = 0;

    // ตัวแปร currentBookingStatus มาจาก inline script ของ listBooking.html
    // เผื่อไฟล์นั้นยังไม่โหลด ให้ไม่กรองสถานะ
    const statusKey = (typeof currentBookingStatus !== 'undefined' && currentBookingStatus)
        ? currentBookingStatus
        : null;

    rows.forEach(row => {
        const matchStatus = !statusKey || row.dataset.status === statusKey;

        const searchable = (row.getAttribute('data-search') || '').toLowerCase();
        const matchSearch = !currentBookingSearchTerm || searchable.includes(currentBookingSearchTerm);

        const isVisible = matchStatus && matchSearch;
        row.style.display = isVisible ? '' : 'none';
        if (isVisible) visibleCount++;
    });

    updateNoResultsMessage(noResults, visibleCount, rows.length);
}

/**
 * แสดง/ซ่อนข้อความ "ไม่พบรายการที่ตรงกับเงื่อนไข" และปรับข้อความ
 * ให้เหมาะสมกับกรณีที่กำลังค้นหาด้วย เพื่อให้ผู้ใช้เข้าใจว่าทำไมไม่เจอ
 */
function updateNoResultsMessage(noResultsEl, visibleCount, totalRows) {
    if (!noResultsEl) return;

    const shouldShow = (visibleCount === 0 && totalRows > 0);
    noResultsEl.style.display = shouldShow ? 'block' : 'none';

    if (shouldShow) {
        const textEl = noResultsEl.querySelector('p');
        if (textEl) {
            textEl.textContent = currentBookingSearchTerm
                ? `ไม่พบรายการที่ตรงกับคำค้นหา "${currentBookingSearchTerm}"`
                : 'ไม่พบรายการที่ตรงกับสถานะที่เลือก';
        }
    }
}