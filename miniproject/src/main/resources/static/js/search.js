/* ============================================================
   search.js  –  ท่องเที่ยวชุมชนเผ่า (ฉบับอัปเดตระบบกรองพรีเมียม)
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
};

// 💡 รองรับทั้งกรณีหน้าบ้านเรียก changeTab หรือ switchTab ให้วิ่งมาที่เดียวกัน
function changeTab(type) {
  switchTab(type);
}

function switchTab(type) {
  // 1. จัดการสถานะปุ่มแท็บ
  document.querySelectorAll('.tab-btn').forEach(btn => {
    // ตรวจสอบว่ามีปุ่มกดส่ง Event มาด้วยไหม ถ้าไม่มีให้เช็กจาก dataset
    const tabName = btn.dataset.tab || (btn.getAttribute('onclick') && btn.getAttribute('onclick').includes(type) ? type : null);
    if (tabName) {
      btn.classList.toggle('active', tabName === type);
    }
  });

  // 2. อัปเดต Label และ Placeholder ของช่องค้นหาหลัก
  const cfg = tabCfg[type] || tabCfg.activity;
  const labelEl = document.getElementById('sf-label-text');
  const inputEl = document.getElementById('keyword-input');
  if (labelEl) labelEl.textContent = cfg.label;
  if (inputEl) inputEl.placeholder = cfg.placeholder;

  // 3. อัปเดตค่าไปยัง hidden input เพื่อส่งให้ Controller หลังบ้านรู้ว่าอยู่แท็บไหน
  // 💡 รองรับทั้ง ID 'type-input' และ 'searchTypeInput' ตามที่เขียนเผื่อไว้ในเวอร์ชันก่อน ๆ
  const typeInput = document.getElementById('type-input') || document.getElementById('searchTypeInput');
  if (typeInput) typeInput.value = type;

  // 4. สลับบล็อกเนื้อหาข้างล่าง (แสดงเฉพาะแท็บที่เลือก)
  document.querySelectorAll('.section-block').forEach(block => {
    block.classList.toggle('show', block.dataset.type === type);
  });

  // 🌟 [อัปเดตใหม่] จัดการเปิด-ปิด ฟิลด์กรองตามเงื่อนไขแท็บ
  const dateSingle = document.getElementById('date-single-wrapper');
  const dateRange = document.getElementById('date-range-wrapper');
  const guestWrapper = document.getElementById('guest-wrapper');

  if (type === 'activity') {
    // ❌ แท็บกิจกรรม: ซ่อนวันที่ออกทั้งหมด และซ่อนช่องจำนวนคน (คลีนที่สุด)
    if (dateSingle) dateSingle.style.display = 'none';
    if (dateRange) dateRange.style.display = 'none';
    if (guestWrapper) guestWrapper.style.display = 'none';
  } else {
    //  แท็บทัวร์ / โฮมสเตย์: แสดงช่วงเวลาเดินทางเริ่ม-สิ้นสุด และจำนวนคน
    if (dateSingle) dateSingle.style.display = 'none';
    if (dateRange) dateRange.style.display = 'block';     
    if (guestWrapper) guestWrapper.style.display = 'block'; 
  }

  // 5. อัปเดต Parameter บนแถบ URL โดยไม่ทำให้หน้าเว็บรีโหลด
  try {
    const url = new URL(window.location.href);
    url.searchParams.set('type', type);
    window.history.replaceState({}, '', url.toString());
  } catch (e) {
    console.log("URL state update skipped");
  }
}

/* ── Favourite (heart) toggle ──────────────────────────── */
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
  let type = params.get('type');
  
  if (params.has('managerId') && !type) {
    type = 'tour';
    
    // สั่งซ่อนกล่องแนะนำเมื่อมี managerId
    const recommendBox = document.querySelector('.recommend-section') || document.getElementById('featuredAll');
    if (recommendBox) {
      recommendBox.style.display = 'none';
    }
  }
  
  if (!type) {
    type = 'activity';
  }
  
  // เรียกใช้งานฟังก์ชันแท็บเพื่อ Setup ฟิลด์วันที่และแสดงสเตทที่ถูกต้อง
  switchTab(type);
});