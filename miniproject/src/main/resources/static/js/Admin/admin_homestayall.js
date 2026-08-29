// ══════════════════════════════════════════
// admin_homestayall.js
// หน้า: /admin/homestay/all — บัญชีโฮมสเตย์ทั้งหมด
// ══════════════════════════════════════════

// ── ตัวแปรเก็บ owner ที่กำลังเปิด modal อยู่ (ใช้ตอนกดยืนยันระงับ) ──
let currentSuspendOwnerId = null;
let currentActivateOwnerId = null;

// สถานะ report ที่ใช้ map เป็น label/class ภาษาไทย
const REPORT_STATUS_MAP = {
  PENDING:  { label: 'รอดำเนินการ', cssClass: 'ris-pending' },
  RESOLVED: { label: 'ดำเนินการแล้ว', cssClass: 'ris-resolved' },
  REJECTED: { label: 'ปฏิเสธแล้ว',   cssClass: 'ris-rejected' },
};

document.addEventListener('DOMContentLoaded', () => {
  initFilterPills();
  initSuspendConfirmButton();
  initActivateConfirmButton();
  initPageNavDropdown();

  // ปิด modal เมื่อคลิกพื้นหลัง (นอกกล่อง)
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) closeModal(overlay.id);
    });
  });
});

// ══════════════════════════════════════════
// Modal เปิด/ปิด (ใช้ร่วมกันทั้ง activateModal และ reportDetailModal)
// ══════════════════════════════════════════
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('show');
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('show');
}

// ══════════════════════════════════════════
// เปิดใช้งานบัญชี (activate)
// ══════════════════════════════════════════
function openActivateModal(btn) {
  currentActivateOwnerId = btn.dataset.id;
  document.getElementById('activateOwnerName').textContent = btn.dataset.name || '-';
  openModal('activateModal');
}

function initActivateConfirmButton() {
  const btn = document.getElementById('activateConfirmBtn');
  if (!btn) return;

  btn.addEventListener('click', () => {
    if (!currentActivateOwnerId) return;

    const form = document.getElementById('activateForm');
    form.action = `/admin/homestay/activate/${currentActivateOwnerId}`;
    form.submit();
  });
}

// ══════════════════════════════════════════
// เปิด modal รายละเอียดรายงาน + โหลดข้อมูลผ่าน AJAX
// ══════════════════════════════════════════
function openReportModal(btn) {
  const ownerId    = btn.dataset.id;
  const targetName = btn.dataset.target;
  const ownerName  = btn.dataset.owner;

  currentSuspendOwnerId = ownerId;

  document.getElementById('rmTargetName').textContent = targetName || '-';
  document.getElementById('rmOwnerName').textContent  = ownerName || '-';

  // เคลียร์ค่าเก่าทุกครั้งที่เปิด modal ใหม่
  document.getElementById('rmSuspendReasonInput').value = '';
  document.getElementById('rmSummaryRow').innerHTML = '';
  document.getElementById('rmReportList').innerHTML = '';
  document.getElementById('rmEmptyState').style.display = 'none';
  updateSuspendButtonState();

  openModal('reportDetailModal');

  fetch(`/api/admin/reports/by-owner/${encodeURIComponent(ownerId)}`)
    .then(res => {
      if (!res.ok) throw new Error('โหลดข้อมูลรายงานไม่สำเร็จ');
      return res.json();
    })
    .then(reports => renderReportModal(reports))
    .catch(err => {
      console.error(err);
      document.getElementById('rmReportList').innerHTML =
        '<div class="report-empty">ไม่สามารถโหลดข้อมูลรายงานได้ กรุณาลองใหม่อีกครั้ง</div>';
    });
}

// รับ List<ReportListItemDto> จาก /api/admin/reports/by-owner/{id} มา render
function renderReportModal(reports) {
  renderSummary(reports);

  const listContainer = document.getElementById('rmReportList');
  const emptyState = document.getElementById('rmEmptyState');
  listContainer.innerHTML = '';

  if (!reports || reports.length === 0) {
    emptyState.style.display = 'block';
    return;
  }
  emptyState.style.display = 'none';

  // จัดกลุ่มตามรายการที่ถูกรายงาน (targetType + targetId) เพราะ owner คนหนึ่งมีได้หลายโฮมสเตย์
  const groups = groupReportsByTarget(reports);

  groups.forEach((group, idx) => {
    listContainer.appendChild(buildGroupCard(group, idx));
  });
}

