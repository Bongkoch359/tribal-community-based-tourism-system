document.querySelectorAll('textarea.form-control').forEach(function (ta) {
    const autoResize = () => {
        ta.style.height = 'auto';
        ta.style.height = (ta.scrollHeight + 2) + 'px';
    };
    ta.addEventListener('input', autoResize);
    autoResize();
});
(function () {
    if (window.showAlertModal) return;

    const style = document.createElement('style');
    style.textContent = `
        .ui-modal-icon.error   { background: linear-gradient(135deg, #ba1a1a, #e14b4b); box-shadow: 0 8px 24px rgba(186,26,26,.35); }
        .ui-modal-icon.warning { background: linear-gradient(135deg, #ff8e4d, #c9600f); box-shadow: 0 8px 24px rgba(255,142,77,.35); }
        .ui-modal-icon.success { background: linear-gradient(135deg, var(--green-dark,#006e2f), var(--green-mid,#22c55e)); box-shadow: 0 8px 24px rgba(0,110,47,.35); }
        .modal-btn-secondary {
            font-size: 13px; font-family: 'Sarabun', sans-serif; padding: 10px 22px;
            background: transparent; color: var(--text-muted, #3d4a3d);
            border: 1px solid var(--border, #e4e0d4); border-radius: 8px;
            cursor: pointer; font-weight: 700; transition: background .15s;
        }
        .modal-btn-secondary:hover { background: #faf7ef; }
        .modal-btn-danger { background: #ba1a1a !important; }
        .modal-btn-danger:hover { background: #931414 !important; }
        .ui-modal-desc { white-space: pre-line; }
    `;
    document.head.appendChild(style);

    const ICON_CLASS = { error: 'fa-circle-exclamation', warning: 'fa-triangle-exclamation', success: 'fa-check' };
    const DEFAULT_TITLE = { error: 'เกิดข้อผิดพลาด', warning: 'แจ้งเตือน', success: 'สำเร็จ' };

    function buildBackdrop() {
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop';
        backdrop.innerHTML =
            '<div class="modal-box">' +
            '  <div class="checkmark-wrap ui-modal-icon">' +
            '    <i class="fas checkmark-icon ui-modal-icon-glyph"></i>' +
            '  </div>' +
            '  <div class="modal-title ui-modal-title"></div>' +
            '  <div class="modal-desc ui-modal-desc"></div>' +
            '  <div class="modal-btn-row ui-modal-btn-row"></div>' +
            '</div>';
        document.body.appendChild(backdrop);
        // trigger reflow แล้วค่อยเปิด class .show เพื่อให้ transition เล่น
        requestAnimationFrame(() => requestAnimationFrame(() => backdrop.classList.add('show')));
        return backdrop;
    }

    function closeBackdrop(backdrop) {
        backdrop.classList.remove('show');
        setTimeout(() => backdrop.remove(), 300);
    }

    /**
     * แสดง modal แจ้งเตือนแทน alert()
     * @param {string} message ข้อความที่จะแสดง
     * @param {{type?: 'error'|'warning'|'success', title?: string}} [opts]
     * @returns {Promise<void>} resolve เมื่อผู้ใช้กดตกลง
     */
    function showAlertModal(message, opts = {}) {
        const type = opts.type || 'success';
        const title = opts.title || DEFAULT_TITLE[type];

        return new Promise((resolve) => {
            const backdrop = buildBackdrop();
            backdrop.querySelector('.ui-modal-icon').classList.add(type);
            backdrop.querySelector('.ui-modal-icon-glyph').classList.add(ICON_CLASS[type]);
            backdrop.querySelector('.ui-modal-title').textContent = title;
            backdrop.querySelector('.ui-modal-desc').textContent = message;

            const okBtn = document.createElement('button');
            okBtn.type = 'button';
            okBtn.className = 'modal-btn-primary';
            okBtn.innerHTML = '<i class="fas fa-check"></i> ตกลง';
            okBtn.onclick = () => { closeBackdrop(backdrop); resolve(); };
            backdrop.querySelector('.ui-modal-btn-row').appendChild(okBtn);
            okBtn.focus();
        });
    }

    /**
     * แสดง modal ยืนยันแทน confirm()
     * @param {string} message ข้อความที่จะแสดง
     * @param {{title?: string, confirmText?: string, cancelText?: string, danger?: boolean}} [opts]
     * @returns {Promise<boolean>} resolve(true) ถ้ากดยืนยัน, resolve(false) ถ้ายกเลิก
     */
    function showConfirmModal(message, opts = {}) {
        const danger = !!opts.danger;
        const title = opts.title || (danger ? 'ยืนยันการลบ' : 'ยืนยันการทำรายการ');
        const confirmText = opts.confirmText || 'ยืนยัน';
        const cancelText = opts.cancelText || 'ยกเลิก';

        return new Promise((resolve) => {
            const backdrop = buildBackdrop();
            backdrop.querySelector('.ui-modal-icon').classList.add(danger ? 'warning' : 'success');
            backdrop.querySelector('.ui-modal-icon-glyph').classList.add(danger ? ICON_CLASS.warning : ICON_CLASS.success);
            backdrop.querySelector('.ui-modal-title').textContent = title;
            backdrop.querySelector('.ui-modal-desc').textContent = message;

            const btnRow = backdrop.querySelector('.ui-modal-btn-row');

            const cancelBtn = document.createElement('button');
            cancelBtn.type = 'button';
            cancelBtn.className = 'modal-btn-secondary';
            cancelBtn.textContent = cancelText;
            cancelBtn.onclick = () => { closeBackdrop(backdrop); resolve(false); };

            const confirmBtn = document.createElement('button');
            confirmBtn.type = 'button';
            confirmBtn.className = 'modal-btn-primary' + (danger ? ' modal-btn-danger' : '');
            confirmBtn.innerHTML = '<i class="fas fa-check"></i> ' + confirmText;
            confirmBtn.onclick = () => { closeBackdrop(backdrop); resolve(true); };

            btnRow.appendChild(cancelBtn);
            btnRow.appendChild(confirmBtn);
            confirmBtn.focus();
        });
    }

    window.showAlertModal = showAlertModal;
    window.showConfirmModal = showConfirmModal;
})();

/* ═══════════════════════════════════════════
    MULTI-IMAGE UPLOAD
 ═══════════════════════════════════════════ */
const MAX_IMAGES = 5;
let imageDataList = [];
let primaryIndex = 0;

const fileInput = document.getElementById('fileInput');
const imgGrid = document.getElementById('imgGrid');
const imgCountNote = document.getElementById('imgCountNote');
const imagesInput = document.getElementById('imagesInput');
const uploadZone = document.getElementById('uploadZone');

fileInput.addEventListener('change', function () {
    addFiles(Array.from(this.files));
    this.value = '';
});

