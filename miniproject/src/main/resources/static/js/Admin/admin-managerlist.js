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

function showSuspendModal(id, name) {
  const modal = document.getElementById('suspendModal');
  const nameSpan = document.getElementById('modalManagerName');

  if (modal && nameSpan) {
    nameSpan.innerText = name;
    document.getElementById('suspendReasonSelect').value = ""; // รีเซ็ต Dropdown
    currentSuspendUrl = '/admin/manager/suspend/' + id;
    modal.style.display = 'flex';
  }
}

function closeSuspendModal() {
  const modal = document.getElementById('suspendModal');
  if (modal) modal.style.display = 'none';
}

// ดักคลิกปุ่มยืนยันระงับเพื่อเช็คความถูกต้องของเหตุผล
document.addEventListener('DOMContentLoaded', function() {
  const suspendConfirmBtn = document.getElementById('suspendConfirmBtn');
  if (suspendConfirmBtn) {
    suspendConfirmBtn.addEventListener('click', function() {
      const reasonValue = document.getElementById('suspendReasonSelect').value;

      if (!reasonValue) {
        alert('กรุณาเลือกเหตุผลในการระงับบัญชีก่อนยืนยัน');
        return;
      }

      // ป้องกันการกดซ้ำ
      this.disabled = true;
      this.innerText = 'กำลังดำเนินการ...';

      // ใส่ค่าเหตุผลลงใน hidden input และส่งฟอร์ม
      const form = document.getElementById('modalSuspendForm');
      document.getElementById('hiddenReasonInput').value = reasonValue;
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

  if (event.target == suspendModal) closeSuspendModal();
  if (event.target == activateModal) closeActivateModal();
}

window.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    closeSuspendModal();
    closeActivateModal();
  }
});