// สร้าง summary chip 3 ช่อง: รอดำเนินการ / ดำเนินการแล้ว / ปฏิเสธแล้ว
function renderSummary(reports) {
  const counts = { PENDING: 0, RESOLVED: 0, REJECTED: 0 };
  (reports || []).forEach(r => {
    if (counts[r.status] !== undefined) counts[r.status]++;
  });

  const row = document.getElementById('rmSummaryRow');
  row.innerHTML = '';

  const chipsConfig = [
    { key: 'PENDING',  label: 'รอดำเนินการ' },
    { key: 'RESOLVED', label: 'ดำเนินการแล้ว' },
    { key: 'REJECTED', label: 'ปฏิเสธแล้ว' },
  ];

  chipsConfig.forEach(cfg => {
    const num = counts[cfg.key] || 0;
    const chip = document.createElement('div');
    chip.className = 'rm-summary-chip' + (num > 0 ? ' has-value' : '');
    chip.innerHTML = `
      <div class="rm-summary-num">${num}</div>
      <div class="rm-summary-label">${cfg.label}</div>
    `;
    row.appendChild(chip);
  });
}

// จัดกลุ่ม report ตาม target (โฮมสเตย์/ทัวร์ที่ถูกรายงาน)
function groupReportsByTarget(reports) {
  const map = new Map();

  reports.forEach(r => {
    const key = `${r.targetType}-${r.targetId}`;
    if (!map.has(key)) {
      map.set(key, {
        targetType: r.targetType,
        targetName: r.targetName || '-',
        items: [],
      });
    }
    map.get(key).items.push(r);
  });

  return Array.from(map.values());
}

// สร้างการ์ดกลุ่ม (พับ/กางได้) 1 การ์ด ต่อ 1 รายการที่ถูกรายงาน
function buildGroupCard(group, idx) {
  const card = document.createElement('div');
  card.className = 'rm-group-card';
  card.id = `rmGroup-${idx}`;

  const typeLabel = group.targetType === 'TOUR' ? 'ทัวร์' : 'โฮมสเตย์';

  const top = document.createElement('div');
  top.className = 'rm-group-top';
  top.innerHTML = `
    <div>
      <div class="rm-group-name">${escapeHtml(group.targetName)}</div>
      <div class="rm-group-type">${typeLabel}</div>
    </div>
    <div style="display:flex; align-items:center; gap:8px;">
      <span class="rm-group-count">${group.items.length} รายงาน</span>
      <span class="material-symbols-outlined rm-group-toggle">expand_more</span>
    </div>
  `;
  top.addEventListener('click', () => card.classList.toggle('open'));

  const detail = document.createElement('div');
  detail.className = 'rm-group-detail';
  group.items.forEach(item => detail.appendChild(buildReportItem(item)));

  card.appendChild(top);
  card.appendChild(detail);

  // การ์ดแรกกางไว้ตั้งแต่แรก ให้เห็นรายละเอียดทันที
  if (idx === 0) card.classList.add('open');

  return card;
}

// สร้าง 1 รายการรายงาน (การ์ดเล็กในกลุ่ม)
function buildReportItem(item) {
  const statusInfo = REPORT_STATUS_MAP[item.status] || { label: item.status, cssClass: 'ris-pending' };

  const el = document.createElement('div');
  el.className = 'report-item';
  el.innerHTML = `
    <div class="report-item-top">
      <div class="report-item-reason">${escapeHtml(item.reason || '-')}</div>
      <div class="report-item-date">${escapeHtml(item.createdAt || '')}</div>
    </div>
    <div class="report-item-desc">${escapeHtml(item.description || '')}</div>
    <div class="report-item-footer">
      <span class="report-item-status ${statusInfo.cssClass}">${statusInfo.label}</span>
    </div>
  `;
  return el;
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}