function handleDragOver(e) {
    e.preventDefault();
    uploadZone.classList.add('drag-over');
}
function handleDragLeave(e) {
    uploadZone.classList.remove('drag-over');
}
function handleDrop(e) {
    e.preventDefault();
    uploadZone.classList.remove('drag-over');
    addFiles(Array.from(e.dataTransfer.files));
}

function addFiles(files) {
    const allowed = MAX_IMAGES - imageDataList.length;
    const validFiles = files
        .filter(file => file.type.match(/^image\/(jpeg|png|webp)$/))
        .slice(0, allowed);

    // แจ้งผู้ใช้เมื่อมีไฟล์ถูกตัดทิ้ง (ผิดชนิด หรือเกินโควตา)
    if (validFiles.length < files.length) {
        const invalidCount = files.filter(f => !f.type.match(/^image\/(jpeg|png|webp)$/)).length;
        if (invalidCount > 0) {
            showAlertModal('มีไฟล์บางไฟล์ไม่ใช่รูปภาพชนิด jpg/png/webp จึงถูกข้ามไป', { type: 'warning' });
        } else if (allowed <= 0 || files.length > allowed) {
            showAlertModal(`เลือกรูปได้สูงสุด ${MAX_IMAGES} รูป มีบางไฟล์ถูกข้ามไปเนื่องจากเกินจำนวนที่กำหนด`, { type: 'warning' });
        }
    }

    if (validFiles.length === 0) return;

    let loaded = 0;
    validFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = e => {
            imageDataList.push({ base64: e.target.result, name: file.name });
            loaded++;
            if (loaded === validFiles.length) renderGrid();
        };
        reader.readAsDataURL(file);
    });
}

function renderGrid() {
    imgGrid.innerHTML = '';
    imageDataList.forEach((img, i) => {
        const thumb = document.createElement('div');
        thumb.className = 'img-thumb' + (i === primaryIndex ? ' primary' : '');
        thumb.innerHTML = `
    <img src="${img.base64}" alt="${img.name}"/>
    <div class="thumb-overlay">
        <button type="button" class="thumb-btn star-btn" onclick="setPrimary(${i})" title="ตั้งเป็นรูปหลัก">
            <i class="fas fa-star"></i>
        </button>
        <button type="button" class="thumb-btn del-btn" onclick="removeImage(${i})" title="ลบรูป">
            <i class="fas fa-trash"></i>
        </button>
    </div>
    ${i === primaryIndex ? '<div class="primary-badge">หลัก</div>' : ''}
`;
        imgGrid.appendChild(thumb);
    });

    if (imageDataList.length > 0) {
        imgCountNote.style.display = 'block';
        imgCountNote.textContent = `เลือกแล้ว ${imageDataList.length}/${MAX_IMAGES} รูป · คลิก ⭐ เพื่อตั้งรูปหลัก`;
    } else {
        imgCountNote.style.display = 'none';
    }

    const ordered = imageDataList.map((img, i) => ({ base64: img.base64, primary: i === primaryIndex }));
    imagesInput.value = JSON.stringify(ordered);
}

function setPrimary(index) {
    primaryIndex = index;
    renderGrid();
}

function removeImage(index) {
    imageDataList.splice(index, 1);
    if (index < primaryIndex) {
        primaryIndex--;
    } else if (index === primaryIndex) {
        primaryIndex = 0;
    }
    if (primaryIndex >= imageDataList.length) primaryIndex = 0;
    renderGrid();
}

/* ═══════════════════════════════════════════
   รอบทัวร์ (SCHEDULES) — เพิ่ม/ลบ ก่อนบันทึกทัวร์
═══════════════════════════════════════════ */
let scheduleList = []; // [{opendate, enddate, status, groupId?}]

const scheduleOpenDate = document.getElementById('scheduleOpenDate');
const scheduleEndDate = document.getElementById('scheduleEndDate');
const scheduleEndHint = document.getElementById('scheduleEndHint');
const scheduleStatus = document.getElementById('scheduleStatus');
const addScheduleBtn = document.getElementById('addScheduleBtn');
const scheduleTable = document.getElementById('scheduleTable');
const scheduleTableBody = document.getElementById('scheduleTableBody');
const scheduleEmptyNote = document.getElementById('scheduleEmptyNote');
const schedulesInput = document.getElementById('schedulesInput');

// ── สลับโหมด ──
const modeSingleBtn = document.getElementById('modeSingleBtn');
const modeRangeBtn = document.getElementById('modeRangeBtn');
const singleAddMode = document.getElementById('singleAddMode');
const rangeAddMode = document.getElementById('rangeAddMode');

function setScheduleMode(mode) {
    const isSingle = mode === 'single';
    singleAddMode.style.display = isSingle ? 'block' : 'none';
    rangeAddMode.style.display = isSingle ? 'none' : 'block';
    modeSingleBtn.classList.toggle('active', isSingle);
    modeRangeBtn.classList.toggle('active', !isSingle);
}

// บังคับให้ใช้ได้ทีละโหมดต่อทัวร์หนึ่งๆ: เช็คว่ารอบทัวร์ที่มีอยู่ตอนนี้
function currentUsageMode() {
    if (scheduleList.length === 0) return null;
    const hasRange = scheduleList.some(s => s.groupId);
    const hasSingle = scheduleList.some(s => !s.groupId);
    if (hasRange && hasSingle) return 'mixed';
    return hasRange ? 'range' : 'single';
}

async function tryToSwitchMode(mode) {
    const usage = currentUsageMode();
    if (!usage || usage === mode) {
        setScheduleMode(mode);
        return;
    }
    const usageLabel = usage === 'range' ? 'เปิดทั้งช่วง (เปิดทุกวัน)' : 'เพิ่มทีละรอบ';
    const confirmed = await showConfirmModal(
        `ทัวร์นี้เพิ่มรอบทัวร์แบบ "${usageLabel}" ไว้แล้ว ${scheduleList.length} รอบ\n` +
        `ระบบอนุญาตให้ใช้ได้ทีละแบบต่อทัวร์เท่านั้น เพื่อป้องกันความสับสนตอนจัดการรอบทัวร์ทีหลัง\n\n` +
        `หากสลับโหมด รอบทัวร์ทั้งหมดที่เพิ่มไว้จะถูกลบทิ้ง ต้องการดำเนินการต่อหรือไม่?`,
        { danger: true, confirmText: 'สลับโหมด' }
    );
    if (!confirmed) return;

    scheduleList = [];
    renderScheduleTable();
    exceptionDates = {};
    renderExceptionList();
    rangePreviewNote.style.display = 'none';
    setScheduleMode(mode);
}

