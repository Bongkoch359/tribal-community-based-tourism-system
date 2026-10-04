
const pricePerNight   = parseFloat(document.getElementById('pricePerNight').value) || 0;
const maxGuestPerRoom = parseInt(document.getElementById('maxGuestPerRoom').value) || 1;
let pendingForm = null;

function fmt(dateStr){
    if(!dateStr) return '—';
    const d = new Date(dateStr);
    const months = ['ม.ค.','ก.พ.','มี.ค.','เม.ย.','พ.ค.','มิ.ย.','ก.ค.','ส.ค.','ก.ย.','ต.ค.','พ.ย.','ธ.ค.'];
    return d.getDate()+' '+months[d.getMonth()]+' '+(d.getFullYear()+543);
}


function clampGuestCounts(sourceId){
    const roomsEl    = document.getElementById('numofrooms');
    const guestEl    = document.getElementById('guest');
    const childrenEl = document.getElementById('children');

    let rooms = parseInt(roomsEl.value);
    if (isNaN(rooms) || rooms < 1) rooms = 1;
    const roomsMaxAttr = roomsEl.max ? parseInt(roomsEl.max) : 99;
    rooms = Math.min(rooms, roomsMaxAttr);
    roomsEl.value = rooms;

    const capacity = maxGuestPerRoom * rooms;

    // 2) ผู้ใหญ่ / เด็ก — ห้ามต่ำกว่า min
    let adults   = parseInt(guestEl.value);
    let children = parseInt(childrenEl.value);
    if (isNaN(adults)   || adults   < 1) adults   = 1;
    if (isNaN(children) || children < 0) children = 0;

    // 3) ถ้ารวมกันเกินความจุ ให้ปรับ "อีกช่อง" ที่ไม่ใช่ช่องที่ผู้ใช้เพิ่งกด/พิมพ์ก่อน
    if (adults + children > capacity) {
        if (sourceId === 'guest') {
            children = Math.max(0, capacity - adults);
        } else if (sourceId === 'children') {
            adults = Math.max(1, capacity - children);
            // ถ้ายังเกิน (เช่น capacity < 1) ให้บีบ children ต่อ
            if (adults + children > capacity) children = Math.max(0, capacity - adults);
        } else {
            // เปลี่ยนจำนวนห้องแล้ว capacity เล็กลงจนทั้งคู่เกิน → บีบเด็กก่อน แล้วค่อยผู้ใหญ่
            if (adults > capacity) adults = Math.max(1, capacity);
            children = Math.max(0, capacity - adults);
        }
    }

    guestEl.value    = adults;
    childrenEl.value = children;

    const hint = document.getElementById('capacityHint');
    if (hint) {
        hint.textContent = `ℹ️ ห้องนี้รองรับสูงสุด ${maxGuestPerRoom} ท่าน/ห้อง × ${rooms} ห้อง = ${capacity} ท่าน (รวมผู้ใหญ่และเด็ก)`;
    }
}

function changeNum(id, delta){
    const el = document.getElementById(id);
    const min = parseInt(el.min)||0;
    const max = el.max ? parseInt(el.max) : 99;
    el.value = Math.min(max, Math.max(min, parseInt(el.value||0)+delta));
    clampGuestCounts(id);
    updateSummary();
}

function updateSummary(){
    const ci = document.getElementById('checkin').value;
    const co = document.getElementById('checkout').value;
    const rooms = parseInt(document.getElementById('numofrooms').value)||1;
    const adults = parseInt(document.getElementById('guest').value)||1;
    const children = parseInt(document.getElementById('children').value)||0;
    document.getElementById('sum-checkin').textContent = fmt(ci);
    document.getElementById('sum-checkout').textContent = fmt(co);
    document.getElementById('sum-rooms').textContent = rooms+' ห้อง';
    document.getElementById('sum-guests').textContent = (adults+children)+' ท่าน';
    let nights = 0;
    if(ci && co) nights = Math.max(0,(new Date(co)-new Date(ci))/86400000);
    document.getElementById('sum-nights').textContent = nights+' คืน';
    document.getElementById('sum-price').textContent = (pricePerNight*Math.max(0,nights)*rooms).toLocaleString();
}

function toggleGuestForm(val){
    const wrap = document.getElementById('guestFormWrap');
    wrap.style.display = (val==='false')?'block':'none';
    if(val==='true'){
        document.getElementById('guestFirstname').value='';
        document.getElementById('guestLastname').value='';
        // เคลียร์ error ของช่องชื่อผู้เข้าพักถ้ามีอยู่ก่อนหน้า
        clearFieldError('guestFirstname','err-guestFirstname');
        clearFieldError('guestLastname','err-guestLastname');
    }
}

function toggleUserMenu(){
    document.getElementById('userMenuWrapper').classList.toggle('open');
}

document.addEventListener('click', function(e){
    const w = document.getElementById('userMenuWrapper');
    if(w && !w.contains(e.target)) w.classList.remove('open');
});

window.onload = function(){
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('checkin').min = today;
    document.getElementById('checkout').min = today;
    clampGuestCounts('numofrooms'); // ตั้งค่าเริ่มต้น + แสดง hint ความจุ
    updateSummary();
}

