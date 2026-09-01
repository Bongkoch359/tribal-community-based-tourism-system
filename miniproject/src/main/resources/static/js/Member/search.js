/* ============================================================
   search.js
============================================================ */

const tabCfg = {
  activity: {
    label: 'ค้นหากิจกรรม',
    placeholder: 'ชื่อกิจกรรม หรือสถานที่ เช่น อมก๋อย, ดอยอินทนนท์',
    guestLabel: 'จำนวนคน',
  },
  tour: {
  label: 'ค้นหาทัวร์ชุมชน',
  placeholder: 'ชื่อทัวร์ เช่น ทัวร์เดินป่าดอยอินทนนท์',  
  guestLabel: 'จำนวนผู้เดินทาง',
},
  homestay: {
    label: 'ค้นหาโฮมสเตย์',
    placeholder: 'ชื่อโฮมสเตย์ หรือสถานที่ เช่น อมก๋อย, ปาย',
    guestLabel: 'จำนวนผู้เข้าพัก',
  },
};

/* คืนค่าวันนี้ในรูปแบบ yyyy-MM-dd (อิงเวลาเครื่องผู้ใช้) */
function getTodayStr() {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/* ตั้ง min ของช่องวันที่ทั้งหมดให้เป็นวันนี้เสมอ */
function setAllDateMinToday() {
  const today = getTodayStr();
  ['tour-date-input', 'tour-date-end-input', 'checkin-input', 'checkout-input']
    .forEach(id => {
      const el = document.getElementById(id);
      if (el) el.min = today;
    });
}

function changeTab(type) { switchTab(type, true); }

/* ── ล้างค่าฟอร์มค้นหาเมื่อผู้ใช้เปลี่ยนแท็บเอง ── */
function resetSearchFields() {
  const keywordInput = document.getElementById('keyword-input');
  if (keywordInput) keywordInput.value = '';

  ['tour-date-input', 'tour-date-end-input', 'checkin-input', 'checkout-input']
    .forEach(id => {
      const el = document.getElementById(id);
      if (el) { el.value = ''; el.min = getTodayStr(); }
    });

  const guestInput = document.getElementById('guestInput');
  if (guestInput) guestInput.value = 1;

  const tourTypeSelect = document.getElementById('tourtype-select');
  if (tourTypeSelect) tourTypeSelect.value = '';

  const tribeSelect = document.getElementById('tribe-select');
  if (tribeSelect) tribeSelect.value = '';
}

/**
 * @param {string} type - 'activity' | 'tour' | 'homestay'
 * @param {boolean} isUserClick - true = ผู้ใช้กด tab เอง (จะซ่อน "รายการแนะนำ")
 *                                false = เรียกตอนโหลดหน้าแรก
 */
function switchTab(type, isUserClick = true) {
  // 1. Tab button active
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === type);
  });

  if (isUserClick) {
    resetSearchFields();
  }

  // 2. Label + Placeholder
  const cfg = tabCfg[type] || tabCfg.activity;
  const labelEl      = document.getElementById('sf-label-text');
  const inputEl      = document.getElementById('keyword-input');
  const guestLabelEl = document.getElementById('guest-label-text');
  if (labelEl)      labelEl.textContent      = cfg.label;
  if (inputEl)      inputEl.placeholder      = cfg.placeholder;
  if (guestLabelEl) guestLabelEl.textContent = cfg.guestLabel;

  // 3. Hidden type input
  const typeInput = document.getElementById('type-input');
  if (typeInput) typeInput.value = type;

  // 4. Section block
  document.querySelectorAll('.section-block').forEach(block => {
    block.classList.toggle('show', block.dataset.type === type);
  });

  // 4.1 ซ่อน "รายการแนะนำ" ทันทีที่ผู้ใช้กด tab เอง
  if (isUserClick) {
    const featuredAll = document.getElementById('featuredAll');
    if (featuredAll) featuredAll.style.display = 'none';
  }

  // 5. Toggle search fields ตาม tab
  const dateTourStart = document.getElementById('date-tour-start-wrapper');
  const dateTourEnd    = document.getElementById('date-tour-end-wrapper');
  const checkinBox    = document.getElementById('checkin-wrapper');
  const checkoutBox   = document.getElementById('checkout-wrapper');
  const guestWrapper  = document.getElementById('guest-wrapper');
  const tourTypeBox   = document.getElementById('tourtype-wrapper');
  const tribeBox      = document.getElementById('tribe-wrapper');
  const keywordBox    = document.getElementById('keyword-wrapper'); // [NEW]

  function setBoxState(box, show) {
    if (!box) return;
    box.style.display = show ? 'flex' : 'none';
    box.querySelectorAll('input, select').forEach(el => {
      el.disabled = !show;
    });
  }

  // ซ่อนทั้งหมดก่อน (และ disable ไปด้วย)
  setBoxState(dateTourStart, false);
  setBoxState(dateTourEnd, false);
  setBoxState(checkinBox, false);
  setBoxState(checkoutBox, false);
  setBoxState(guestWrapper, false);
  setBoxState(tourTypeBox, false);
  setBoxState(tribeBox, false);

  // [NEW] keyword โชว์เป็นค่า default เสมอ ยกเว้นแท็บ tour
  setBoxState(keywordBox, true);

  if (type === 'tour') {
    setBoxState(dateTourStart, true);
    setBoxState(dateTourEnd, true);
    setBoxState(guestWrapper, true);
    setBoxState(tourTypeBox, true);
    setBoxState(tribeBox, true);
    setBoxState(keywordBox, false); // [NEW] ซ่อน + disable keyword เฉพาะแท็บ tour
  } else if (type === 'homestay') {
    setBoxState(checkinBox, true);
    setBoxState(checkoutBox, true);
    setBoxState(guestWrapper, true);
  }

  // กันไว้อีกชั้น: ทุกครั้งที่สลับแท็บ ให้ min ของช่องวันที่เป็นวันนี้เสมอ
  setAllDateMinToday();

  // 6. Update URL
  try {
    const url = new URL(window.location.href);
    url.searchParams.set('type', type);
    window.history.replaceState({}, '', url.toString());
  } catch (e) {}
}