modeSingleBtn.addEventListener('click', () => tryToSwitchMode('single'));
modeRangeBtn.addEventListener('click', () => tryToSwitchMode('range'));

function isDailyTour() {
    return tourtypeSelect.value === 'ทัวร์รายวัน';
}

// ทัวร์รายวัน: ล็อกวันที่จบให้เท่ากับวันที่เริ่มเสมอ 
function applyScheduleDailyLock() {
    if (isDailyTour()) {
        scheduleEndDate.readOnly = true;
        scheduleEndHint.style.visibility = 'visible';
        if (scheduleOpenDate.value) scheduleEndDate.value = scheduleOpenDate.value;
    } else {
        scheduleEndDate.readOnly = false;
        scheduleEndHint.style.visibility = 'hidden';
    }
}

scheduleOpenDate.addEventListener('change', () => {
    if (isDailyTour()) scheduleEndDate.value = scheduleOpenDate.value;
    clearErr('schedules', 'err-schedules');
});

function formatThaiDate(iso) {
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
}

function addDaysISO(iso, days) {
    const [y, m, d] = iso.split('-').map(Number);
    const date = new Date(Date.UTC(y, m - 1, d));
    date.setUTCDate(date.getUTCDate() + days);
    return date.toISOString().slice(0, 10);
}

function statusBadge(status) {
    const cls = status === 'ปิด' ? 'st-closed' : 'st-open';
    return `<span class="schedule-status-badge ${cls}">${status}</span>`;
}

// ── ตรวจสอบวันที่ทับซ้อน: ป้องกันไม่ให้เพิ่มรอบทัวร์ที่วันที่คาบเกี่ยวกับรอบที่มีอยู่แล้ว
function rangesOverlap(aStart, aEnd, bStart, bEnd) {
    return aStart <= bEnd && bStart <= aEnd;
}
function findConflict(newStart, newEnd) {
    return scheduleList.find(s => rangesOverlap(newStart, newEnd, s.opendate, s.enddate));
}

function renderScheduleTable() {
    scheduleTableBody.innerHTML = '';

    // ── group แถวที่มี groupId เดียวกันและติดกัน ให้แสดงเป็น 1 แถวสรุป
    let i = 0;
    while (i < scheduleList.length) {
        const s = scheduleList[i];
        if (s.groupId) {
            let j = i;
            while (j < scheduleList.length &&
                scheduleList[j].groupId === s.groupId &&
                scheduleList[j].status === s.status) {
                j++;
            }
            const first = scheduleList[i];
            const last = scheduleList[j - 1];
            const count = j - i;
            const tr = document.createElement('tr');
            tr.innerHTML = `
    <td>${formatThaiDate(first.opendate)} <span style="color:#6b6b68;">ถึง</span> ${formatThaiDate(last.opendate)}
        <div style="font-size:11px;color:var(--text-hint);margin-top:2px;">เปิดทุกวัน · รวม ${count} วัน</div></td>
    <td>${statusBadge(first.status)}</td>
    <td><button type="button" class="schedule-del-btn" onclick="removeScheduleGroup('${s.groupId}')" title="ลบทั้งช่วงนี้"><i class="fas fa-trash"></i></button></td>
`;
            scheduleTableBody.appendChild(tr);
            i = j;
        } else {
            const tr = document.createElement('tr');
            tr.innerHTML = `
    <td>${formatThaiDate(s.opendate)} <span style="color:#6b6b68;">-</span> ${formatThaiDate(s.enddate)}</td>
    <td>${statusBadge(s.status)}</td>
    <td><button type="button" class="schedule-del-btn" onclick="removeSchedule(${i})" title="ลบรอบนี้"><i class="fas fa-trash"></i></button></td>
`;
            scheduleTableBody.appendChild(tr);
            i++;
        }
    }

    const has = scheduleList.length > 0;
    scheduleTable.style.display = has ? 'table' : 'none';
    scheduleEmptyNote.style.display = has ? 'none' : 'block';
    schedulesInput.value = JSON.stringify(
        scheduleList.map(({ opendate, enddate, status }) => ({ opendate, enddate, status }))
    );
}

function removeSchedule(index) {
    scheduleList.splice(index, 1);
    renderScheduleTable();
}

function removeScheduleGroup(groupId) {
    scheduleList = scheduleList.filter(s => s.groupId !== groupId);
    renderScheduleTable();
}

addScheduleBtn.addEventListener('click', () => {
    const open = scheduleOpenDate.value;
    // ทัวร์รายวัน: วันที่เริ่มกับวันที่จบต้องเท่ากันเสมอ
    let end = isDailyTour() ? open : scheduleEndDate.value;

    if (!open) {
        showErr('scheduleOpenDate', 'err-schedules', 'กรุณาเลือกวันที่เริ่มทัวร์');
        scheduleOpenDate.classList.add('error');
        return;
    }
    if (!end) {
        showErr('scheduleEndDate', 'err-schedules', 'กรุณาเลือกวันที่จบทัวร์');
        scheduleEndDate.classList.add('error');
        return;
    }
    if (end < open) {
        showErr('scheduleEndDate', 'err-schedules', 'วันที่จบทัวร์ต้องไม่ก่อนหน้าวันที่เริ่มทัวร์');
        scheduleEndDate.classList.add('error');
        return;
    }

    // กันวันที่ทับซ้อนกับรอบทัวร์ที่มีอยู่แล้ว
    const conflict = findConflict(open, end);
    if (conflict) {
        showErr('scheduleOpenDate', 'err-schedules',
            `ช่วงวันที่นี้ทับซ้อนกับรอบทัวร์ที่มีอยู่แล้ว (${formatThaiDate(conflict.opendate)} - ${formatThaiDate(conflict.enddate)}) กรุณาเลือกวันที่อื่น`);
        scheduleOpenDate.classList.add('error');
        scheduleEndDate.classList.add('error');
        return;
    }

    scheduleOpenDate.classList.remove('error');
    scheduleEndDate.classList.remove('error');
    clearErr('schedules', 'err-schedules');

    scheduleList.push({ opendate: open, enddate: end, status: scheduleStatus.value, groupId: null });
    renderScheduleTable();

    scheduleOpenDate.value = '';
    scheduleEndDate.value = '';
    scheduleStatus.value = 'เปิดรับจอง';
});

// ── ปุ่ม "สร้างรอบทัวร์ทั้งช่วง" (เปิดทุกวัน) ──
const rangeStartDate = document.getElementById('rangeStartDate');
const rangeEndDate = document.getElementById('rangeEndDate');
const rangeStatus = document.getElementById('rangeStatus');
const addScheduleRangeBtn = document.getElementById('addScheduleRangeBtn');
const MAX_RANGE_DAYS = 366;