/* ══════════════════════════════════════════
   VALIDATION — เลียนแบบ pattern เดียวกับหน้าทัวร์
   ช่องไหนพลาด: ขอบ/พื้นแดง + ข้อความใต้ช่อง + banner รวม
══════════════════════════════════════════ */
function clearErrors(){
    document.querySelectorAll('.field-error').forEach(el => el.textContent = '');
    document.querySelectorAll('.form-control').forEach(el => el.classList.remove('is-invalid','is-valid'));
    document.querySelectorAll('.num-input-wrap').forEach(el => el.classList.remove('is-invalid','is-valid'));
    document.getElementById('validationBanner').classList.remove('show');
    document.getElementById('validationList').innerHTML = '';
}

function clearFieldError(inputId, errId){
    const input = document.getElementById(inputId);
    const errEl = document.getElementById(errId);
    if (input) input.classList.remove('is-invalid');
    if (errEl) errEl.textContent = '';
}

function setFieldError(inputId, errId, message){
    const input = document.getElementById(inputId);
    const errEl = document.getElementById(errId);
    if (input) { input.classList.add('is-invalid'); input.classList.remove('is-valid'); }
    if (errEl) errEl.textContent = message;
}

function setFieldValid(inputId){
    const input = document.getElementById(inputId);
    if (input) { input.classList.remove('is-invalid'); input.classList.add('is-valid'); }
}

function setWrapError(wrapId, errId, message){
    const wrap = document.getElementById(wrapId);
    const errEl = document.getElementById(errId);
    if (wrap) { wrap.classList.add('is-invalid'); wrap.classList.remove('is-valid'); }
    if (errEl) errEl.textContent = message;
}

function setWrapValid(wrapId){
    const wrap = document.getElementById(wrapId);
    if (wrap) { wrap.classList.remove('is-invalid'); wrap.classList.add('is-valid'); }
}

