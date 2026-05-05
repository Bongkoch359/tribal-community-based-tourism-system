/* ============================================================
   search.js  –  ท่องเที่ยวชุมชนเผ่า
============================================================ */

/* ── Tab switching ─────────────────────────────────────── */
const tabCfg = {
  activity: {
    label: 'ค้นหากิจกรรม',
    placeholder: 'ชื่อกิจกรรม หรือสถานที่',
  },
  tour: {
    label: 'ค้นหาทัวร์ชุมชน',
    placeholder: 'ชื่อทัวร์ หรือสถานที่',
  },
  homestay: {
    label: 'ค้นหาโฮมสเตย์',
    placeholder: 'ชื่อโฮมสเตย์ หรือที่อยู่',
  },
  package: {
    label: 'ค้นหาแพ็กเกจ',
    placeholder: 'ชื่อแพ็กเกจ',
  },
};

function switchTab(type) {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === type);
  });

  const cfg = tabCfg[type] || tabCfg.activity;
  const labelEl = document.getElementById('sf-label-text');
  const inputEl = document.getElementById('keyword-input');
  if (labelEl) labelEl.textContent = cfg.label;
  if (inputEl) inputEl.placeholder = cfg.placeholder;

  const typeInput = document.getElementById('type-input');
  if (typeInput) typeInput.value = type;

  document.querySelectorAll('.section-block').forEach(block => {
    block.classList.toggle('show', block.dataset.type === type);
  });

  const url = new URL(window.location.href);
  url.searchParams.set('type', type);
  window.history.replaceState({}, '', url.toString());
}

/* ── Favourite (heart) toggle ──────────────────────────── */
function toggleFav(btn) {
  btn.classList.toggle('liked');
  const svg = btn.querySelector('svg');
  if (btn.classList.contains('liked')) {
    svg.setAttribute('fill', '#ef4444');
    svg.setAttribute('stroke', '#ef4444');
  } else {
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
  }
}

/* ── Filter chips ──────────────────────────────────────── */
function selectChip(el) {
  el.closest('.filter-chips')
    .querySelectorAll('.chip')
    .forEach(c => c.classList.remove('active'));
  el.classList.add('active');
}

/* ── Guest counter ─────────────────────────────────────── */
function changeGuest(delta) {
  const input = document.getElementById('guestInput');
  if (!input) return;
  let val = parseInt(input.value) || 1;
  val = Math.min(50, Math.max(1, val + delta));
  input.value = val;
}

/* ── User Menu toggle (เมื่อล็อกอินแล้ว) ──────────────── */
function toggleUserMenu() {
  const wrapper = document.getElementById('userMenuWrapper');
  if (wrapper) wrapper.classList.toggle('open');
}

/* ── ปิด User Menu เมื่อคลิกข้างนอก ───────────────────── */
document.addEventListener('click', function (e) {
  const wrapper = document.getElementById('userMenuWrapper');
  if (wrapper && !wrapper.contains(e.target)) {
    wrapper.classList.remove('open');
  }
});

/* ── Restore state from URL on page load ──────────────── */
document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  const type   = params.get('type') || 'activity';
  switchTab(type);
});