// ═══════════════════════════════════════════
// สถานะพิเศษเฉพาะวัน สำหรับโหมด "เปิดทุกวัน"
// ═══════════════════════════════════════════
let exceptionDates = {};

const exceptionDateInput = document.getElementById('exceptionDate');
const exceptionStatusSelect = document.getElementById('exceptionStatus');
const addExceptionBtn = document.getElementById('addExceptionBtn');
const exceptionList = document.getElementById('exceptionList');

// ── group วันที่ต่อเนื่องกัน (ติดกันวันต่อวัน) ที่มีสถานะเดียวกัน ให้เป็นช่วงเดียว ──
function groupConsecutiveDates(sortedDates) {
    const groups = [];
    let current = null;
    sortedDates.forEach(({ date, status }) => {
        if (current && current.status === status && addDaysISO(current.end, 1) === date) {
            current.end = date;
        } else {
            if (current) groups.push(current);
            current = { start: date, end: date, status };
        }
    });
    if (current) groups.push(current);
    return groups;
}

function renderExceptionList() {
    exceptionList.innerHTML = '';

    const items = Object.keys(exceptionDates)
        .sort()
        .map(date => ({ date, status: exceptionDates[date] }));
    const groups = groupConsecutiveDates(items);

    groups.forEach(group => {
        const isClosed = group.status === 'ปิด';
        const isSingleDay = group.start === group.end;
        const dayCount = Math.round((new Date(group.end) - new Date(group.start)) / 86400000) + 1;

        const label = isSingleDay
            ? formatThaiDate(group.start)
            : `${formatThaiDate(group.start)} - ${formatThaiDate(group.end)} (${dayCount} วัน)`;

        const chip = document.createElement('span');
        chip.style.cssText =
            'display:inline-flex;align-items:center;gap:6px;font-size:12px;' +
            'padding:4px 6px 4px 10px;border-radius:20px;border:1px solid var(--border);' +
            'background:' + (isClosed ? '#f0f0f0' : 'var(--green-light)') + ';' +
            'color:' + (isClosed ? 'var(--text-muted)' : 'var(--green-dark)') + ';font-weight:600;';
        chip.innerHTML =
            label + ' · ' + group.status +
            ' <button type="button" title="ลบช่วงนี้ออก" ' +
            'style="border:none;background:rgba(0,0,0,.08);width:16px;height:16px;border-radius:50%;' +
            'cursor:pointer;color:inherit;font-size:10px;line-height:1;display:inline-flex;' +
            'align-items:center;justify-content:center;">✕</button>';

        chip.querySelector('button').addEventListener('click', () => {

            let cursor = group.start;
            while (cursor <= group.end) {
                delete exceptionDates[cursor];
                cursor = addDaysISO(cursor, 1);
            }
            renderExceptionList();
            updateRangePreview();
        });

        exceptionList.appendChild(chip);
    });
}

const exceptionEndDateInput = document.getElementById('exceptionEndDate');

addExceptionBtn.addEventListener('click', () => {
    const start = exceptionDateInput.value;
    const end = exceptionEndDateInput.value || start;

    if (!start) {
        showErr('exceptionDate', 'err-exceptionDate', 'กรุณาเลือกวันที่');
        return;
    }
    if (end < start) {
        showErr('exceptionDate', 'err-exceptionDate', 'วันที่สิ้นสุดต้องไม่ก่อนวันที่เริ่ม');
        return;
    }

    exceptionDateInput.classList.remove('error');
    clearErr('exceptionDate', 'err-exceptionDate');

    let cursor = start;
    while (cursor <= end) {
        exceptionDates[cursor] = exceptionStatusSelect.value;
        cursor = addDaysISO(cursor, 1);
    }

    renderExceptionList();
    updateRangePreview();

    exceptionDateInput.value = '';
    exceptionEndDateInput.value = '';
});

// ── พรีวิวจำนวนรอบทัวร์ที่จะถูกสร้าง ก่อนกดยืนยัน ──
const rangePreviewNote = document.getElementById('rangePreviewNote');

function updateRangePreview() {
    const start = rangeStartDate.value;
    const end = rangeEndDate.value;
    if (!start || !end || end < start) {
        rangePreviewNote.style.display = 'none';
        return;
    }
    const dayCount = Math.round((new Date(end) - new Date(start)) / 86400000) + 1;
    const exceptionInRange = Object.keys(exceptionDates).filter(d => d >= start && d <= end).length;

    rangePreviewNote.style.display = 'block';
    rangePreviewNote.innerHTML = '<i class="fas fa-circle-info"></i> จะสร้างรอบทัวร์ทั้งหมด ' + dayCount + ' วัน' +
        (exceptionInRange > 0
            ? ' (มี ' + exceptionInRange + ' วันที่จะใช้สถานะพิเศษตามที่กำหนดไว้ด้านล่าง)'
            : '');
}

rangeStartDate.addEventListener('input', updateRangePreview);
rangeEndDate.addEventListener('input', updateRangePreview);

