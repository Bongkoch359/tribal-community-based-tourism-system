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
    placeholder: 'ชื่อทัวร์ หรือสถานที่ เช่น อมก๋อย, แม่แจ่ม',
    guestLabel: 'จำนวนผู้เดินทาง',
  },
  homestay: {
    label: 'ค้นหาโฮมสเตย์',
    placeholder: 'ชื่อโฮมสเตย์ หรือสถานที่ เช่น อมก๋อย, ปาย',
    guestLabel: 'จำนวนผู้เข้าพัก',
  },
};

function changeTab(type) { switchTab(type, true); }

/**
 * @param {string} type - 'activity' | 'tour' | 'homestay'
 * @param {boolean} isUserClick - true = ผู้ใช้กด tab เอง (จะซ่อน "รายการแนะนำ")
 *                                false = เรียกตอนโหลดหน้าแรก (จะไม่ยุ่งกับ "รายการแนะนำ")
 */
function switchTab(type, isUserClick = true) {
  // 1. Tab button active
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === type);
  });

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
  //     (ให้เห็นเฉพาะตอนเปิดหน้าแรกเท่านั้น)
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

  // ซ่อนทั้งหมดก่อน
  if (dateTourStart) dateTourStart.style.display = 'none';
  if (dateTourEnd)    dateTourEnd.style.display    = 'none';
  if (checkinBox)     checkinBox.style.display     = 'none';
  if (checkoutBox)    checkoutBox.style.display    = 'none';
  if (guestWrapper)   guestWrapper.style.display   = 'none';
  if (tourTypeBox)    tourTypeBox.style.display    = 'none';

  if (type === 'tour') {
    if (dateTourStart) dateTourStart.style.display = 'flex';
    if (dateTourEnd)    dateTourEnd.style.display    = 'flex';
    if (guestWrapper)   guestWrapper.style.display   = 'flex';
    if (tourTypeBox)    tourTypeBox.style.display    = 'flex';
  } else if (type === 'homestay') {
    // แยก 2 กล่องอิสระ
    if (checkinBox)  checkinBox.style.display  = 'flex';
    if (checkoutBox) checkoutBox.style.display = 'flex';
    if (guestWrapper)guestWrapper.style.display= 'flex';
  }

  // 6. Update URL
  try {
    const url = new URL(window.location.href);
    url.searchParams.set('type', type);
    window.history.replaceState({}, '', url.toString());
  } catch (e) {}
}

/* ── วันที่เดินทาง (Tour): endDate ต้อง >= startDate ── */
function initTourDateValidation() {
  const tourStartInput = document.getElementById('tour-date-input');
  const tourEndInput   = document.getElementById('tour-date-end-input');
  if (!tourStartInput || !tourEndInput) return;

  tourStartInput.addEventListener('change', () => {
    if (tourEndInput.value && tourEndInput.value < tourStartInput.value) {
      tourEndInput.value = '';
    }
    tourEndInput.min = tourStartInput.value;
  });

  if (tourStartInput.value) {
    tourEndInput.min = tourStartInput.value;
  }
}

/* ── วันที่เช็คอิน/เช็คเอาท์ (Homestay): checkout ต้อง >= checkin ── */
function initHomestayDateValidation() {
  const checkinInput  = document.getElementById('checkin-input');
  const checkoutInput = document.getElementById('checkout-input');
  if (!checkinInput || !checkoutInput) return;

  checkinInput.addEventListener('change', () => {
    if (checkoutInput.value && checkoutInput.value < checkinInput.value) {
      checkoutInput.value = '';
    }
    checkoutInput.min = checkinInput.value;
  });

  if (checkinInput.value) {
    checkoutInput.min = checkinInput.value;
  }
}

/* ── Bookmark toggle (แทน heart) ── */
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

/* ── Click ข้างนอกปิด dropdown ทั้งหมด ── */
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

  // ถ้ามีรูปเดียว ไม่ต้องแสดงลูกศร/จุด
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

  // ปัดซ้าย-ขวาบนมือถือ
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

  // isUserClick = false -> ไม่ยุ่งกับ "รายการแนะนำ" ตอนโหลดหน้าแรก
  // (แสดงหรือไม่แสดงถูกกำหนดโดย server ผ่าน th:if อยู่แล้ว)
  switchTab(type, false);

  // ผูก event listener ของ date validation แค่ครั้งเดียวตอนโหลดหน้า
  initTourDateValidation();
  initHomestayDateValidation();

  // Login Dropdown
  const dropdown = document.querySelector('.login-dropdown');
  const btn      = document.querySelector('.btn-login');
  if (btn && dropdown) {
    btn.addEventListener('click', function(e) {
      e.stopPropagation();
      dropdown.classList.toggle('open');
    });
  }
});