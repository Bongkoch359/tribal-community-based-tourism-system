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

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) closeModal(overlay.id);
    });
  });
});
// ══════════════════════════════════════════
// Modal เปิด/ปิด (ใช้ร่วมกันทุก modal ในหน้านี้)
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
  const email      = btn.dataset.email;
  const phone      = btn.dataset.phone;
  const address    = btn.dataset.address;
  const status     = btn.dataset.status; // เช่น 'SUSPENDED' หรือค่าอื่นที่ถือว่าปกติ

  currentSuspendOwnerId = ownerId;

  // ── หัว modal: avatar / ชื่อ / ไอดี / badge สถานะ ──
  document.getElementById('rmOwnerName').textContent = ownerName || '-';
  document.getElementById('rmOwnerId').textContent   = ownerId ? `ID: ${ownerId}` : '-';
  document.getElementById('rmAvatar').textContent     = getInitials(ownerName);

  const statusBadge = document.getElementById('rmStatusBadge');
  const isSuspended = status === 'SUSPENDED';
  statusBadge.textContent = isSuspended ? 'ถูกระงับ' : 'ใช้งานปกติ';
  statusBadge.classList.toggle('suspended', isSuspended);

  // ── กล่องข้อมูลติดต่อ ──
  document.getElementById('rmAddress').textContent = address && address !== '-' ? address : 'ไม่มีข้อมูลที่อยู่';
  document.getElementById('rmEmail').textContent   = email || '-';
  document.getElementById('rmPhone').textContent   = phone || '-';

  // ── รีเซ็ตฟอร์มระงับให้กลับไปเป็นปุ่มเดียวทุกครั้งที่เปิด modal ใหม่ ──
  const reasonInput = document.getElementById('rmSuspendReasonInput');
  const reasonError = document.getElementById('rmReasonError');
  reasonInput.value = '';
  reasonInput.classList.remove('invalid');
  if (reasonError) reasonError.classList.remove('show');
  document.getElementById('rmSuspendForm').classList.remove('show');
  document.getElementById('rmSuspendTriggerBtn').style.display = '';

  document.getElementById('rmSummaryRow').innerHTML = '';
  document.getElementById('rmReportList').innerHTML = '';
  document.getElementById('rmEmptyState').style.display = 'none';

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

// เอาอักษรแรกของชื่อ-นามสกุลมาทำเป็นตัวย่อ avatar เช่น "รานี วันดี" -> "รว"
function getInitials(fullName) {
  if (!fullName) return '-';
  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].substring(0, 2);
  return (parts[0][0] || '') + (parts[1][0] || '');
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

// สรุป 2 ช่อง: จำนวนการรายงานทั้งหมด / จำนวนโฮมสเตย์ที่ถูกรายงาน (เหมือนหน้าทัวร์)
function renderSummary(reports) {
  const list = reports || [];
  const totalReports = list.length;
  const groupCount = groupReportsByTarget(list).length;

  const row = document.getElementById('rmSummaryRow');
  row.innerHTML = '';

  const chipsConfig = [
    { num: totalReports, label: 'การรายงานทั้งหมด' },
    { num: groupCount,   label: 'โฮมสเตย์ที่ถูกรายงาน' },
  ];

  chipsConfig.forEach(cfg => {
    const chip = document.createElement('div');
    chip.className = 'rm-summary-chip' + (cfg.num > 0 ? ' has-value' : '');
    chip.innerHTML = `
      <div class="rm-summary-num">${cfg.num}</div>
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
      <span class="rm-group-count">ถูกรายงาน ${group.items.length} ครั้ง</span>
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

// สร้าง 1 รายการรายงาน (การ์ดเล็กในกลุ่ม) — มีปุ่ม "ดูหลักฐาน" ถ้ามีรูปแนบ
function buildReportItem(item) {
  const statusInfo = REPORT_STATUS_MAP[item.status] || { label: item.status, cssClass: 'ris-pending' };
  const hasEvidence = !!item.evidenceImage;

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
      ${hasEvidence ? `
        <button type="button" class="btn-view-evidence">
          <span class="material-symbols-outlined">image</span> ดูหลักฐาน
        </button>` : ''}
    </div>
  `;

  if (hasEvidence) {
    el.querySelector('.btn-view-evidence').addEventListener('click', () => {
      openEvidenceModal(item.evidenceImage);
    });
  }

  return el;
}

// เปิด modal แสดงรูปหลักฐานของรายงานที่เลือก
function openEvidenceModal(imageUrl) {
  const body = document.getElementById('evidenceModalBody');
  body.innerHTML = '';

  if (!imageUrl) {
    body.innerHTML = '<div class="evidence-modal-empty">ไม่มีรูปหลักฐานแนบมากับรายงานนี้</div>';
  } else {
    const img = document.createElement('img');
    img.src = imageUrl;
    img.alt = 'รูปหลักฐานการรายงาน';
    body.appendChild(img);
  }

  openModal('evidenceImageModal');
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}

// ══════════════════════════════════════════
// ปุ่มระงับบัญชี: กดได้เสมอ (ไม่ disable) — พอกด "ยืนยันระงับบัญชี"
// ค่อยเช็คว่ากรอกเหตุผลหรือยัง ถ้ายัง -> ขึ้นกรอบแดง + ข้อความเตือนใต้ช่อง
// เหมือนหน้าจองทัวร์ ถ้ากรอกแล้วค่อยเปิด modal ยืนยันซ้อนต่อ
// ══════════════════════════════════════════
function initSuspendConfirmButton() {
  const triggerBtn = document.getElementById('rmSuspendTriggerBtn');
  const suspendForm = document.getElementById('rmSuspendForm');
  const cancelBtn = document.getElementById('rmSuspendCancelBtn');
  const textarea = document.getElementById('rmSuspendReasonInput');
  const errorEl = document.getElementById('rmReasonError');
  const btn = document.getElementById('rmSuspendConfirmBtn');
  if (!textarea || !btn || !triggerBtn || !suspendForm) return;

  // ขั้นที่ 0: กดปุ่มแดงแรก -> ซ่อนปุ่มนั้น แล้วกางฟอร์มเหตุผลแทน
  triggerBtn.addEventListener('click', () => {
    triggerBtn.style.display = 'none';
    suspendForm.classList.add('show');
    textarea.focus();
  });

  // กดยกเลิกในฟอร์ม -> พับฟอร์มกลับ แสดงปุ่มแดงเหมือนเดิม และล้าง error
  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      textarea.value = '';
      textarea.classList.remove('invalid');
      if (errorEl) errorEl.classList.remove('show');
      suspendForm.classList.remove('show');
      triggerBtn.style.display = '';
    });
  }

  // เคลียร์ error ทันทีที่ผู้ใช้เริ่มพิมพ์ (ไม่ยุ่งกับ disabled ของปุ่มอีกต่อไป)
  textarea.addEventListener('input', () => {
    textarea.classList.remove('invalid');
    if (errorEl) errorEl.classList.remove('show');
  });

  // ขั้นที่ 1: กด "ยืนยันระงับบัญชี" — เช็ค validate ตรงนี้แทนการ disable ปุ่มไว้ก่อน
  btn.addEventListener('click', () => {
    const reason = textarea.value.trim();

    if (!reason) {
      textarea.classList.add('invalid');
      if (errorEl) errorEl.classList.add('show');
      textarea.focus();
      return; // ไม่เปิด modal ยืนยัน ถ้ายังไม่กรอกเหตุผล
    }

    textarea.classList.remove('invalid');
    if (errorEl) errorEl.classList.remove('show');

    if (!currentSuspendOwnerId) return;

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