addScheduleRangeBtn.addEventListener('click', () => {
    const start = rangeStartDate.value;
    const end = rangeEndDate.value;

    if (!start) {
        showErr('rangeStartDate', 'err-schedules', 'กรุณาเลือกวันที่เริ่มช่วง');
        rangeStartDate.classList.add('error');
        return;
    }
    if (!end) {
        showErr('rangeEndDate', 'err-schedules', 'กรุณาเลือกวันที่สิ้นสุดช่วง');
        rangeEndDate.classList.add('error');
        return;
    }
    if (end < start) {
        showErr('rangeEndDate', 'err-schedules', 'วันที่สิ้นสุดช่วงต้องไม่ก่อนวันที่เริ่มช่วง');
        rangeEndDate.classList.add('error');
        return;
    }

    const dayCount = Math.round((new Date(end) - new Date(start)) / 86400000) + 1;
    if (dayCount > MAX_RANGE_DAYS) {
        showErr('rangeEndDate', 'err-schedules',
            `ช่วงวันที่ยาวเกินไป (สูงสุด ${MAX_RANGE_DAYS} วัน/ครั้ง) กรุณาแบ่งสร้างหลายครั้ง`);
        rangeEndDate.classList.add('error');
        return;
    }

    rangeStartDate.classList.remove('error');
    rangeEndDate.classList.remove('error');
    clearErr('schedules', 'err-schedules');

    // จำนวนวันของแต่ละรอบ: ทัวร์รายวัน = 1 วัน / ทัวร์หลายวัน = ตามจำนวนวันของทัวร์ที่กรอกไว้
    const tourLenDays = isDailyTour() ? 1 : (parseInt(daysInput.value) || 1);

    // กันวันที่ทับซ้อนกับรอบทัวร์ที่มีอยู่แล้ว: วันไหนชนกับรอบเดิมจะถูก "ข้าม" ไม่สร้างซ้ำ
    const groupId = 'grp_' + Date.now() + '_' + Math.random().toString(36).slice(2, 7);
    let cursor = start;
    let addedCount = 0;
    let skippedCount = 0;
    // ใช้สถานะพิเศษเฉพาะวัน (ถ้ามีกำหนดไว้) แทนสถานะเริ่มต้น 
    let exceptionAppliedCount = 0;
    for (let d = 0; d < dayCount; d++) {
        const opendate = cursor;
        const enddate = isDailyTour() ? opendate : addDaysISO(opendate, tourLenDays - 1);
        const statusForDay = exceptionDates.hasOwnProperty(opendate) ? exceptionDates[opendate] : rangeStatus.value;

        if (findConflict(opendate, enddate)) {
            skippedCount++;
        } else {
            scheduleList.push({ opendate, enddate, status: statusForDay, groupId });
            addedCount++;
            if (exceptionDates.hasOwnProperty(opendate)) exceptionAppliedCount++;
        }
        cursor = addDaysISO(cursor, 1);
    }

    renderScheduleTable();

    if (addedCount === 0) {
        showErr('rangeStartDate', 'err-schedules',
            'ทุกวันในช่วงนี้ทับซ้อนกับรอบทัวร์ที่มีอยู่แล้วทั้งหมด ไม่มีรอบใหม่ถูกสร้าง');
    } else if (skippedCount > 0) {
        clearErr('schedules', 'err-schedules');
        showAlertModal(`สร้างรอบทัวร์สำเร็จ ${addedCount} วัน (ข้ามไป ${skippedCount} วัน เพราะทับซ้อนกับรอบทัวร์ที่มีอยู่แล้ว)`, { type: 'success' });
    } else if (exceptionAppliedCount > 0) {
        clearErr('schedules', 'err-schedules');
        showAlertModal(`สร้างรอบทัวร์สำเร็จ ${addedCount} วัน (มี ${exceptionAppliedCount} วันที่ใช้สถานะพิเศษตามที่กำหนดไว้)`, { type: 'success' });
    }

    Object.keys(exceptionDates).forEach(d => {
        if (d >= start && d <= end) delete exceptionDates[d];
    });
    renderExceptionList();

    rangeStartDate.value = '';
    rangeEndDate.value = '';
    rangeStatus.value = 'เปิดรับจอง';
    rangePreviewNote.style.display = 'none';
});

renderScheduleTable();

/* ═══════════════════════════════════════════
   - "ทัวร์รายวัน" เท่านั้น → numberOfDays ล็อก = 1, numberOfNights ล็อก = 0
═══════════════════════════════════════════ */
const tourtypeSelect = document.getElementById('tourtypeSelect');
const daysInput = document.getElementById('numberOfDays');
const nightsInput = document.getElementById('numberOfNights');
const customTypeGroup = document.getElementById('customTypeGroup');
const tourtypeCustom = document.getElementById('tourtypeCustom');

function applyTourTypeRules() {
    const type = tourtypeSelect.value;

    if (type === 'ทัวร์รายวัน') {
        customTypeGroup.style.display = 'none';
        daysInput.readOnly = true;
        nightsInput.readOnly = true;
        daysInput.value = 1;
        nightsInput.value = 0;
    } else if (type === '') {
        customTypeGroup.style.display = 'none';
        daysInput.readOnly = false;
        nightsInput.readOnly = false;
    } else {
        // ทุกประเภทที่ไม่ใช่ทัวร์รายวัน→ ให้ผู้ใช้กรอกวัน/คืนเอง
        customTypeGroup.style.display = (type === '__custom__') ? 'block' : 'none';
        daysInput.readOnly = false;
        nightsInput.readOnly = false;
        // ถ้าค่าเดิมตกค้างมาจากโหมดทัวร์รายวัน (1/0) ให้เคลียร์เพื่อบังคับกรอกใหม่
        if (parseInt(daysInput.value) === 1 && parseInt(nightsInput.value) === 0) {
            daysInput.value = '';
            nightsInput.value = '';
        }
    }
    clearErr('numberOfDays', 'err-numberOfDays');
    clearErr('tourtypeSelect', 'err-tourtype');
    clearErr('tourtypeCustom', 'err-tourtypeCustom');
    applyScheduleDailyLock();
}

tourtypeSelect.addEventListener('change', applyTourTypeRules);
tourtypeCustom.addEventListener('input', () => clearErr('tourtypeCustom', 'err-tourtypeCustom'));

/* ═══════════════════════════════════════════
   จุดรับ / นัดพบ — เปิด/ปิดช่องกรอกรายละเอียดตาม checkbox
═══════════════════════════════════════════ */
const allowMeetingPointChk = document.getElementById('allowMeetingPointChk');
const meetingPointDetailGroup = document.getElementById('meetingPointDetailGroup');
const meetingPointDetailInput = document.getElementById('meetingPointDetail');
const allowHotelPickupChk = document.getElementById('allowHotelPickupChk');
const hotelPickupAreaGroup = document.getElementById('hotelPickupAreaGroup');
const hotelPickupAreaInput = document.getElementById('hotelPickupArea');

function applyPickupRules() {
    meetingPointDetailGroup.style.display = allowMeetingPointChk.checked ? 'block' : 'none';
    hotelPickupAreaGroup.style.display = allowHotelPickupChk.checked ? 'block' : 'none';
    clearErr('meetingPointDetail', 'err-meetingPointDetail');
    clearErr('hotelPickupArea', 'err-hotelPickupArea');
    document.getElementById('err-pickupOption')?.classList.remove('show');

    if (allowMeetingPointChk.checked) initMeetingMap();
    if (allowHotelPickupChk.checked) initHotelSearch();
}

allowMeetingPointChk.addEventListener('change', applyPickupRules);
allowHotelPickupChk.addEventListener('change', applyPickupRules);
meetingPointDetailInput.addEventListener('input', () => clearErr('meetingPointDetail', 'err-meetingPointDetail'));
hotelPickupAreaInput.addEventListener('input', () => clearErr('hotelPickupArea', 'err-hotelPickupArea'));

window.addEventListener('DOMContentLoaded', applyPickupRules);

/* ═══════════════════════════════════════════
   LEAFLET + OPENSTREETMAP — ช่วยหาที่อยู่ (จุดรวมพล / เขตรับที่โรงแรม)
═══════════════════════════════════════════ */
const DEFAULT_MAP_CENTER = [18.7883, 98.9853]; // ศูนย์กลางเชียงใหม่ (ปรับตามพื้นที่ให้บริการจริงได้)
const NOMINATIM_BASE = 'https://nominatim.openstreetmap.org';

let meetingMap, meetingMarker;
let hotelMap, hotelMarker;

