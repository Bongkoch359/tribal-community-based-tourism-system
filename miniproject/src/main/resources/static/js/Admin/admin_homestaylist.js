
  // Filter Pills — กรองแถวตาม data-status
  document.querySelectorAll('.filter-pill').forEach(pill => {
    pill.addEventListener('click', function (e) {
      e.preventDefault();

      document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
      this.classList.add('active');

      const filter = this.dataset.filter;
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
    });
  });

  // เลือกแท็บ "รออนุมัติ" อัตโนมัติเมื่อโหลดหน้า
  window.addEventListener('DOMContentLoaded', () => {
    const pendingPill = document.querySelector('[data-filter="pending"]');
    if (pendingPill) pendingPill.click();
  });

  // ===== CONFIRM MODAL (Alternate Flow 3.1 / 5.1.1) =====
  function openConfirmModal(ownerId, action, ownerName) {
    const icon = document.getElementById('confirm-icon');
    const title = document.getElementById('confirm-title');
    const desc = document.getElementById('confirm-desc');
    const form = document.getElementById('confirm-form');
    const submitBtn = document.getElementById('confirm-submit-btn');

    if (action === 'approve') {
      icon.className = 'confirm-icon approve';
      icon.innerHTML = '<span class="material-symbols-outlined" style="font-size: 32px;">check_circle</span>';
      title.textContent = 'ยืนยันการอนุมัติ';
      desc.innerHTML = 'คุณแน่ใจหรือไม่ว่าต้องการอนุมัติการสมัครสมาชิกเจ้าของโฮมสเตย์ <strong id="confirm-name"></strong>?';
      submitBtn.className = 'btn-action btn-approve';
      submitBtn.textContent = 'ยืนยันการอนุมัติ';
      form.action = '/admin/homestay/approve/' + ownerId;
    } else {
      icon.className = 'confirm-icon reject';
      icon.innerHTML = '<span class="material-symbols-outlined" style="font-size: 32px;">warning</span>';
      title.textContent = 'ยืนยันการปฏิเสธ';
      desc.innerHTML = 'คุณแน่ใจหรือไม่ว่าต้องการปฏิเสธการสมัครสมาชิกเจ้าของโฮมสเตย์ <strong id="confirm-name"></strong>?';
      submitBtn.className = 'btn-action btn-reject';
      submitBtn.textContent = 'ยืนยันการปฏิเสธ';
      form.action = '/admin/homestay/reject/' + ownerId;
    }
    document.getElementById('confirm-name').textContent = ownerName;
    document.getElementById('confirmModal').classList.add('show');
  }

  function closeConfirmModal() {
    document.getElementById('confirmModal').classList.remove('show');
  }

  // ปิด confirm modal เมื่อคลิกพื้นหลัง
  document.getElementById('confirmModal').addEventListener('click', function (e) {
    if (e.target === this) closeConfirmModal();
  });