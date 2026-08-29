function filterTab(status, el) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  el.classList.add('active');
  document.querySelectorAll('#tableBody tr[data-status]').forEach(row => {
    if (status === 'all') {
      row.style.display = '';
    } else {
      row.style.display = row.dataset.status === status.toUpperCase() ? '' : 'none';
    }
  });
}

let currentSuspendUrl = "";

const STATUS_LABEL = { RESOLVED: 'ดำเนินการแล้ว', REJECTED: 'ปฏิเสธแล้ว', PENDING: 'รอดำเนินการ' };
const STATUS_CLASS = { RESOLVED: 'ris-resolved', REJECTED: 'ris-rejected', PENDING: 'ris-pending' };

function formatReportDate(raw) {
  if (!raw) return '';
  const d = new Date(raw);
  if (isNaN(d.getTime())) return raw;
  return d.toLocaleString('th-TH', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

// Backend ตอนนี้ส่ง ReportListItemDto (แบบแบน) แทน Report entity เต็มก้อนแล้ว
// field ที่ใช้คือ targetType ("TOUR" / "HOMESTAY"), targetId, targetName
function getTargetName(rp) {
  return rp.targetName || null;
}
function getTargetType(rp) {
  if (rp.targetType === 'TOUR') return 'ทัวร์';
  if (rp.targetType === 'HOMESTAY') return 'โฮมสเตย์';
  return null;
}

// evidenceImage เป็น LONGTEXT — อาจเป็น URL หรือ base64 ดิบ ๆ รองรับทั้งสองแบบ
function buildEvidenceLinkEl(evidenceImage) {
  if (!evidenceImage) return null;
  const isUrlOrDataUri = /^https?:\/\//i.test(evidenceImage) || /^data:image\//i.test(evidenceImage);
  const src = isUrlOrDataUri ? evidenceImage : ('data:image/jpeg;base64,' + evidenceImage);

  const link = document.createElement('a');
  link.href = src;
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  link.style.cssText = 'display:inline-flex; align-items:center; gap:4px; margin-top:8px; margin-left:8px; font-size:12px; font-weight:700; color: var(--primary); text-decoration:none;';
  link.innerHTML = '<span class="material-symbols-outlined" style="font-size:15px;">image</span> ดูหลักฐาน';
  return link;
}

function buildReportItemEl(rp) {
  const status = (rp.status || 'PENDING').toUpperCase();
  const statusClass = STATUS_CLASS[status] || 'ris-pending';
  const statusLabel = STATUS_LABEL[status] || 'รอดำเนินการ';

  const div = document.createElement('div');
  div.className = 'report-item';
  div.innerHTML =
    '<div class="report-item-top">' +
      '<div class="report-item-reason"></div>' +
      '<div class="report-item-date"></div>' +
    '</div>' +
    '<div class="report-item-desc"></div>' +
    '<div class="report-item-footer"></div>';

  div.querySelector('.report-item-reason').textContent = rp.reason || '-';
  div.querySelector('.report-item-date').textContent = formatReportDate(rp.createdAt);
  div.querySelector('.report-item-desc').textContent = rp.description || '-';

  const footer = div.querySelector('.report-item-footer');
  const statusBadge = document.createElement('span');
  statusBadge.className = 'report-item-status ' + statusClass;
  statusBadge.textContent = statusLabel;
  footer.appendChild(statusBadge);

  const evidenceLink = buildEvidenceLinkEl(rp.evidenceImage);
  if (evidenceLink) footer.appendChild(evidenceLink);

  return div;
}

// สร้างการ์ดกลุ่ม (รายการทัวร์/โฮมสเตย์ 1 รายการ + จำนวนครั้งที่ถูกรายงาน + รายละเอียดที่กดขยายได้)
function buildGroupCardEl(name, type, reports) {
  const card = document.createElement('div');
  card.className = 'rm-group-card';

  const top = document.createElement('div');
  top.className = 'rm-group-top';
  top.innerHTML =
    '<div>' +
      '<div class="rm-group-name"></div>' +
      '<div class="rm-group-type"></div>' +
    '</div>' +
    '<div style="display:flex; align-items:center; gap:8px;">' +
      '<span class="rm-group-count"></span>' +
      '<span class="material-symbols-outlined rm-group-toggle">expand_more</span>' +
    '</div>';
  top.querySelector('.rm-group-name').textContent = name;
  top.querySelector('.rm-group-type').textContent = type ? ('ประเภท: ' + type) : '';
  top.querySelector('.rm-group-count').textContent = 'ถูกรายงาน ' + reports.length + ' ครั้ง';
  top.addEventListener('click', function () { card.classList.toggle('open'); });

  const detail = document.createElement('div');
  detail.className = 'rm-group-detail';
  reports.forEach(rp => detail.appendChild(buildReportItemEl(rp)));

  card.appendChild(top);
  card.appendChild(detail);
  return card;
}

function buildSummaryChip(num, label) {
  const chip = document.createElement('div');
  chip.className = 'rm-summary-chip' + (num > 0 ? ' has-value' : '');
  chip.innerHTML = '<div class="rm-summary-num"></div><div class="rm-summary-label"></div>';
  chip.querySelector('.rm-summary-num').textContent = num;
  chip.querySelector('.rm-summary-label').textContent = label;
  return chip;
}

function renderReportSummary(reports) {
  const row = document.getElementById('rmSummaryRow');
  row.innerHTML = '';

  const withType = reports.filter(rp => getTargetType(rp));
  if (!withType.length) {
    // Backend ยังไม่ได้ส่งข้อมูลประเภทรายการมาด้วย -> โชว์แค่ยอดรวม
    row.appendChild(buildSummaryChip(reports.length, 'การรายงานทั้งหมด'));
    return;
  }

  const tourNames = new Set();
  const homestayNames = new Set();
  reports.forEach(rp => {
    const type = getTargetType(rp);
    const name = getTargetName(rp) || '-';
    if (type === 'ทัวร์') tourNames.add(name);
    else if (type === 'โฮมสเตย์') homestayNames.add(name);
  });

  row.appendChild(buildSummaryChip(reports.length, 'การรายงานทั้งหมด'));
  row.appendChild(buildSummaryChip(tourNames.size, 'ทัวร์ที่ถูกรายงาน'));
  row.appendChild(buildSummaryChip(homestayNames.size, 'โฮมสเตย์ที่ถูกรายงาน'));
}

function renderReportList(reports) {
  const listEl = document.getElementById('rmReportList');
  listEl.innerHTML = '';

  const groupable = reports.filter(rp => getTargetName(rp));
  if (!groupable.length) {
    // ไม่มีข้อมูลชื่อรายการ -> แสดงเป็นลิสต์รายงานแบบเดิม (flat)
    reports.forEach(rp => listEl.appendChild(buildReportItemEl(rp)));
    return;
  }

  // จัดกลุ่มตามชื่อรายการ (ทัวร์/โฮมสเตย์)
  const groups = new Map(); // key: name -> { type, reports: [] }
  reports.forEach(rp => {
    const name = getTargetName(rp) || 'ไม่ระบุรายการ';
    const type = getTargetType(rp);
    if (!groups.has(name)) groups.set(name, { type, reports: [] });
    groups.get(name).reports.push(rp);
  });

  groups.forEach((g, name) => {
    listEl.appendChild(buildGroupCardEl(name, g.type, g.reports));
  });
}

async function showReportModal(btn) {
  const id = btn.dataset.id;

  const modal = document.getElementById('suspendModal');
  if (!modal) return;

  // ── เติมข้อมูลโปรไฟล์ผู้จัดการจากปุ่มที่กด ──
  const name = btn.dataset.name || '-';
  document.getElementById('modalManagerName').innerText = name;
  document.getElementById('modalManagerId').innerText = 'ID: ' + id;
  document.getElementById('modalManagerAvatar').innerText = name.trim().charAt(0) || 'A';
  document.getElementById('modalManagerCommunity').innerText = btn.dataset.community || '-';
  document.getElementById('modalManagerEmail').innerText = btn.dataset.email || '-';
  document.getElementById('modalManagerPhone').innerText = btn.dataset.phone || '-';
  document.getElementById('modalManagerDate').innerText = btn.dataset.date || '-';

  const statusEl = document.getElementById('modalManagerStatus');
  const isActive = (btn.dataset.status || '').toUpperCase() === 'ACTIVE';
  statusEl.innerText = isActive ? 'ใช้งานปกติ' : 'ถูกระงับการใช้งาน';
  statusEl.className = 'rm-status-chip ' + (isActive ? 'active' : 'inactive');

  document.getElementById('rmSuspendReasonInput').value = ""; // รีเซ็ตช่องเหตุผล
  currentSuspendUrl = '/admin/manager/suspend/' + id;

  const listEl = document.getElementById('rmReportList');
  const emptyEl = document.getElementById('rmEmptyState');
  const summaryRow = document.getElementById('rmSummaryRow');
  emptyEl.style.display = 'none';
  summaryRow.innerHTML = '';
  listEl.innerHTML = '<div class="rm-empty">กำลังโหลดรายการรายงาน...</div>';

  modal.style.display = 'flex';

  try {
    const res = await fetch('/api/admin/reports/by-manager/' + encodeURIComponent(id));
    if (!res.ok) throw new Error('โหลดรายงานไม่สำเร็จ');
    const reports = await res.json();

    listEl.innerHTML = '';

    if (!reports || !reports.length) {
      emptyEl.style.display = 'block';
      return;
    }

    renderReportSummary(reports);
    renderReportList(reports);
  } catch (err) {
    listEl.innerHTML = '';
    emptyEl.textContent = 'ไม่สามารถโหลดรายการรายงานได้ กรุณาลองใหม่อีกครั้ง';
    emptyEl.style.display = 'block';
  }
}

function closeSuspendModal() {
  const modal = document.getElementById('suspendModal');
  if (modal) modal.style.display = 'none';
}

// modal ทั่วไปแบบ overlay (ใช้กับ suspendConfirmModal)
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

// ขั้นตอนที่ 1: ตรวจสอบเหตุผล แล้วเปิด modal ยืนยัน (สีแดง)
// ขั้นตอนที่ 2: กดยืนยันใน modal สีแดง แล้วค่อยส่งฟอร์มจริง
document.addEventListener('DOMContentLoaded', function() {
  const suspendConfirmBtn = document.getElementById('suspendConfirmBtn');
  if (suspendConfirmBtn) {
    suspendConfirmBtn.addEventListener('click', function() {
      const reasonValue = document.getElementById('rmSuspendReasonInput').value.trim();

      if (!reasonValue) {
        alert('กรุณาระบุเหตุผลในการระงับบัญชีก่อนยืนยัน');
        return;
      }

      document.getElementById('scManagerName').innerText = document.getElementById('modalManagerName').innerText;
      document.getElementById('scReasonPreview').innerText = reasonValue;
      document.getElementById('hiddenReasonInput').value = reasonValue;

      openModal('suspendConfirmModal');
    });
  }

  const suspendFinalConfirmBtn = document.getElementById('suspendFinalConfirmBtn');
  if (suspendFinalConfirmBtn) {
    suspendFinalConfirmBtn.addEventListener('click', function() {
      // ป้องกันการกดซ้ำ
      this.disabled = true;
      this.innerHTML = 'กำลังดำเนินการ...';

      const form = document.getElementById('modalSuspendForm');
      form.action = currentSuspendUrl;
      form.submit();
    });
  }
});

function showActivateModal(id, name) {
  const modal = document.getElementById('activateModal');
  const nameSpan = document.getElementById('modalActivateManagerName');
  const form = document.getElementById('modalActivateForm');

  if (modal && nameSpan && form) {
    nameSpan.innerText = name;
    form.action = '/admin/manager/activate/' + id;
    modal.style.display = 'flex';
  }
}

function closeActivateModal() {
  const modal = document.getElementById('activateModal');
  if (modal) modal.style.display = 'none';
}

// ปิด Modal เมื่อคลิกพื้นที่รอบนอก หรือกดปุ่ม ESC
window.onclick = function(event) {
  const suspendModal = document.getElementById('suspendModal');
  const activateModal = document.getElementById('activateModal');
  const suspendConfirmModal = document.getElementById('suspendConfirmModal');

  if (event.target == suspendModal) closeSuspendModal();
  if (event.target == activateModal) closeActivateModal();
  if (event.target == suspendConfirmModal) closeModal('suspendConfirmModal');
}

window.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    closeSuspendModal();
    closeActivateModal();
    closeModal('suspendConfirmModal');
  }
});