// ══════════════════════════════════════════
// ปุ่ม "ยืนยันระงับบัญชี" ใน modal รายละเอียดรายงาน
// ══════════════════════════════════════════
function initSuspendConfirmButton() {
  const textarea = document.getElementById('rmSuspendReasonInput');
  const btn = document.getElementById('rmSuspendConfirmBtn');
  if (!textarea || !btn) return;

  textarea.addEventListener('input', updateSuspendButtonState);

  // ขั้นที่ 1: กด "ยืนยันระงับบัญชี" ใน modal รายละเอียด → เปิด modal ยืนยันซ้อน
  btn.addEventListener('click', () => {
    const reason = textarea.value.trim();
    if (!currentSuspendOwnerId || !reason) return;

    document.getElementById('scOwnerName').textContent =
      document.getElementById('rmOwnerName').textContent || '-';
    document.getElementById('scReasonPreview').textContent = reason;

    openModal('suspendConfirmModal');
  });

  // ขั้นที่ 2: กด "ยืนยันการระงับ" ใน modal ยืนยันซ้อน → submit form จริง
  const finalBtn = document.getElementById('suspendFinalConfirmBtn');
  if (finalBtn) {
    finalBtn.addEventListener('click', () => {
      const reason = textarea.value.trim();
      if (!currentSuspendOwnerId || !reason) return;

      const form = document.getElementById('suspendForm');
      document.getElementById('hiddenReasonInput').value = reason;
      form.action = `/admin/homestay/suspend/${currentSuspendOwnerId}`;
      form.submit();
    });
  }
}

// ปิดปุ่มยืนยันไว้ก่อน จนกว่าจะพิมพ์เหตุผลระงับ
function updateSuspendButtonState() {
  const textarea = document.getElementById('rmSuspendReasonInput');
  const btn = document.getElementById('rmSuspendConfirmBtn');
  if (!textarea || !btn) return;
  btn.disabled = textarea.value.trim().length === 0;
}

// ══════════════════════════════════════════
// Filter pills: ทั้งหมด / เปิดใช้งานอยู่ / ถูกระงับ
// ══════════════════════════════════════════
function initFilterPills() {
  const dropdown = document.getElementById('statusFilterDropdown');
  const options = document.querySelectorAll('.sf-option');
  const rows = document.querySelectorAll('table tbody tr[data-status]');

  options.forEach(opt => {
    opt.addEventListener('click', (e) => {
      e.preventDefault();
      const filter = opt.dataset.filter;

      options.forEach(o => o.classList.remove('active'));
      opt.classList.add('active');

      // อัปเดตข้อความ/ไอคอนบนปุ่มหลักให้ตรงกับตัวที่เลือก
      document.getElementById('sfTriggerText').textContent = opt.textContent.split('(')[0].trim();
      document.getElementById('sfTriggerCount').textContent =
        opt.textContent.match(/\((\d+)\)/)?.[1] ?? '0';
      document.getElementById('sfTriggerIcon').textContent = opt.dataset.icon || 'check_circle';

      rows.forEach(row => {
        const status = row.dataset.status;
        const show = filter === 'all' || status === filter;
        row.style.display = show ? '' : 'none';
      });

      dropdown.classList.remove('open');
    });
  });

  // ปิด dropdown เมื่อคลิกนอกกล่อง
  document.addEventListener('click', (e) => {
    if (!dropdown.contains(e.target)) dropdown.classList.remove('open');
  });
}

function toggleStatusFilterMenu() {
  document.getElementById('statusFilterDropdown').classList.toggle('open');
}

// ══════════════════════════════════════════
// Dropdown: เปลี่ยนหน้า (มุมมอง)
// ══════════════════════════════════════════
function togglePageNavMenu(e) {
  e.stopPropagation();
  document.getElementById('pageNavDropdown').classList.toggle('open');
}

function initPageNavDropdown() {
  const nav = document.getElementById('pageNavDropdown');
  if (!nav) return;

  const activeOpt = nav.querySelector('.page-nav-option.active');
  if (activeOpt) {
    document.getElementById('pageNavTriggerText').textContent = activeOpt.dataset.label;
    document.getElementById('pageNavTriggerIcon').textContent = activeOpt.dataset.icon;
  }

  document.addEventListener('click', (e) => {
    if (!nav.contains(e.target)) nav.classList.remove('open');
  });
}

