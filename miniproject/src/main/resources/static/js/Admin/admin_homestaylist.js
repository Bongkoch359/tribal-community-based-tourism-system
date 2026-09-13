// ══════════════════════════════════════════
// admin_homestaylist.js
// ใช้ร่วมกันทั้งหน้า /admin/homestay (รายการ) และ /admin/homestay/detail/{id} (รายละเอียด)
// ══════════════════════════════════════════

function handleConfirmClick(button) {
  const ownerId = button.dataset.ownerId;
  const ownerName = button.dataset.ownerName;
  const action = button.dataset.action; // 'approve' | 'reject' | 'suspend'

  openConfirmModal(ownerId, action, ownerName);
}

// ══════════════════════════════════════════
// กรองตามสถานะบัญชี (data-status) — มีเฉพาะหน้ารายการ
// ══════════════════════════════════════════
function toggleStatusFilterMenu(e) {
  e.stopPropagation();
  const dropdown = document.getElementById('statusFilterDropdown');
  if (dropdown) dropdown.classList.toggle('open');
}

function initFilterDropdown() {
  const dropdown = document.getElementById('statusFilterDropdown');
  if (!dropdown) return; // หน้านี้ไม่มีตัวกรองสถานะ (เช่น หน้ารายละเอียด) ข้ามไปเลย

  const options = document.querySelectorAll('#statusFilterDropdown .sf-option');

  options.forEach(opt => {
    opt.addEventListener('click', function (e) {
      e.preventDefault();

      options.forEach(o => o.classList.remove('active'));
      this.classList.add('active');

      const filter = this.dataset.filter;

      document.getElementById('sfTriggerText').textContent = this.textContent.split('(')[0].trim();
      document.getElementById('sfTriggerCount').textContent =
        this.textContent.match(/\((\d+)\)/)?.[1] ?? '0';
      document.getElementById('sfTriggerIcon').textContent = this.dataset.icon || 'schedule';

      let visibleRows = 0;
      document.querySelectorAll('tbody tr[data-status]').forEach(row => {
        if (filter === 'all' || row.dataset.status === filter) {
          row.style.display = '';
          visibleRows++;
        } else {
          row.style.display = 'none';
        }
      });

      const emptyRow = document.getElementById('empty-row');
      if (emptyRow) {
        emptyRow.style.display = visibleRows === 0 ? '' : 'none';
      }

      dropdown.classList.remove('open');
    });
  });

  document.addEventListener('click', (e) => {
    if (!dropdown.contains(e.target)) dropdown.classList.remove('open');
  });
}

// ══════════════════════════════════════════
// CONFIRM MODAL (อนุมัติ / ปฏิเสธ / ระงับบัญชี)
// ══════════════════════════════════════════
function openConfirmModal(ownerId, action, ownerName) {
  const icon = document.getElementById('confirm-icon');
  const title = document.getElementById('confirm-title');
  const desc = document.getElementById('confirm-desc');
  const form = document.getElementById('confirm-form');
  const submitBtn = document.getElementById('confirm-submit-btn');
  const reasonField = document.getElementById('rejectReasonField');
  const reasonInput = document.getElementById('rejectReasonInput');
  const hiddenReason = document.getElementById('confirmHiddenReason');

  reasonInput.value = '';
  hiddenReason.value = '';

  if (action === 'approve') {
    icon.className = 'confirm-icon approve';
    icon.innerHTML = '<span class="material-symbols-outlined" style="font-size: 32px;">check_circle</span>';
    title.textContent = 'ยืนยันการอนุมัติ';
    desc.innerHTML = 'คุณแน่ใจหรือไม่ว่าต้องการอนุมัติการสมัครสมาชิกเจ้าของโฮมสเตย์ <strong id="confirm-name"></strong>?';
    submitBtn.className = 'btn-action btn-approve';
    submitBtn.textContent = 'ยืนยันการอนุมัติ';
    submitBtn.disabled = false;
    form.action = '/admin/homestay/approve/' + ownerId;
    reasonField.style.display = 'none';

  } else if (action === 'suspend') {
    // ระงับบัญชีที่อนุมัติไปแล้ว — ใช้ endpoint เดียวกับปฏิเสธ (ตั้งสถานะไม่ ACTIVE) แต่ข้อความต่างออกไป
    icon.className = 'confirm-icon reject';
    icon.innerHTML = '<span class="material-symbols-outlined" style="font-size: 32px;">block</span>';
    title.textContent = 'ยืนยันการระงับบัญชี';
    desc.innerHTML = 'คุณแน่ใจหรือไม่ว่าต้องการระงับบัญชีเจ้าของโฮมสเตย์ <strong id="confirm-name"></strong>? บัญชีนี้จะไม่สามารถใช้งานระบบได้จนกว่าจะได้รับการอนุมัติใหม่อีกครั้ง';
    submitBtn.className = 'btn-action btn-reject';
    submitBtn.textContent = 'ยืนยันการระงับบัญชี';
    submitBtn.disabled = true;
    form.action = '/admin/homestay/reject/' + ownerId;
    reasonField.style.display = 'block';

    reasonInput.oninput = () => {
      submitBtn.disabled = reasonInput.value.trim().length === 0;
    };

  } else {
    // reject (ปฏิเสธคำขอที่ยังรออนุมัติ)
    icon.className = 'confirm-icon reject';
    icon.innerHTML = '<span class="material-symbols-outlined" style="font-size: 32px;">warning</span>';
    title.textContent = 'ยืนยันการปฏิเสธ';
    desc.innerHTML = 'คุณแน่ใจหรือไม่ว่าต้องการปฏิเสธการสมัครสมาชิกเจ้าของโฮมสเตย์ <strong id="confirm-name"></strong>?';
    submitBtn.className = 'btn-action btn-reject';
    submitBtn.textContent = 'ยืนยันการปฏิเสธ';
    submitBtn.disabled = true;
    form.action = '/admin/homestay/reject/' + ownerId;
    reasonField.style.display = 'block';

    reasonInput.oninput = () => {
      submitBtn.disabled = reasonInput.value.trim().length === 0;
    };
  }

  form.onsubmit = () => {
    hiddenReason.value = reasonInput.value.trim();
  };

  document.getElementById('confirm-name').textContent = ownerName;
  document.getElementById('confirmModal').classList.add('show');
}

function closeConfirmModal() {
  document.getElementById('confirmModal').classList.remove('show');
}

// ══════════════════════════════════════════
// INIT ทั้งหมดตอนโหลดหน้า
// ══════════════════════════════════════════
window.addEventListener('DOMContentLoaded', () => {
  initFilterDropdown();

  const modal = document.getElementById('confirmModal');
  if (modal) {
    modal.addEventListener('click', function (e) {
      if (e.target === this) closeConfirmModal();
    });
  }

  const pendingOption = document.querySelector('#statusFilterDropdown [data-filter="pending"]');
  if (pendingOption) pendingOption.click();
});