function validateBookingForm(){
    clearErrors();
    const errors = [];

    const isBookerGoing = document.querySelector('input[name="isBookerGoing"]:checked').value;

    // 1. ชื่อ-นามสกุลผู้เข้าพัก (บังคับเฉพาะกรณีจองให้ผู้อื่น)
    if (isBookerGoing === 'false') {
        const fn = document.getElementById('guestFirstname').value.trim();
        const ln = document.getElementById('guestLastname').value.trim();
        if (!fn) {
            setFieldError('guestFirstname','err-guestFirstname','กรุณากรอกชื่อจริงผู้เข้าพัก');
            errors.push('กรุณากรอกชื่อจริงผู้เข้าพัก');
        } else setFieldValid('guestFirstname');

        if (!ln) {
            setFieldError('guestLastname','err-guestLastname','กรุณากรอกนามสกุลผู้เข้าพัก');
            errors.push('กรุณากรอกนามสกุลผู้เข้าพัก');
        } else setFieldValid('guestLastname');
    }

    // 2. วันที่เช็คอิน / เช็คเอาท์
    const ciVal = document.getElementById('checkin').value;
    const coVal = document.getElementById('checkout').value;
    const today = new Date(); today.setHours(0,0,0,0);

    if (!ciVal) {
        setFieldError('checkin','err-checkin','กรุณาเลือกวันเช็คอิน');
        errors.push('กรุณาเลือกวันเช็คอิน');
    } else if (new Date(ciVal + 'T00:00:00') < today) {
        setFieldError('checkin','err-checkin','ไม่สามารถเลือกวันย้อนหลังได้');
        errors.push('วันเช็คอินไม่สามารถย้อนหลังได้');
    } else {
        setFieldValid('checkin');
    }

    if (!coVal) {
        setFieldError('checkout','err-checkout','กรุณาเลือกวันเช็คเอาท์');
        errors.push('กรุณาเลือกวันเช็คเอาท์');
    } else if (ciVal && new Date(coVal + 'T00:00:00') <= new Date(ciVal + 'T00:00:00')) {
        setFieldError('checkout','err-checkout','วันเช็คเอาท์ต้องมากกว่าวันเช็คอิน');
        errors.push('วันเช็คเอาท์ต้องมากกว่าวันเช็คอิน');
    } else {
        setFieldValid('checkout');
    }

    // 3. จำนวนห้อง — ห้ามต่ำกว่า 1 / ห้ามเกิน max / ห้ามติดลบหรือไม่ใช่ตัวเลข
    const roomsEl = document.getElementById('numofrooms');
    const roomsVal = parseInt(roomsEl.value);
    const roomsMin = parseInt(roomsEl.min || '1');
    const roomsMax = roomsEl.max ? parseInt(roomsEl.max) : null;
    if (isNaN(roomsVal) || roomsVal < roomsMin) {
        setWrapError('wrap-numofrooms','err-numofrooms', `จำนวนห้องต้องอย่างน้อย ${roomsMin} ห้อง`);
        errors.push('จำนวนห้องไม่ถูกต้อง');
    } else if (roomsMax !== null && roomsVal > roomsMax) {
        setWrapError('wrap-numofrooms','err-numofrooms', `จำนวนห้องมีสูงสุด ${roomsMax} ห้อง`);
        errors.push('จำนวนห้องเกินจำนวนที่มี');
    } else {
        setWrapValid('wrap-numofrooms');
    }

    // 4. จำนวนผู้ใหญ่ — ห้ามต่ำกว่า 1 / ห้ามติดลบ / ไม่ใช่ตัวเลข
    const guestEl = document.getElementById('guest');
    const guestVal = parseInt(guestEl.value);
    const guestMin = parseInt(guestEl.min || '1');
    let guestOk = true;
    if (isNaN(guestVal) || guestVal < guestMin) {
        setWrapError('wrap-guest','err-guest', `จำนวนผู้ใหญ่ต้องอย่างน้อย ${guestMin} ท่าน`);
        errors.push('จำนวนผู้ใหญ่ไม่ถูกต้อง');
        guestOk = false;
    } else {
        setWrapValid('wrap-guest');
    }

    // 5. จำนวนเด็ก — ห้ามติดลบ / ไม่ใช่ตัวเลข
    const childrenEl = document.getElementById('children');
    const childrenVal = parseInt(childrenEl.value);
    let childrenOk = true;
    if (isNaN(childrenVal) || childrenVal < 0) {
        setWrapError('wrap-children','err-children','จำนวนเด็กต้องไม่ติดลบ');
        errors.push('จำนวนเด็กต้องไม่ติดลบ');
        childrenOk = false;
    } else {
        setWrapValid('wrap-children');
    }

    // 6. ผู้ใหญ่ + เด็ก รวมกันต้องไม่เกินความจุห้อง (maxGuestPerRoom × จำนวนห้อง)
    //    เช็คเฉพาะกรณีทั้งสองช่องเป็นตัวเลขที่ถูกต้องแล้วเท่านั้น (ไม่ซ้ำ error เดิม)
    if (guestOk && childrenOk) {
        const capacity = maxGuestPerRoom * (parseInt(roomsEl.value) || 1);
        const totalGuest = guestVal + childrenVal;
        if (totalGuest > capacity) {
            const msg = `ผู้เข้าพักรวม ${totalGuest} ท่าน เกินความจุห้อง (สูงสุด ${capacity} ท่าน)`;
            setWrapError('wrap-guest','err-guest', msg);
            setWrapError('wrap-children','err-children', msg);
            errors.push(msg);
        }
    }

    // 7. จำนวนผู้เข้าพักรวม ต้องไม่น้อยกว่าจำนวนห้อง (อย่างน้อย 1 คน/ห้อง)
if (guestOk && (guestVal + childrenVal) < roomsVal) {
    const msg = `จำนวนผู้เข้าพัก (${guestVal + childrenVal} ท่าน) น้อยกว่าจำนวนห้องที่เลือก (${roomsVal} ห้อง)`;
    setWrapError('wrap-numofrooms','err-numofrooms', msg);
    setWrapError('wrap-guest','err-guest', msg);
    errors.push(msg);
}

       if (errors.length > 0) {
        const list = document.getElementById('validationList');
        errors.forEach(e => {
            const li = document.createElement('li');
            li.textContent = e;
            list.appendChild(li);
        });
        const banner = document.getElementById('validationBanner');
        banner.classList.add('show');

       
        requestAnimationFrame(() => {
            banner.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }

    return errors.length === 0;
}

function openConfirmModal() {
    const form = document.getElementById('bookingForm');

    if (!validateBookingForm()) {
        return; 
    }

    pendingForm = form;
    document.getElementById('confirmModal').classList.add('show');
}

function closeConfirmModal() {
    document.getElementById('confirmModal').classList.remove('show');
    pendingForm = null;
}

function executeBooking() {
    if (!pendingForm) return;
    document.getElementById('confirmModal').classList.remove('show');
    submitBooking();
}

function submitBooking(){
    const form = document.getElementById('bookingForm');
    const formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        body: new URLSearchParams(formData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(() => {
                throw new Error('เกิดข้อผิดพลาดจากเซิร์ฟเวอร์ (status ' + response.status + ')');
            });
        }
        if (!response.redirected) {
            return response.text().then(html => {
                const match = html.match(/class="error-box"[\s\S]*?<span[^>]*>([^<]*)<\/span>/);
                const msg = match ? match[1].trim() : 'กรุณาตรวจสอบข้อมูลที่กรอกอีกครั้ง';
                throw new Error(msg);
            });
        }
        return response.url;
    })
    .then(redirectUrl => {
        showSuccessModal(redirectUrl);
    })
    .catch(err => {
        console.error('จองโฮมสเตย์ไม่สำเร็จ:', err);
        alert('เกิดข้อผิดพลาด: ' + err.message);
    });
}

function showSuccessModal(redirectUrl){
    const modal = document.getElementById('successModal');
    const fill = document.getElementById('progressFill');
    modal.classList.add('show');
    fill.style.animation = 'none';
    fill.offsetHeight;
    fill.style.animation = 'progress-drain 2.5s linear forwards';
    setTimeout(() => {
        window.location.href = redirectUrl;
    }, 2500);
}