function createLeafletMap(divId) {
    const map = L.map(divId, { center: DEFAULT_MAP_CENTER, zoom: 13 });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);
    return map;
}

function reverseGeocode(lat, lng, callback) {
    fetch(`${NOMINATIM_BASE}/reverse?format=jsonv2&lat=${lat}&lon=${lng}&accept-language=th`)
        .then(res => {
            if (!res.ok) throw new Error('Nominatim status ' + res.status);
            return res.json();
        })
        .then(data => {
            if (data && data.display_name) {
                callback(data.display_name);
            } else {
                console.warn('ไม่พบชื่อสถานที่สำหรับพิกัดนี้', data);
                callback(`พิกัด: ${lat.toFixed(5)}, ${lng.toFixed(5)} (ไม่พบชื่อสถานที่ กรุณาพิมพ์เอง)`);
            }
        })
        .catch(err => {
            console.error('reverseGeocode ล้มเหลว:', err);
            callback(`พิกัด: ${lat.toFixed(5)}, ${lng.toFixed(5)} (โหลดชื่อสถานที่ไม่สำเร็จ กรุณาพิมพ์เอง)`);
        });
}

function searchPlaces(query, callback) {
    if (!query || query.trim().length < 3) { callback([]); return; }
    const params = new URLSearchParams({
        format: 'jsonv2',
        q: query,
        countrycodes: 'th',
        'accept-language': 'th',
        limit: '5'
    });
    fetch(`${NOMINATIM_BASE}/search?${params.toString()}`)
        .then(res => res.json())
        .then(data => callback(data || []))
        .catch(() => callback([]));
}

/* ── ผูก search box + suggestion dropdown ให้แต่ละช่อง input ── */
function attachPlaceSearch(inputEl, suggestBoxEl, onSelect, timerRef) {
    inputEl.addEventListener('input', () => {
        clearTimeout(timerRef.id);
        const query = inputEl.value;
        timerRef.id = setTimeout(() => {
            suggestBoxEl.innerHTML = '<div class="search-suggest-loading">กำลังค้นหา...</div>';
            suggestBoxEl.style.display = 'block';
            searchPlaces(query, (results) => {
                if (results.length === 0) {
                    suggestBoxEl.style.display = 'none';
                    return;
                }
                suggestBoxEl.innerHTML = '';
                results.forEach(place => {
                    const item = document.createElement('div');
                    item.className = 'search-suggest-item';
                    item.textContent = place.display_name;
                    item.addEventListener('click', () => {
                        inputEl.value = place.display_name;
                        suggestBoxEl.style.display = 'none';
                        onSelect(parseFloat(place.lat), parseFloat(place.lon), place.display_name);
                    });
                    suggestBoxEl.appendChild(item);
                });
                suggestBoxEl.style.display = 'block';
            });
        }, 500);
    });

    document.addEventListener('click', (e) => {
        if (!inputEl.contains(e.target) && !suggestBoxEl.contains(e.target)) {
            suggestBoxEl.style.display = 'none';
        }
    });
}

/* ── หาตำแหน่งปัจจุบันของผู้ใช้  ── */
function getCurrentPositionOrDefault(onLocated, onFallback) {
    if (!navigator.geolocation) {
        onFallback();
        return;
    }
    navigator.geolocation.getCurrentPosition(
        (pos) => onLocated([pos.coords.latitude, pos.coords.longitude]),
        () => onFallback(),
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
    );
}

/* ── จุดรวมพล ── */
function initMeetingMap() {
    if (meetingMap) return;

    meetingMap = createLeafletMap('meetingPointMap');
    meetingMarker = L.marker(DEFAULT_MAP_CENTER, { draggable: true }).addTo(meetingMap);

    meetingMarker.on('dragend', () => {
        const pos = meetingMarker.getLatLng();
        reverseGeocode(pos.lat, pos.lng, (address) => {
            meetingPointDetailInput.value = address;
            clearErr('meetingPointDetail', 'err-meetingPointDetail');
        });
    });

    meetingMap.on('click', (e) => {
        meetingMarker.setLatLng(e.latlng);
        reverseGeocode(e.latlng.lat, e.latlng.lng, (address) => {
            meetingPointDetailInput.value = address;
            clearErr('meetingPointDetail', 'err-meetingPointDetail');
        });
    });

    setTimeout(() => meetingMap.invalidateSize(), 200);

    //  ปักหมุดที่ตำแหน่งปัจจุบันของผู้ใช้อัตโนมัติ
    if (!meetingPointDetailInput.value.trim()) {
        getCurrentPositionOrDefault(
            ([lat, lng]) => {
                meetingMap.setView([lat, lng], 16);
                meetingMarker.setLatLng([lat, lng]);
                reverseGeocode(lat, lng, (address) => {
                    meetingPointDetailInput.value = address;
                    clearErr('meetingPointDetail', 'err-meetingPointDetail');
                });
            },
            () => { }
        );
    }

    attachPlaceSearch(
        meetingPointDetailInput,
        document.getElementById('meetingPointSuggest'),
        (lat, lng) => {
            meetingMap.setView([lat, lng], 16);
            meetingMarker.setLatLng([lat, lng]);
            clearErr('meetingPointDetail', 'err-meetingPointDetail');
        },
        { id: null }
    );
}

/* ── เขตรับที่โรงแรม */
function initHotelSearch() {
    if (initHotelSearch._bound) return;
    initHotelSearch._bound = true;

    attachPlaceSearch(
        hotelPickupAreaInput,
        document.getElementById('hotelPickupSuggest'),
        (lat, lng, displayName) => {
            // ไม่มีแผนที่ให้ปัก แค่เคลียร์ error
            clearErr('hotelPickupArea', 'err-hotelPickupArea');
        },
        { id: null }
    );
}

// เผื่อมีค่าเดิมติดมาจาก server
window.addEventListener('DOMContentLoaded', () => {
    const savedType = /*[[${tour?.tourtype?.typename}]]*/ '';
    if (savedType) {
        const KNOWN_TYPES = ['ทัวร์รายวัน', 'ทัวร์วัฒนธรรมชนเผ่า', 'ทัวร์วิถีชีวิต'];
        if (KNOWN_TYPES.includes(savedType)) {
            tourtypeSelect.value = savedType;
        } else {
            tourtypeSelect.value = '__custom__';
            tourtypeCustom.value = savedType;
        }
        const d = parseInt(daysInput.value);
        const n = parseInt(nightsInput.value);
        applyTourTypeRules();
        if (!isNaN(d)) daysInput.value = d;
        if (!isNaN(n)) nightsInput.value = n;
    }
});

