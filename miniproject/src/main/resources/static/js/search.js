/* ============================================================
   search.js
============================================================ */

const tabCfg = {
  activity: {
    label: 'ค้นหากิจกรรม',
    placeholder: 'ชื่อกิจกรรม หรือสถานที่',
    guestLabel: 'จำนวนคน',
  },
  tour: {
    label: 'ค้นหาทัวร์ชุมชน',
    placeholder: 'ชื่อทัวร์ หรือสถานที่',
    guestLabel: 'จำนวนผู้เดินทาง',
  },
  homestay: {
    label: 'ค้นหาโฮมสเตย์',
    placeholder: 'ชื่อโฮมสเตย์ หรือที่อยู่',
    guestLabel: 'จำนวนผู้เข้าพัก',
  },
};

function changeTab(type) { switchTab(type); }

function switchTab(type) {
  // 1. Tab button active
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === type);
  });

  // 2. Label + Placeholder
  const cfg = tabCfg[type] || tabCfg.activity;
  const labelEl = document.getElementById('sf-label-text');
  const inputEl = document.getElementById('keyword-input');
  const guestLabelEl = document.getElementById('guest-label-text');
  if (labelEl) labelEl.textContent = cfg.label;
  if (inputEl) inputEl.placeholder = cfg.placeholder;
  if (guestLabelEl) guestLabelEl.textContent = cfg.guestLabel;

  // 3. Hidden type input
  const typeInput = document.getElementById('type-input');
  if (typeInput) typeInput.value = type;

  // 4. Section block
  document.querySelectorAll('.section-block').forEach(block => {
    block.classList.toggle('show', block.dataset.type === type);
  });

  // 5. ✅ Toggle search fields ตาม tab
  const dateTour     = document.getElementById('date-tour-wrapper');
  const dateRange    = document.getElementById('date-range-wrapper');
  const guestWrapper = document.getElementById('guest-wrapper');

  // ซ่อนทั้งหมดก่อน
  if (dateTour)     dateTour.style.display     = 'none';
  if (dateRange)    dateRange.style.display    = 'none';
  if (guestWrapper) guestWrapper.style.display = 'none';

  if (type === 'tour') {
    // Tour: วันที่เดินทาง (1 วัน) + จำนวนผู้เดินทาง
    if (dateTour)     dateTour.style.display     = 'flex';
    if (guestWrapper) guestWrapper.style.display = 'flex';
  } else if (type === 'homestay') {
    // Homestay: วันเข้า-ออก + จำนวนผู้เข้าพัก
    if (dateRange)    dateRange.style.display    = 'flex';
    if (guestWrapper) guestWrapper.style.display = 'flex';
  }
  // activity: ไม่แสดงวันที่และจำนวนคน

  // 6. Sync tour endDate = startDate
  if (type === 'tour') {
    const tourDateInput = document.getElementById('tour-date-input');
    const tourDateEnd   = document.getElementById('tour-date-end-input');
    if (tourDateInput && tourDateEnd) {
      tourDateInput.addEventListener('change', () => {
        tourDateEnd.value = tourDateInput.value;
      });
    }
  }

  // 7. Update URL
  try {
    const url = new URL(window.location.href);
    url.searchParams.set('type', type);
    window.history.replaceState({}, '', url.toString());
  } catch (e) {}
}

/* ── Favourite toggle ── */
function toggleFav(btn) {
  btn.classList.toggle('liked');
  const svg = btn.querySelector('svg');
  if (svg) {
    if (btn.classList.contains('liked')) {
      svg.setAttribute('fill', '#ef4444');
      svg.setAttribute('stroke', '#ef4444');
    } else {
      svg.setAttribute('fill', 'none');
      svg.setAttribute('stroke', 'currentColor');
    }
  }
}

/* ── Filter chips ── */
function selectChip(el) {
  el.closest('.filter-chips')
    .querySelectorAll('.chip')
    .forEach(c => c.classList.remove('active'));
  el.classList.add('active');
}

/* ── Featured filter ── */
function filterFeatured(el) {
  const filter = el.dataset.filter;
  document.querySelectorAll('.feat-chip').forEach(c => c.classList.remove('active'));
  el.classList.add('active');
  document.querySelectorAll('#featuredGrid .fcard').forEach(card => {
    card.style.display = (filter === 'all' || card.dataset.type === filter) ? '' : 'none';
  });
}

/* ── Guest counter ── */
function changeGuest(delta) {
  const input = document.getElementById('guestInput');
  if (!input) return;
  let val = Math.min(50, Math.max(1, (parseInt(input.value) || 1) + delta));
  input.value = val;
}

/* ── User Menu ── */
function toggleUserMenu() {
  const wrapper = document.getElementById('userMenuWrapper');
  if (wrapper) wrapper.classList.toggle('open');
}

document.addEventListener('click', function (e) {
  const wrapper = document.getElementById('userMenuWrapper');
  if (wrapper && !wrapper.contains(e.target)) wrapper.classList.remove('open');
});

/* ── Init ── */
document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  let type = params.get('type');

  if (params.has('managerId') && !type) {
    type = 'tour';
    const featuredAll = document.getElementById('featuredAll');
    if (featuredAll) featuredAll.style.display = 'none';
  }

  if (!type || !tabCfg[type]) type = 'activity';

  switchTab(type);
});