/* ── วันที่เดินทาง (Tour): endDate ต้อง >= startDate และห้ามย้อนหลัง ── */
function initTourDateValidation() {
  const tourStartInput = document.getElementById('tour-date-input');
  const tourEndInput   = document.getElementById('tour-date-end-input');
  if (!tourStartInput || !tourEndInput) return;

  const today = getTodayStr();
  tourStartInput.min = today;
  tourEndInput.min   = tourStartInput.value && tourStartInput.value > today
    ? tourStartInput.value
    : today;

  if (tourStartInput.value && tourStartInput.value < today) {
    tourStartInput.value = '';
  }
  if (tourEndInput.value && tourEndInput.value < today) {
    tourEndInput.value = '';
  }

  tourStartInput.addEventListener('change', () => {
    const t = getTodayStr();
    if (tourStartInput.value && tourStartInput.value < t) {
      tourStartInput.value = '';
      alert('ไม่สามารถเลือกวันที่ย้อนหลังได้');
      return;
    }
    if (tourEndInput.value && tourEndInput.value < tourStartInput.value) {
      tourEndInput.value = '';
    }
    tourEndInput.min = tourStartInput.value || t;
  });

  tourEndInput.addEventListener('change', () => {
    const t = getTodayStr();
    if (tourEndInput.value && tourEndInput.value < t) {
      tourEndInput.value = '';
      alert('ไม่สามารถเลือกวันที่ย้อนหลังได้');
      return;
    }
    if (tourStartInput.value && tourEndInput.value && tourEndInput.value < tourStartInput.value) {
      alert('วันที่สิ้นสุดต้องไม่มาก่อนวันที่เริ่มเดินทาง');
      tourEndInput.value = '';
    }
  });
}

/* ── วันที่เช็คอิน/เช็คเอาท์ (Homestay): checkout ต้อง >= checkin และห้ามย้อนหลัง ── */
function initHomestayDateValidation() {
  const checkinInput  = document.getElementById('checkin-input');
  const checkoutInput = document.getElementById('checkout-input');
  if (!checkinInput || !checkoutInput) return;

  const today = getTodayStr();
  checkinInput.min  = today;
  checkoutInput.min = checkinInput.value && checkinInput.value > today
    ? checkinInput.value
    : today;

  if (checkinInput.value && checkinInput.value < today) {
    checkinInput.value = '';
  }
  if (checkoutInput.value && checkoutInput.value < today) {
    checkoutInput.value = '';
  }

  checkinInput.addEventListener('change', () => {
    const t = getTodayStr();
    if (checkinInput.value && checkinInput.value < t) {
      checkinInput.value = '';
      alert('ไม่สามารถเลือกวันที่ย้อนหลังได้');
      return;
    }
    if (checkoutInput.value && checkoutInput.value < checkinInput.value) {
      checkoutInput.value = '';
    }
    checkoutInput.min = checkinInput.value || t;
  });

  checkoutInput.addEventListener('change', () => {
    const t = getTodayStr();
    if (checkoutInput.value && checkoutInput.value < t) {
      checkoutInput.value = '';
      alert('ไม่สามารถเลือกวันที่ย้อนหลังได้');
      return;
    }
    if (checkinInput.value && checkoutInput.value && checkoutInput.value < checkinInput.value) {
      alert('วันที่เช็คเอาท์ต้องไม่มาก่อนวันที่เช็คอิน');
      checkoutInput.value = '';
    }
  });
}