// ตรวจ "จำนวนวัน/คืน" แบบ real-time 
function validateDaysNights() {
    if (tourtypeSelect.value === 'ทัวร์รายวัน') {
        clearErr('numberOfDays', 'err-numberOfDays');
        return;
    }
    const dVal = daysInput.value;
    const nVal = nightsInput.value;
    const d = parseInt(dVal);
    const n = parseInt(nVal);

    if (isInvalidNumeric(dVal, true)) {
        showErr('numberOfDays', 'err-numberOfDays', 'จำนวนวันต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)');
        return;
    }
    if (isInvalidNumeric(nVal, false)) {
        showErr('numberOfDays', 'err-numberOfDays', 'จำนวนคืนต้องไม่ติดลบ');
        return;
    }
    if (!isNaN(d) && !isNaN(n) && n >= d) {
        showErr('numberOfDays', 'err-numberOfDays', 'จำนวนคืนต้องน้อยกว่าจำนวนวัน');
        return;
    }
    clearErr('numberOfDays', 'err-numberOfDays');
}
nightsInput.addEventListener('input', validateDaysNights);
daysInput.addEventListener('input', validateDaysNights);

/* ═══════════════════════════════════════════
   VALIDATION
═══════════════════════════════════════════ */
const REQUIRED = [
    { id: 'tourmname', errId: 'err-tourmname', msg: 'กรุณากรอกชื่อทัวร์' },
    { id: 'tourtypeSelect', errId: 'err-tourtype', msg: 'กรุณาเลือกประเภททัวร์' },
    { id: 'tourdetail', errId: 'err-tourdetail', msg: 'กรุณากรอกรายละเอียดทัวร์' },
    { id: 'conditiontour', errId: 'err-conditiontour', msg: 'กรุณากรอกเงื่อนไขทัวร์' },
    { id: 'minSeatstour', errId: 'err-minSeatstour', msg: 'กรุณาระบุจำนวนที่นั่งขั้นต่ำ' },
    { id: 'maxSeatstour', errId: 'err-maxSeatstour', msg: 'กรุณาระบุจำนวนที่นั่งสูงสุด' },
    { id: 'adultprice', errId: 'err-adultprice', msg: 'กรุณาระบุราคาผู้ใหญ่' },
    { id: 'childprice', errId: 'err-childprice', msg: 'กรุณาระบุราคาเด็ก' },
    { id: 'meetingTime', errId: 'err-meetingTime', msg: 'กรุณาระบุเวลานัดพบ' },
];

REQUIRED.forEach(f => {
    const el = document.getElementById(f.id);
    if (!el) return;
    const evt = el.tagName === 'SELECT' ? 'change' : 'input';
    el.addEventListener(evt, () => clearErr(f.id, f.errId));
});

// เช็คค่าตัวเลข: ห้ามติดลบทุกช่อง และช่องที่ห้ามเป็น 0 

const NUMERIC_MIN = [
    { id: 'numberOfDays', errId: 'err-numberOfDays', msg: 'จำนวนวันต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)', mustBePositive: true },
    { id: 'numberOfNights', errId: 'err-numberOfDays', msg: 'จำนวนคืนต้องไม่ติดลบ', mustBePositive: false },
    { id: 'minSeatstour', errId: 'err-minSeatstour', msg: 'ที่นั่งขั้นต่ำต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)', mustBePositive: true },
    { id: 'maxSeatstour', errId: 'err-maxSeatstour', msg: 'ที่นั่งสูงสุดต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)', mustBePositive: true },
    { id: 'adultprice', errId: 'err-adultprice', msg: 'ราคาผู้ใหญ่ต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)', mustBePositive: true },
    { id: 'childprice', errId: 'err-childprice', msg: 'ราคาเด็กต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)', mustBePositive: true },
];

function isInvalidNumeric(val, mustBePositive) {
    if (val === '' || isNaN(parseFloat(val))) return false;
    const num = parseFloat(val);
    return mustBePositive ? num <= 0 : num < 0;
}

NUMERIC_MIN
    .filter(f => !['numberOfDays', 'numberOfNights', 'minSeatstour', 'maxSeatstour'].includes(f.id))
    .forEach(f => {
        const el = document.getElementById(f.id);
        if (!el) return;
        // ผูก listener นี้ "หลัง" listener อื่นๆ ที่ clearErr ไปแล้ว (REQUIRED / tour-type)
        // เพื่อให้ข้อความ error เรื่องค่าไม่ถูกต้องแสดงทับได้เสมอถ้ายังไม่ถูกต้องอยู่
        el.addEventListener('input', () => {
            if (isInvalidNumeric(el.value, f.mustBePositive)) {
                showErr(f.id, f.errId, f.msg);
            } else {
                clearErr(f.id, f.errId);
            }
        });
    });

//  ที่นั่งขั้นต่ำ/สูงสุด รวมเช็คเป็นฟังก์ชันเดียว (เหมือน validateDaysNights())
//    เพื่อไม่ให้ error "สูงสุดต้องมากกว่าขั้นต่ำ" ถูกเคลียร์ทับตอนพิมพ์อีกช่อง
function validateSeats() {
    const minEl = document.getElementById('minSeatstour');
    const maxEl = document.getElementById('maxSeatstour');
    const minVal = minEl.value, maxVal = maxEl.value;
    const min = parseInt(minVal), max = parseInt(maxVal);

    if (isInvalidNumeric(minVal, true)) {
        showErr('minSeatstour', 'err-minSeatstour', 'ที่นั่งขั้นต่ำต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)');
    } else {
        clearErr('minSeatstour', 'err-minSeatstour');
    }

    if (isInvalidNumeric(maxVal, true)) {
        showErr('maxSeatstour', 'err-maxSeatstour', 'ที่นั่งสูงสุดต้องมากกว่า 0 (ห้ามเป็น 0 หรือติดลบ)');
        return;
    }

    if (!isNaN(min) && !isNaN(max) && min >= max) {
        showErr('maxSeatstour', 'err-maxSeatstour', 'จำนวนที่นั่งสูงสุดต้องมากกว่าที่นั่งขั้นต่ำ');
        return;
    }
    clearErr('maxSeatstour', 'err-maxSeatstour');
}
document.getElementById('minSeatstour').addEventListener('input', validateSeats);
document.getElementById('maxSeatstour').addEventListener('input', validateSeats);

function showErr(id, errId, msg) {
    document.getElementById(id)?.classList.add('error');
    const err = document.getElementById(errId);
    if (err) { err.textContent = msg; err.classList.add('show'); }
}
function clearErr(id, errId) {
    document.getElementById(id)?.classList.remove('error');
    document.getElementById(errId)?.classList.remove('show');
}

