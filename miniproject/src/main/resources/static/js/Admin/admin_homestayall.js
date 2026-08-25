  document.querySelectorAll('.filter-pill').forEach(pill => {
    pill.addEventListener('click', function(e) {
      e.preventDefault();
      document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
      this.classList.add('active');
      const filter = this.dataset.filter;
      document.querySelectorAll('tbody tr[data-status]').forEach(row => {
        if (filter === 'all') {
          row.style.display = '';
        } else {
          row.style.display = row.dataset.status === filter ? '' : 'none';
        }
      });
    });
  });

  // ── Suspend Modal (Basic Flow 1-4) ──
  // ── Suspend Modal ──
  function openSuspendModal(btn) {
    const id   = btn.dataset.id;
    const name = btn.dataset.name;
    document.getElementById('suspendOwnerName').textContent = name;
    document.getElementById('suspendReasonSelect').value = ""; // รีเซ็ตค่า dropdown ทุกครั้งที่เปิด
    document.getElementById('suspendForm').action = '/admin/homestay/suspend/' + id;
    document.getElementById('suspendModal').classList.add('show');
  }

  // ยืนยัน → ตรวจสอบเหตุผล → ส่งฟอร์ม
  document.getElementById('suspendConfirmBtn').addEventListener('click', function() {
    const reasonValue = document.getElementById('suspendReasonSelect').value;
    
    if (!reasonValue) {
      showErrorToast('กรุณาเลือกเหตุผลในการระงับบัญชี');
      return;
    }

    // เอาค่าเหตุผลใส่ลงใน hidden input ของฟอร์ม
    document.getElementById('hiddenReasonInput').value = reasonValue;

    const form = document.getElementById('suspendForm');
    if (!form.action) {
      showErrorToast('ไม่สามารถระงับบัญชีโฮมสเตย์นี้ได้ กรุณาลองใหม่อีกครั้ง');
      return;
    }
    form.submit();
  });

  // ── Activate Modal ──
  function openActivateModal(btn) {
    const id   = btn.dataset.id;
    const name = btn.dataset.name;
    document.getElementById('activateOwnerName').textContent = name;
    document.getElementById('activateForm').action = '/admin/homestay/activate/' + id;
    document.getElementById('activateModal').classList.add('show');
  }

  document.getElementById('activateConfirmBtn').addEventListener('click', function() {
    const form = document.getElementById('activateForm');
    if (!form.action) {
      showErrorToast('ไม่สามารถเปิดใช้งานบัญชีโฮมสเตย์นี้ได้ กรุณาลองใหม่อีกครั้ง');
      return;
    }
    form.submit();
  });

  // ── ปิด Modal ──
  function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
  }
  document.getElementById('suspendModal').addEventListener('click', function(e) {
    if (e.target === this) closeModal('suspendModal');
  });
  document.getElementById('activateModal').addEventListener('click', function(e) {
    if (e.target === this) closeModal('activateModal');
  });

  // ── Error Toast (Alternate Flow 3.1 / 5.1.1) ──
  function showErrorToast(message) {
    const toast = document.getElementById('errorToast');
    document.getElementById('errorToastMsg').textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3500);
  }

  // ── รับ errorMessage จาก Controller (Alternate Flow 5.1.1) ──
  // ── Error Toast & DOMContentLoaded ──
  window.addEventListener('DOMContentLoaded', () => {
    const toast = document.getElementById('errorToast');
    if (toast.classList.contains('show')) {
      setTimeout(() => toast.classList.remove('show'), 3500);
    }
  });