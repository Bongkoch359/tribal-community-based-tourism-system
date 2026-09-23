
// คำค้นหาปัจจุบัน 
let currentBookingSearchTerm = '';

// หน่วงเวลาก่อนค้นหา 
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

function applyBookingFilters() {
    const rows = document.querySelectorAll('#bookingsTbody tr[data-status]');
    const noResults = document.getElementById('noFilterResults');
    let visibleCount = 0;

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