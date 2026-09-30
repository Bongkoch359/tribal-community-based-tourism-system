    function toggleUserMenu() {
      const w = document.getElementById('userMenuWrapper');
      if (w) w.classList.toggle('open');
    }

    function toggleLoginMenu(e) {
      e.stopPropagation();
      const w = document.getElementById('loginDropdown');
      if (w) w.classList.toggle('open');
    }

    document.addEventListener('click', function (e) {
      const w = document.getElementById('userMenuWrapper');
      if (w && !w.contains(e.target)) w.classList.remove('open');

      const l = document.getElementById('loginDropdown');
      if (l && !l.contains(e.target)) l.classList.remove('open');
    });

    // ═══════════════════════════════════════════
    // ปฏิทินเลือกวันเดินทาง — โชว์สถานะจริงของแต่ละวันจากรอบทัวร์ (Tourschedule)
    // ═══════════════════════════════════════════
    var scheduleByDate = {};
    (scheduleCalendarData || []).forEach(function (item) {
      scheduleByDate[item.date] = item;
    });

    // จับกลุ่มวันที่ตาม scheduleid เดียวกัน -> ได้ set ของวันที่ทั้งหมดในรอบทัวร์นั้น
    // ใช้ตอนกด select เพื่อไฮไลต์ทั้งช่วง (เช่น 29-31)
    var datesByScheduleId = {};
    (scheduleCalendarData || []).forEach(function (item) {
      if (item.scheduleid == null) return; // ไม่มี scheduleid -> ข้าม (จะ fallback เป็นวันเดียว)
      if (!datesByScheduleId[item.scheduleid]) datesByScheduleId[item.scheduleid] = [];
      datesByScheduleId[item.scheduleid].push(item.date);
    });

    // หาวันแรกที่จองได้ (เรียงตามวันที่จริง ไม่ใช่ลำดับที่ backend ส่งมา)
    function findFirstAvailableDate() {
      var list = (scheduleCalendarData || [])
        .filter(function (item) {
          return item.status === 'available' || item.status === 'low';
        })
        .sort(function (a, b) { return a.date.localeCompare(b.date); });
      return list.length > 0 ? list[0] : null;
    }

    var firstAvailable = findFirstAvailableDate();

    // cursor เริ่มต้น: เดือนของวันแรกที่จองได้ — ถ้าไม่มีรอบจองได้เลย fallback เป็นเดือนปัจจุบัน
    var calendarCursor = firstAvailable
      ? new Date(firstAvailable.date + 'T00:00:00')
      : new Date();
    calendarCursor.setDate(1);

    // เปลี่ยนจาก selectedDate เดี่ยว -> เป็น "ชุดวันที่ที่ถูกไฮไลต์" ของทั้งรอบทัวร์
    var selectedDate = null;          // วันที่ที่ผู้ใช้กดจริง (ใช้ส่งไป booking)
    var selectedDateGroup = [];       // ทุกวันในรอบทัวร์เดียวกัน (ใช้ไฮไลต์ในปฏิทิน)

    var THAI_MONTHS = ['มกราคม', 'กุมภาพันธ์', 'มีนาคม', 'เมษายน', 'พฤษภาคม', 'มิถุนายน',
      'กรกฎาคม', 'สิงหาคม', 'กันยายน', 'ตุลาคม', 'พฤศจิกายน', 'ธันวาคม'];

    function pad2(n) { return n < 10 ? '0' + n : '' + n; }
    function toISO(y, m, d) { return y + '-' + pad2(m + 1) + '-' + pad2(d); }

    function openCalendarModal() {
      // ทุกครั้งที่เปิด modal ให้กลับไปเดือนของวันที่เลือกไว้แล้ว หรือวันแรกที่จองได้
      var target = selectedDate || (firstAvailable ? firstAvailable.date : null);
      if (target) {
        calendarCursor = new Date(target + 'T00:00:00');
        calendarCursor.setDate(1);
      }
      document.getElementById('calendarModalOverlay').classList.add('open');
      renderCalendar();
    }
    function closeCalendarModal() {
      document.getElementById('calendarModalOverlay').classList.remove('open');
    }
    function changeCalendarMonth(delta) {
      var newCursor = new Date(calendarCursor);
      newCursor.setMonth(newCursor.getMonth() + delta);

      var minMonth = new Date();
      minMonth.setDate(1);
      minMonth.setHours(0, 0, 0, 0);

      var lastScheduleDate = (scheduleCalendarData || [])
        .map(function (d) { return d.date; })
        .sort()
        .pop();
      var maxMonth = lastScheduleDate
        ? new Date(lastScheduleDate + 'T00:00:00')
        : new Date(minMonth);
      maxMonth.setDate(1);

      if (newCursor < minMonth || newCursor > maxMonth) return; // กันเลื่อนเกินขอบเขตที่มีข้อมูลจริง

      calendarCursor = newCursor;
      renderCalendar();
    }
    // เช็กว่าวันที่กำหนดเป็น "วันแรก" ของ scheduleid นั้นๆ หรือไม่
    function isFirstDateOfSchedule(iso) {
      var info = scheduleByDate[iso];
      if (!info || info.scheduleid == null) return true; // ถ้าไม่มีระบบกลุ่ม ให้ถือว่าปกติ

      var groupDates = datesByScheduleId[info.scheduleid];
      if (!groupDates || groupDates.length === 0) return true;

      // เรียงวันที่ในกลุ่ม แล้วเช็กว่า iso นี้คือน้องเล็กสุด (วันแรก) หรือไม่
      var sorted = groupDates.slice().sort();
      return sorted[0] === iso;
    }

    function renderCalendar() {
      var y = calendarCursor.getFullYear(), m = calendarCursor.getMonth();
      document.getElementById('calendarMonthLabel').textContent = THAI_MONTHS[m] + ' ' + (y + 543);
      var grid = document.getElementById('calendarGrid');
      grid.innerHTML = '';

      var firstDay = new Date(y, m, 1).getDay();
      var daysInMonth = new Date(y, m + 1, 0).getDate();

      for (var i = 0; i < firstDay; i++) {
        var empty = document.createElement('div');
        empty.className = 'cal-day empty';
        grid.appendChild(empty);
      }

      for (var d = 1; d <= daysInMonth; d++) {
        var iso = toISO(y, m, d);
        var info = scheduleByDate[iso];
        var cell = document.createElement('div');

        // ตรวจสอบว่าเป็นวันแรกของรอบทัวร์ไหม (ถ้ามีรอบ แต่ไม่ใช่วันแรก ให้มองว่าไม่มีรอบในมุมมองปฏิทินนี้)
        var showAsTourStart = true;
        if (info && (info.status === 'available' || info.status === 'low')) {
          showAsTourStart = isFirstDateOfSchedule(iso);
        }

        var status = (info && showAsTourStart) ? info.status : 'none';
        cell.className = 'cal-day st-' + status;

        cell.title = status === 'full' ? 'ที่นั่งเต็มแล้ว' :
          status === 'closed' ? 'ยังไม่เปิดรับจองรอบนี้' :
            status === 'past' ? 'วันที่ผ่านมาแล้ว' :
              status === 'none' ? 'ไม่มีรอบทัวร์เริ่มต้นวันนี้' : '';

        // ไฮไลต์ทุกวันที่อยู่ใน group เดียวกับวันที่เลือกไว้
        if (selectedDateGroup.indexOf(iso) !== -1) {
          cell.classList.add('selected');
        }

        var num = document.createElement('div');
        num.className = 'dnum';
        num.textContent = d;
        cell.appendChild(num);

        // ให้คลิกและแสดงจำนวนที่นั่งเฉพาะ "วันแรกของรอบ" ที่สถานะ available หรือ low เท่านั้น
        if (info && showAsTourStart && (status === 'available' || status === 'low')) {
          var seats = document.createElement('div');
          seats.className = 'dseats';
          seats.textContent = info.availableSeats + ' ที่';
          cell.appendChild(seats);

          cell.addEventListener('click', (function (dateVal) {
            return function () { selectScheduleDate(dateVal); };
          })(iso));
        }

        grid.appendChild(cell);
      }
    }
    // ตอนคลิกเลือกวัน ให้แค่ไฮไลต์และเปลี่ยนข้อความโชว์รอบ แต่ "ยังไม่ปิด Modal"
    function selectScheduleDate(iso) {
      var info = scheduleByDate[iso];
      if (!info || (info.status !== 'available' && info.status !== 'low')) {
        return; // กดวันที่ว่าง/เต็ม ไม่ทำอะไร (มี tooltip อธิบายอยู่แล้ว)
      }

      // commit ทันที ไม่ต้องรอปุ่มยืนยัน
      selectedDate = iso;
      document.getElementById('tourDate').value = selectedDate;
      document.getElementById('tourDateError').style.display = 'none';

      if (info.scheduleid != null && datesByScheduleId[info.scheduleid]) {
        selectedDateGroup = datesByScheduleId[info.scheduleid];
      } else {
        selectedDateGroup = [iso];
      }

      // อัปเดต label บนปุ่มหลักทันที (ย้าย logic จาก confirmSelectedDate มาไว้ตรงนี้)
      var labelText = "";
      if (selectedDateGroup.length > 1) {
        var sortedGroup = selectedDateGroup.slice().sort();
        var s = sortedGroup[0].split('-'), e = sortedGroup[sortedGroup.length - 1].split('-');
        labelText = s[2] + '/' + s[1] + '/' + (parseInt(s[0], 10) + 543) + ' ถึง ' +
          e[2] + '/' + e[1] + '/' + (parseInt(e[0], 10) + 543);
      } else {
        var p = iso.split('-');
        labelText = p[2] + '/' + p[1] + '/' + (parseInt(p[0], 10) + 543);
      }
      document.getElementById('selectedDateLabel').textContent = labelText + ' (' + info.availableSeats + ' ที่ว่าง)';
      document.getElementById('openCalendarBtn').classList.add('has-date');

      renderCalendar(); // อัปเดตไฮไลต์ก่อนปิด ให้เห็นแวบนึงว่ากดวันไหน
      setTimeout(closeCalendarModal, 150); // หน่วงนิดให้เห็นไฮไลต์ก่อนปิด ไม่ปิดกระชากจนงง
    }

    function goToBooking() {
      const date = document.getElementById('tourDate').value;
      const errorEl = document.getElementById('tourDateError');

      if (!date) {
        errorEl.style.display = 'block';
        document.getElementById('openCalendarBtn').scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
      }
      errorEl.style.display = 'none';

      const tourIdEl = document.querySelector('[data-tour-id]');
      if (!tourIdEl || !tourIdEl.dataset.tourId) {
        alert('เกิดข้อผิดพลาด: ไม่พบรหัสทัวร์ กรุณารีเฟรชหน้าใหม่');
        return;
      }
      const tourId = tourIdEl.dataset.tourId;
      const params = new URLSearchParams({ tourdate: date });
      window.location.href = `/booking/tour/${tourId}?${params}`;
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

    var reportEvidenceBase64 = null;

    function openReportModal() {
      document.getElementById('reportModalOverlay').classList.add('open');
    }
    function closeReportModal() {
      document.getElementById('reportModalOverlay').classList.remove('open');
      resetReportForm();
    }
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
        reportEvidenceBase64 = ev.target.result; // data:image/...;base64,....
        var preview = document.getElementById('reportFilePreview');
        preview.src = reportEvidenceBase64;
        preview.style.display = 'block';
      };
      reader.readAsDataURL(file);
    }

    function closeConfirmReportModal() {
  document.getElementById('confirmReportOverlay').classList.remove('open');
}
    // ขั้นที่ 1: ตรวจข้อมูลในฟอร์ม แล้วเปิด modal ให้ "ยืนยัน" ก่อน — ยังไม่ยิง API
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

    // ขั้นที่ 2: กดยืนยันใน modal แล้วค่อยยิง API จริง
    function confirmSubmitReport() {
      var reason = document.getElementById('reportReason').value;
      var description = document.getElementById('reportDescription').value.trim();
      var msg = document.getElementById('reportMsg');
      var confirmBtn = document.getElementById('confirmReportBtn');
      var reportBtn = document.getElementById('reportSubmitBtn');

      var tourIdEl = document.querySelector('[data-tour-id]');
      var tourId = tourIdEl ? tourIdEl.dataset.tourId : null;
      if (!tourId) {
        closeConfirmReportModal();
        msg.className = 'report-msg err';
        msg.textContent = 'ไม่พบรหัสทัวร์ กรุณารีเฟรชหน้าใหม่';
        return;
      }

      confirmBtn.disabled = true;
      confirmBtn.textContent = 'กำลังส่ง...';
      reportBtn.disabled = true;

      var formData = new FormData();
      formData.append('tourId', tourId);
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
// TOUR HERO CAROUSEL — เลื่อนดูรูปทัวร์ทั้งหมด
// ═══════════════════════════════════════════
(function () {
  var track = document.getElementById('heroTrack');
  if (!track) return;

  var slides = track.querySelectorAll('.tour-hero-slide');
  var total = slides.length;
  if (total <= 1) return; // มีรูปเดียวไม่ต้องทำ carousel

  var idx = 0;
  var dotsWrap = document.getElementById('heroDots');
  var thumbsWrap = document.getElementById('heroThumbs');
  var counterText = document.getElementById('heroCounterText');

  // สร้าง dots
  for (var i = 0; i < total; i++) {
    var dot = document.createElement('button');
    dot.type = 'button';
    dot.className = 'hero-dot' + (i === 0 ? ' active' : '');
    dot.onclick = (function (n) { return function () { goToSlide(n); }; })(i);
    dotsWrap.appendChild(dot);
  }

  // สร้าง thumbnail strip (ใช้ src เดียวกับรูปใน slide)
  slides.forEach(function (slide, i) {
    var imgSrc = slide.querySelector('img').src;
    var thumb = document.createElement('div');
    thumb.className = 'hero-thumb' + (i === 0 ? ' active' : '');
    thumb.innerHTML = '<img src="' + imgSrc + '" alt=""/>';
    thumb.onclick = (function (n) { return function () { goToSlide(n); }; })(i);
    thumbsWrap.appendChild(thumb);
  });

  function updateUI() {
    track.style.transform = 'translateX(-' + (idx * 100) + '%)';
    counterText.textContent = (idx + 1) + ' / ' + total;
    dotsWrap.querySelectorAll('.hero-dot').forEach(function (d, i) {
      d.classList.toggle('active', i === idx);
    });
    thumbsWrap.querySelectorAll('.hero-thumb').forEach(function (t, i) {
      t.classList.toggle('active', i === idx);
    });
  }

  function goToSlide(n) {
    idx = (n + total) % total;
    updateUI();
  }

  window.heroPrev = function () { goToSlide(idx - 1); };
  window.heroNext = function () { goToSlide(idx + 1); };

  // เลื่อนอัตโนมัติทุก 5 วิ (ถอดออกได้ถ้าไม่ต้องการ)
  var autoTimer = setInterval(function () { goToSlide(idx + 1); }, 5000);
  var heroEl = document.getElementById('tourHero');
  heroEl.addEventListener('mouseenter', function () { clearInterval(autoTimer); });
  heroEl.addEventListener('mouseleave', function () {
    autoTimer = setInterval(function () { goToSlide(idx + 1); }, 5000);
  });
})();


    // ═══════════════════════════════════════════
    // แยกเงื่อนไขเป็นบรรทัด — รองรับทั้งขีด "-" และเลขลำดับ "1." / "1)"
    // ═══════════════════════════════════════════
    (function () {
      var block = document.getElementById('conditionBlock');
      if (!block) return;

      var raw = block.getAttribute('data-condition') || '';
      if (!raw.trim()) return;

      // แยกตาม:
      //  - ขีด "-" ที่มีช่องว่างล้อมอยู่ (เช่น " - ")
      //  - เลขลำดับที่ตามด้วย "." หรือ ")" เช่น "1." "2)" (ต้องอยู่ต้นข้อความ หรือมีช่องว่างนำหน้า)
      // หมายเหตุ: ไม่ตัดตัวเลขทั่วไปแบบ "1 วัน" "1 มื้อ" เพราะไม่มี . หรือ ) ตามหลัง
      var parts = raw
        .split(/\s*-\s+|(?:^|\s)\d+[.)]\s*/)
        .map(function (s) { return s.trim(); })
        .filter(Boolean);

      var list = document.getElementById('conditionList');
      if (parts.length <= 1) {
        // แยกไม่ได้เลย (ไม่มีขีด/เลข) -> โชว์เป็นก้อนเดียวแบบเดิม
        list.outerHTML = '<span>' + raw.trim() + '</span>';
        return;
      }

      parts.forEach(function (p) {
        var li = document.createElement('li');
        li.textContent = p;
        list.appendChild(li);
      });
    })();