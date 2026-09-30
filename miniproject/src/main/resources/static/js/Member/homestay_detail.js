

  (function() {
    var iframe = document.getElementById('map-iframe');
    if (iframe && HOMESTAY_ADDRESS) {
      iframe.src = 'https://maps.google.com/maps?q=' + encodeURIComponent(HOMESTAY_ADDRESS) + '&t=&z=14&ie=UTF8&iwloc=&output=embed';
    }
  })();

  /* User Menu */
  function toggleUserMenu() {
    var w = document.getElementById('userMenuWrapper');
    if (w) w.classList.toggle('open');
  }
  document.addEventListener('click', function(e) {
    var w = document.getElementById('userMenuWrapper');
    if (w && !w.contains(e.target)) w.classList.remove('open');
  });

  /* Room Carousel (thumbnail ในการ์ด) */
  function getCarouselState(btn) {
    var carousel = btn.closest('.room-carousel');
    var track    = carousel.querySelector('.room-carousel-track');
    var slides   = carousel.querySelectorAll('.room-carousel-slide');
    var dots     = carousel.querySelectorAll('.carousel-dot');
    var idx      = parseInt(carousel.dataset.idx || '0');
    return { carousel, track, slides, dots, idx };
  }
  function setCarouselIdx(carousel, track, slides, dots, newIdx) {
    newIdx = (newIdx + slides.length) % slides.length;
    carousel.dataset.idx = newIdx;
    track.style.transform = 'translateX(-' + (newIdx * 100) + '%)';
    dots.forEach(function(d, i) { d.classList.toggle('active', i === newIdx); });
  }
  function carouselPrev(btn) { var s = getCarouselState(btn); setCarouselIdx(s.carousel, s.track, s.slides, s.dots, s.idx - 1); }
  function carouselNext(btn) { var s = getCarouselState(btn); setCarouselIdx(s.carousel, s.track, s.slides, s.dots, s.idx + 1); }

  /* Lightbox (gallery ของโฮมสเตย์) */
  var lbIdx = 0;
  function openLightbox(i) {
    if (!GALLERY_IMGS || !GALLERY_IMGS.length) return;
    lbIdx = i; updateLb();
    document.getElementById('lightbox').classList.add('open');
    document.body.style.overflow = 'hidden';
  }
  function closeLightbox() {
    document.getElementById('lightbox').classList.remove('open');
    document.body.style.overflow = '';
  }
  function updateLb() {
    var src = GALLERY_IMGS[lbIdx];
    if (src) src = src.trim();
    document.getElementById('lb-img').src = src || '';
    document.getElementById('lb-counter').textContent = (lbIdx + 1) + ' / ' + GALLERY_IMGS.length;
  }
  function prevImg() { lbIdx = (lbIdx - 1 + GALLERY_IMGS.length) % GALLERY_IMGS.length; updateLb(); }
  function nextImg() { lbIdx = (lbIdx + 1) % GALLERY_IMGS.length; updateLb(); }
  document.addEventListener('keydown', function(e) {
    var lb = document.getElementById('lightbox');
    if (lb.classList.contains('open')) {
      if (e.key === 'Escape') closeLightbox();
      if (e.key === 'ArrowLeft') prevImg();
      if (e.key === 'ArrowRight') nextImg();
    }
  });
  document.getElementById('lightbox').addEventListener('click', function(e) {
    if (e.target === this) closeLightbox();
  });

  /* Review Modal */
  function openReviewModal(bookingId) {
    document.getElementById('reviewBookingId').value = bookingId;
    document.getElementById('reviewModal').style.display = 'flex';
  }
  function closeReviewModal() {
    document.getElementById('reviewModal').style.display = 'none';
    document.getElementById('ratingInput').value = 0;
    resetStars();
  }
  var stars = document.querySelectorAll('#starPicker span');
  function resetStars() {
    stars.forEach(function(s) { s.style.color = 'var(--neutral-300)'; });
  }
  stars.forEach(function(star) {
    star.addEventListener('click', function() {
      var val = parseInt(this.dataset.val);
      document.getElementById('ratingInput').value = val;
      stars.forEach(function(s) {
        s.style.color = parseInt(s.dataset.val) <= val ? 'var(--amber-500)' : 'var(--neutral-300)';
      });
    });
  });

  /* Room Detail Modal — ดึงจากข้อมูลที่ render ซ่อนไว้ใน .room-detail-data (ไม่ยิง request เพิ่ม) */
  var rdImages = [];
  var rdImgIdx = 0;

  function openRoomDetailModal(roomtypeId) {
    var dataEl = document.querySelector('.room-detail-data[data-roomid="' + roomtypeId + '"]');
    if (!dataEl) return;

    document.getElementById('rdModalName').textContent  = dataEl.querySelector('.rd-name').textContent;
    document.getElementById('rdModalPrice').textContent = dataEl.querySelector('.rd-price').textContent.trim();
    document.getElementById('rdModalBed').textContent   = dataEl.querySelector('.rd-bedtype').textContent;
    document.getElementById('rdModalGuest').textContent = dataEl.querySelector('.rd-maxguest').textContent;
    document.getElementById('rdModalDesc').textContent  = dataEl.querySelector('.rd-description').textContent;

    var condition = dataEl.querySelector('.rd-condition').textContent.trim();
    var condWrap = document.getElementById('rdModalConditionWrap');
    if (condition) {
      document.getElementById('rdModalCondition').textContent = condition;
      condWrap.style.display = '';
    } else {
      condWrap.style.display = 'none';
    }

    // ---- gallery ----
    var imgs = dataEl.querySelectorAll('.rd-images img');
    rdImages = Array.prototype.map.call(imgs, function (img) { return img.src; });
    rdImgIdx = 0;

    var mainWrap = document.getElementById('rdMainWrap');
    var noImgEl  = document.getElementById('rdModalNoImg');
    var thumbsEl = document.getElementById('rdModalThumbs');
    thumbsEl.innerHTML = '';

    if (rdImages.length) {
      mainWrap.style.display = '';
      noImgEl.style.display  = 'none';
      rdUpdateMainImg();

      rdImages.forEach(function (src, i) {
        var thumb = document.createElement('div');
        thumb.className = 'rd-thumb' + (i === 0 ? ' active' : '');
        thumb.innerHTML = '<img src="' + src + '" alt=""/>';
        thumb.onclick = function () { rdImgIdx = i; rdUpdateMainImg(); };
        thumbsEl.appendChild(thumb);
      });

      var navBtns = mainWrap.querySelectorAll('.rd-nav-btn');
      navBtns.forEach(function (btn) { btn.style.display = rdImages.length > 1 ? '' : 'none'; });
    } else {
      mainWrap.style.display = 'none';
      noImgEl.style.display  = 'flex';
    }

    // ---- facilities ----
    var facWrap = document.getElementById('rdModalFacilities');
    facWrap.innerHTML = '';
    dataEl.querySelectorAll('.rd-facilities span').forEach(function (f) {
      var item = document.createElement('div');
      item.className = 'rd-modal-facility-item';
      item.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7"/></svg><span></span>';
      item.querySelector('span').textContent = f.textContent;
      facWrap.appendChild(item);
    });

    document.getElementById('roomDetailModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
  }

  function rdUpdateMainImg() {
    document.getElementById('rdModalMainImg').src = rdImages[rdImgIdx] || '';
    var thumbs = document.querySelectorAll('#rdModalThumbs .rd-thumb');
    thumbs.forEach(function (t, i) { t.classList.toggle('active', i === rdImgIdx); });
  }
  function rdPrevImg() {
    if (!rdImages.length) return;
    rdImgIdx = (rdImgIdx - 1 + rdImages.length) % rdImages.length;
    rdUpdateMainImg();
  }
  function rdNextImg() {
    if (!rdImages.length) return;
    rdImgIdx = (rdImgIdx + 1) % rdImages.length;
    rdUpdateMainImg();
  }

  function closeRoomDetailModal() {
    document.getElementById('roomDetailModal').style.display = 'none';
    document.body.style.overflow = '';
  }
  document.getElementById('roomDetailModal').addEventListener('click', function (e) {
    if (e.target === this) closeRoomDetailModal();
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeRoomDetailModal();
  });


  // ═══════════════════════════════════════════
  // REPORT MODAL (โฮมสเตย์) — 3 ขั้นตอน: กรอกฟอร์ม -> ยืนยัน -> สำเร็จ
  // ═══════════════════════════════════════════
  var reportEvidenceBase64 = null;

  function openReportModal() {
    document.getElementById('reportModalOverlay').classList.add('open');
  }
  function closeReportModal() {
    document.getElementById('reportModalOverlay').classList.remove('open');
    resetReportForm();
  }

  // ★ แก้: เพิ่ม guard กันกรณียังไม่ล็อกอิน + ล้างกรอบแดงของช่องรายละเอียด
  function resetReportForm() {
    var reasonEl = document.getElementById('reportReason');
    if (!reasonEl) return; // ยังไม่ล็อกอิน ไม่มีฟอร์มให้รีเซ็ต
    reasonEl.value = '';
    document.getElementById('reportDescription').value = '';
    document.getElementById('reportDescription').classList.remove('invalid');
    document.getElementById('reportFileInput').value = '';
    document.getElementById('reportFilePreview').style.display = 'none';
    document.getElementById('reportFilePreview').src = '';
    reportEvidenceBase64 = null;
    var msg = document.getElementById('reportMsg');
    msg.className = 'report-msg';
    msg.textContent = '';
    document.getElementById('reportSubmitBtn').disabled = false;
    document.getElementById('reportSubmitBtn').textContent = 'ส่งรายงาน';
  }

  function handleReportFile(e) {
    var file = e.target.files[0];
    if (!file) return;
    if (file.size > 3 * 1024 * 1024) {
      alert('ไฟล์รูปใหญ่เกินไป (จำกัด 3MB)');
      e.target.value = '';
      return;
    }
    var reader = new FileReader();
    reader.onload = function (ev) {
      reportEvidenceBase64 = ev.target.result;
      var preview = document.getElementById('reportFilePreview');
      preview.src = reportEvidenceBase64;
      preview.style.display = 'block';
    };
    reader.readAsDataURL(file);
  }

  // ★ แก้: ขั้นที่ 1 เพิ่มการบังคับกรอกรายละเอียด
  function submitReport() {
    var reason = document.getElementById('reportReason').value;
    var descEl = document.getElementById('reportDescription');
    var description = descEl.value.trim();
    var msg = document.getElementById('reportMsg');

    if (!reason) {
      msg.className = 'report-msg err';
      msg.textContent = 'กรุณาเลือกเหตุผลในการรายงาน';
      return;
    }

    if (!description) {
      descEl.classList.add('invalid');
      descEl.focus();
      msg.className = 'report-msg err';
      msg.textContent = 'กรุณากรอกรายละเอียดเพิ่มเติม';
      return;
    }

    msg.className = 'report-msg';
    msg.textContent = '';

    document.getElementById('confirmReportOverlay').classList.add('open');
  }

  function closeConfirmReportModal() {
    document.getElementById('confirmReportOverlay').classList.remove('open');
  }

  // ขั้นที่ 2: กดยืนยันใน modal แล้วค่อยยิง API จริง
  function confirmSubmitReport() {
    var reason = document.getElementById('reportReason').value;
    var description = document.getElementById('reportDescription').value.trim();
    var msg = document.getElementById('reportMsg');
    var confirmBtn = document.getElementById('confirmReportBtn');
    var reportBtn = document.getElementById('reportSubmitBtn');

    var homestayIdEl = document.querySelector('[data-homestay-id]');
    var homestayId = homestayIdEl ? homestayIdEl.dataset.homestayId : null;
    if (!homestayId) {
      closeConfirmReportModal();
      msg.className = 'report-msg err';
      msg.textContent = 'ไม่พบรหัสโฮมสเตย์ กรุณารีเฟรชหน้าใหม่';
      return;
    }

    confirmBtn.disabled = true;
    confirmBtn.textContent = 'กำลังส่ง...';
    reportBtn.disabled = true;

    var formData = new FormData();
    formData.append('homestayId', homestayId);
    formData.append('reason', reason);
    if (description) formData.append('description', description);
    if (reportEvidenceBase64) formData.append('evidenceImage', reportEvidenceBase64);

    fetch('/api/reports', {
      method: 'POST',
      body: formData
    })
      .then(function (res) {
        if (!res.ok) return res.text().then(function (t) { throw new Error(t || 'ส่งรายงานไม่สำเร็จ'); });
        return res.json();
      })
      .then(function () {
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'ยืนยันส่งรายงาน';
        closeConfirmReportModal();
        document.getElementById('reportModalOverlay').classList.remove('open');
        resetReportForm();
        // ขั้นที่ 3: แจ้งผลว่าสำเร็จ
        document.getElementById('successReportOverlay').classList.add('open');
      })
      .catch(function (err) {
        console.error(err);
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'ยืนยันส่งรายงาน';
        reportBtn.disabled = false;
        closeConfirmReportModal();
        msg.className = 'report-msg err';
        msg.textContent = err.message || 'เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง';
      });
  }

  function closeSuccessReportModal() {
    document.getElementById('successReportOverlay').classList.remove('open');
  }

  // ═══════════════════════════════════════════
  // รีวิว — Pagination แสดงทีละ 6 รีวิว
  // ═══════════════════════════════════════════
  let reviewCurrentPage = 1;
  const REVIEW_PAGE_SIZE = 6;

  function renderReviewPage() {
    const list = document.getElementById('reviewList');
    if (!list) return; // ไม่มีรีวิวเลย -> ไม่มี list ให้ทำงาน

    const cards = Array.from(list.querySelectorAll('.review-item'));
    const totalPages = Math.max(1, Math.ceil(cards.length / REVIEW_PAGE_SIZE));
    if (reviewCurrentPage > totalPages) reviewCurrentPage = totalPages;
    if (reviewCurrentPage < 1) reviewCurrentPage = 1;

    cards.forEach(card => card.style.display = 'none');

    const start = (reviewCurrentPage - 1) * REVIEW_PAGE_SIZE;
    const end = start + REVIEW_PAGE_SIZE;
    cards.slice(start, end).forEach(card => card.style.display = '');

    const pagination = document.getElementById('reviewPagination');
    if (pagination) {
      pagination.style.display = totalPages > 1 ? 'flex' : 'none';
      document.getElementById('reviewPageInfo').textContent = 'หน้า ' + reviewCurrentPage + ' / ' + totalPages;
      document.getElementById('reviewPrevBtn').classList.toggle('disabled', reviewCurrentPage <= 1);
      document.getElementById('reviewNextBtn').classList.toggle('disabled', reviewCurrentPage >= totalPages);
    }
  }

  function goToReviewPage(delta) {
    reviewCurrentPage += delta;
    renderReviewPage();
    document.getElementById('reviewList').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  document.addEventListener('DOMContentLoaded', renderReviewPage);