/* ── ป้องกันชั้นสุดท้ายตอน submit ฟอร์ม ── */
function initSearchFormSubmitGuard() {
  const form = document.getElementById('searchForm');
  if (!form) return;

  form.addEventListener('submit', function (e) {
    const today = getTodayStr();

    const dateInputs = form.querySelectorAll('input[type="date"]:not([disabled])');
    for (const input of dateInputs) {
      if (input.value && input.value < today) {
        e.preventDefault();
        alert('กรุณาเลือกวันที่ตั้งแต่วันนี้เป็นต้นไป');
        input.focus();
        return;
      }
    }

    const tourStartInput = document.getElementById('tour-date-input');
    const tourEndInput   = document.getElementById('tour-date-end-input');
    const checkinInput   = document.getElementById('checkin-input');
    const checkoutInput  = document.getElementById('checkout-input');

    const pairs = [
      [tourStartInput, tourEndInput, 'วันที่สิ้นสุดต้องไม่มาก่อนวันที่เริ่มเดินทาง'],
      [checkinInput, checkoutInput, 'วันที่เช็คเอาท์ต้องไม่มาก่อนวันที่เช็คอิน'],
    ];

    for (const [start, end, msg] of pairs) {
      if (start && end && !start.disabled && !end.disabled &&
          start.value && end.value && end.value < start.value) {
        e.preventDefault();
        alert(msg);
        return;
      }
    }
  });
}

/* ── Bookmark toggle ── */
function toggleFav(btn) {
  btn.classList.toggle('saved');
  const svg = btn.querySelector('svg');
  if (svg) {
    if (btn.classList.contains('saved')) {
      svg.setAttribute('fill', '#2d7a4f');
      svg.setAttribute('stroke', '#2d7a4f');
      btn.setAttribute('title', 'บันทึกแล้ว');
    } else {
      svg.setAttribute('fill', 'none');
      svg.setAttribute('stroke', 'currentColor');
      btn.setAttribute('title', 'บันทึกรายการโปรด');
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

document.addEventListener('click', function(e) {
  const wrapper = document.getElementById('userMenuWrapper');
  if (wrapper && !wrapper.contains(e.target)) wrapper.classList.remove('open');

  const dropdown = document.querySelector('.login-dropdown');
  if (dropdown && !dropdown.contains(e.target)) dropdown.classList.remove('open');
});

/* ── Hero image slider ── */
let heroIndex = 0;
let heroTimer = null;

function heroGoTo(i) {
  const slides = document.querySelectorAll('#heroSlider .hero-slide');
  const dots   = document.querySelectorAll('#heroDots .hero-dot');
  if (!slides.length) return;
  heroIndex = (i + slides.length) % slides.length;
  slides.forEach((s, idx) => s.classList.toggle('active', idx === heroIndex));
  dots.forEach((d, idx) => d.classList.toggle('active', idx === heroIndex));
}

function heroSlide(delta) {
  heroGoTo(heroIndex + delta);
  restartHeroAutoplay();
}

function startHeroAutoplay() {
  const slides = document.querySelectorAll('#heroSlider .hero-slide');
  if (slides.length <= 1) return;
  heroTimer = setInterval(() => heroGoTo(heroIndex + 1), 5500);
}

function restartHeroAutoplay() {
  if (heroTimer) clearInterval(heroTimer);
  startHeroAutoplay();
}

function initHeroSlider() {
  const slides   = document.querySelectorAll('#heroSlider .hero-slide');
  const dotsWrap = document.getElementById('heroDots');
  const arrows   = document.querySelectorAll('.hero-arrow');
  if (!slides.length || !dotsWrap) return;

  if (slides.length <= 1) {
    arrows.forEach(a => a.style.display = 'none');
    dotsWrap.style.display = 'none';
    return;
  }

  dotsWrap.innerHTML = '';
  slides.forEach((_, idx) => {
    const dot = document.createElement('button');
    dot.type = 'button';
    dot.className = 'hero-dot' + (idx === 0 ? ' active' : '');
    dot.setAttribute('aria-label', 'ไปที่รูปที่ ' + (idx + 1));
    dot.addEventListener('click', () => { heroGoTo(idx); restartHeroAutoplay(); });
    dotsWrap.appendChild(dot);
  });

  const heroEl = document.querySelector('.hero');
  if (heroEl) {
    let touchStartX = 0;
    heroEl.addEventListener('touchstart', e => { touchStartX = e.changedTouches[0].screenX; }, { passive: true });
    heroEl.addEventListener('touchend', e => {
      const diff = e.changedTouches[0].screenX - touchStartX;
      if (Math.abs(diff) > 40) heroSlide(diff < 0 ? 1 : -1);
    }, { passive: true });
  }

  startHeroAutoplay();
}

/* ── Init ── */
document.addEventListener('DOMContentLoaded', () => {
  initHeroSlider();
  const params = new URLSearchParams(window.location.search);
  let type = params.get('type');

  if (!type || !tabCfg[type]) type = 'activity';

  switchTab(type, false);

  setAllDateMinToday();

  initTourDateValidation();
  initHomestayDateValidation();

  initSearchFormSubmitGuard();

  const dropdown = document.querySelector('.login-dropdown');
  const btn      = document.querySelector('.btn-login');
  if (btn && dropdown) {
    btn.addEventListener('click', function(e) {
      e.stopPropagation();
      dropdown.classList.toggle('open');
    });
  }
});