/* ═══════════════════════════════════════════
   SUCCESS MODAL + COUNTDOWN + REDIRECT
   ✅ แก้ไข: รับ redirectUrl จริงจาก server มาใช้ตอน redirect
             แทนที่จะ hardcode ไปหน้า list เสมอ
═══════════════════════════════════════════ */
function showSuccessModal(redirectUrl) {
    const modal = document.getElementById('successModal');
    const fill = document.getElementById('progressFill');

    modal.classList.add('show');

    fill.style.animation = 'none';
    fill.offsetHeight;
    fill.style.animation = 'progress-drain 2s linear forwards';

    setTimeout(() => {
        window.location.href = redirectUrl;
    }, 2000);
}

document.getElementById('tourForm').addEventListener('submit', function (e) {
    let valid = true;

    REQUIRED.forEach(f => {
        const el = document.getElementById(f.id);
        if (!el) return;
        const empty = el.tagName === 'SELECT' ? el.value === '' : el.value.trim() === '';
        if (empty) { showErr(f.id, f.errId, f.msg); valid = false; }
        else clearErr(f.id, f.errId);
    });

    // ✅ เช็คว่านั่งขั้นต่ำต้องน้อยกว่า (strict) ที่นั่งสูงสุดเสมอ ห้ามเท่ากัน
    const minV = parseInt(document.getElementById('minSeatstour').value);
    const maxV = parseInt(document.getElementById('maxSeatstour').value);
    if (!isNaN(minV) && !isNaN(maxV) && minV >= maxV) {
        showErr('maxSeatstour', 'err-maxSeatstour', 'จำนวนที่นั่งสูงสุดต้องมากกว่าที่นั่งขั้นต่ำ');
        valid = false;
    }

    // ─── กันค่าไม่ถูกต้องซ้ำอีกชั้นตอน submit (เผื่อกรณี input event ไม่ทำงาน เช่น autofill) ───
    NUMERIC_MIN.forEach(f => {
        const el = document.getElementById(f.id);
        if (!el) return;
        if (isInvalidNumeric(el.value, f.mustBePositive)) {
            showErr(f.id, f.errId, f.msg);
            valid = false;
        }
    });

    // ─── ต้องเปิดอย่างน้อย 1 ช่องทางรับ-ส่ง และกรอกรายละเอียดให้ครบ ───
    if (!allowMeetingPointChk.checked && !allowHotelPickupChk.checked) {
        const err = document.getElementById('err-pickupOption');
        if (err) err.classList.add('show');
        valid = false;
    }
    if (allowMeetingPointChk.checked && meetingPointDetailInput.value.trim() === '') {
        showErr('meetingPointDetail', 'err-meetingPointDetail', 'กรุณาระบุสถานที่จุดรวมพล');
        valid = false;
    }
    if (allowHotelPickupChk.checked && hotelPickupAreaInput.value.trim() === '') {
        showErr('hotelPickupArea', 'err-hotelPickupArea', 'กรุณาระบุเขตพื้นที่ที่รับได้');
        valid = false;
    }

    // ─── ต้องมีอย่างน้อย 1 รอบทัวร์ก่อนบันทึก ───
    if (scheduleList.length === 0) {
        showErr('scheduleOpenDate', 'err-schedules', 'กรุณาเพิ่มอย่างน้อย 1 รอบทัวร์ (วันที่เปิดทัวร์)');
        valid = false;
    }

    // ─── ตรวจสอบความสอดคล้องของประเภททัวร์กับจำนวนวัน/คืน ───
    if (tourtypeSelect.value === '') {
        showErr('tourtypeSelect', 'err-tourtype', 'กรุณาเลือกประเภททัวร์');
        valid = false;
    } else if (tourtypeSelect.value === '__custom__' && tourtypeCustom.value.trim() === '') {
        showErr('tourtypeCustom', 'err-tourtypeCustom', 'กรุณาระบุชื่อประเภททัวร์');
        valid = false;
    } else if (tourtypeSelect.value === 'ทัวร์รายวัน') {
        if (parseInt(daysInput.value) !== 1 || parseInt(nightsInput.value) !== 0) {
            showErr('numberOfDays', 'err-numberOfDays', 'ทัวร์รายวันต้องเป็น 1 วัน 0 คืน');
            valid = false;
        }
    } else {
        // ทัวร์หลายวันทุกประเภท (รวม __custom__): แค่ต้องมากกว่า 1 วัน และคืน < วัน
        const d = parseInt(daysInput.value);
        const n = parseInt(nightsInput.value);
        if (isNaN(d) || d <= 1) {
            showErr('numberOfDays', 'err-numberOfDays', 'ทัวร์หลายวันต้องมากกว่า 1 วัน');
            valid = false;
        } else if (isNaN(n) || n >= d) {
            showErr('numberOfDays', 'err-numberOfDays', 'จำนวนคืนต้องน้อยกว่าจำนวนวัน');
            valid = false;
        }
    }

    if (!valid) {
        e.preventDefault();
        document.querySelector('.form-control.error')
            ?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    e.preventDefault();

    const formData = new FormData(this);
    const finalTourType = tourtypeSelect.value === '__custom__'
        ? tourtypeCustom.value.trim()
        : tourtypeSelect.value;
    formData.set('tourtype', finalTourType); // field จริงที่ backend/Entity ใช้

    fetch(this.action, {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
        .then(response => {
            if (!response.ok) {
                // HTTP error จริง (500, 404 ฯลฯ)
                return response.text().then(() => {
                    throw new Error('เกิดข้อผิดพลาดจากเซิร์ฟเวอร์ (status ' + response.status + ')');
                });
            }

            // ✅ ตรวจว่า redirect ไปหน้า success จริงหรือไม่
            // controller สำเร็จ → "redirect:/manager/tours/{id}/schedules?success=created" → response.redirected = true
            // controller validate ไม่ผ่าน → return "Tour/addTour" ตรงๆ (ไม่ redirect) → response.redirected = false
            if (!response.redirected || !response.url.includes('/manager/tours')) {
                return response.text().then(html => {
                    // ดึงข้อความจาก <div class="alert-error">...<span>ข้อความ</span></div>
                    const match = html.match(/class="alert-error"[\s\S]*?<span[^>]*>([^<]*)<\/span>/);
                    const msg = match ? match[1].trim() : 'กรุณาตรวจสอบข้อมูลที่กรอกอีกครั้ง';
                    throw new Error(msg);
                });
            }
            // ✅ ส่ง URL ปลายทางจริงที่ browser redirect ไปถึง (เช่น
            //    /manager/tours/{tourid}/schedules?success=created) ต่อไปยัง .then ถัดไป
            return response.url;
        })
        .then((redirectUrl) => {
            showSuccessModal(redirectUrl);
        })
        .catch(err => {
            console.error('เพิ่มทัวร์ไม่สำเร็จ:', err);
            showAlertModal('เกิดข้อผิดพลาด: ' + err.message, { type: 'error' });
        });
});