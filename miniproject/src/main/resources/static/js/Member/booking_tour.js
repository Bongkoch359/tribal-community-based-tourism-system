document.addEventListener('alpine:init', () => {
        Alpine.data('bookingForm', () => ({
            adultPrice: ADULT_PRICE,
            childPrice: CHILD_PRICE,
            insurancePrice: INSURANCE_PRICE,
            availableSeats: AVAILABLE_SEATS,
            seatLevel: SEAT_LEVEL_INITIAL, // ★ เพิ่ม — ตอนนี้ badge ที่นั่งอัปเดตได้ตาม onScheduleChange()
            numOfAdult: 1,
            numOfChild: 0,
            isBookingForOther: false,
            wantInsurance: true,
            submitted: false,
            submitting: false,
            fieldErrors: {},
            pickupType: ALLOW_MEETING_POINT ? 'จุดรับส่วนกลาง' : (ALLOW_HOTEL_PICKUP ? 'โรงแรม/ที่พัก' : ''),  
            pickupLocation: '',

            get numOfGuest() { return this.numOfAdult + this.numOfChild; },
            get hasInsuranceOption() { return this.insurancePrice !== null && this.insurancePrice !== undefined; },
            get insuranceSubtotal() { return this.wantInsurance ? (this.numOfGuest * this.insurancePrice) : 0; },
            get subtotal() { return (this.numOfAdult * this.adultPrice) + (this.numOfChild * this.childPrice) + this.insuranceSubtotal; },
            get isSoldOut() { return this.availableSeats <= 0; },
            get canIncreaseGuest() { return this.numOfGuest < this.availableSeats; },
            get pickupAreaMismatch() {
                if (this.pickupType !== 'โรงแรม/ที่พัก') return false;
                if (!this.pickupLocation || !this.pickupLocation.trim()) return false;
                if (!HOTEL_PICKUP_AREA) return false;
                return !this.pickupLocation.includes(HOTEL_PICKUP_AREA);
            },
            get guestListForOther() {
                return Array.from({ length: this.numOfGuest }, (_, i) => i + 1);
            },
            get guestListSelf() {
                return Array.from({ length: this.numOfGuest - 1 }, (_, i) => i + 2);
            },
            get errorList() {
                return Object.values(this.fieldErrors);
            },

            incrementAdult() {
                if (this.canIncreaseGuest) this.numOfAdult++;
            },
            incrementChild() {
                if (this.canIncreaseGuest) this.numOfChild++;
            },

            // ★ ใหม่: เรียกทุกครั้งที่ user เปลี่ยนรอบวันที่ในดรอปดาวน์
            //   ดึงที่นั่งคงเหลือของ "รอบนั้นจริงๆ" (schedule) มา sync หน้าจอ
            //   กันตัวเลขที่โชว์ไม่ตรงกับที่ backend จะเช็คตอน submit จริง
            async onScheduleChange(event) {
                const option = event.target.selectedOptions[0];
                if (!option) return;
                const scheduleId = option.dataset.scheduleid;
                if (!scheduleId) return;

                try {
                    const res = await fetch(
                        `/booking/tour/${TOUR_ID}/seats?scheduleid=${encodeURIComponent(scheduleId)}`
                    );
                    if (!res.ok) throw new Error('โหลดข้อมูลที่นั่งไม่สำเร็จ');
                    const data = await res.json();

                    this.availableSeats = data.availableSeats;
                    this.seatLevel = data.seatLevel;

                    // ถ้าจำนวนคนที่กรอกไว้เกินที่นั่งของรอบใหม่ — ปรับลดอัตโนมัติ กันสับสน
                    if (this.numOfGuest > this.availableSeats) {
                        this.numOfAdult = Math.min(this.numOfAdult, Math.max(1, this.availableSeats));
                        this.numOfChild = Math.max(0, this.availableSeats - this.numOfAdult);
                    }
                } catch (err) {
                    console.error('โหลดที่นั่งไม่สำเร็จ:', err);
                }
            },

           validate() {
    const errors = {};

    // 1. วันที่เดินทาง
    const dateInputEl = document.getElementById('tourdate-input');
    const dateVal = dateInputEl.value;
    if (!dateVal) {
        errors['tourdate'] = 'กรุณาเลือกวันที่เดินทาง';
    } else {
        const chosen = new Date(dateVal + 'T00:00:00');
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (chosen < today) {
            errors['tourdate'] = 'ไม่สามารถเลือกวันย้อนหลังได้';
        }
    }

    // 1.5 ★ ใหม่: เช็คว่า option ที่เลือกมี scheduleid จริง (กันข้อมูล schedule เพี้ยน)
    const selectedOption = dateInputEl.selectedOptions[0];
    if (dateVal && (!selectedOption || !selectedOption.dataset.scheduleid)) {
        errors['tourdate'] = 'ข้อมูลรอบทัวร์ไม่ถูกต้อง กรุณาเลือกรอบวันที่ใหม่อีกครั้ง';
    }

    // 2. จำนวนผู้โดยสาร
    if (this.numOfAdult < 1) {
        // ★ ใหม่: กันเคสค่าถูกแก้ผ่าน devtools ให้ต่ำกว่า 1
        errors['numOfGuest'] = 'ต้องมีผู้ใหญ่อย่างน้อย 1 ท่าน';
    } else if (this.numOfGuest < 1) {
        errors['numOfGuest'] = 'กรุณาระบุจำนวนผู้เดินทางอย่างน้อย 1 ท่าน';
    } else if (this.numOfGuest > this.availableSeats) {
        errors['numOfGuest'] = `ที่นั่งเหลือเพียง ${this.availableSeats} ที่ กรุณาลดจำนวนผู้เดินทาง`;
    }

    // 2.4 ประเภทจุดรับ — ต้องมีค่าเสมอ
    if (!this.pickupType) {
        errors['pickuptype'] = 'ไม่พบช่องทางรับส่งสำหรับทัวร์นี้ กรุณาติดต่อเจ้าหน้าที่';
    }

    // 2.5 ที่อยู่โรงแรม (กรณีเลือกให้ไปรับ)
    if (this.pickupType === 'โรงแรม/ที่พัก') {
        if (!this.pickupLocation || !this.pickupLocation.trim()) {
            errors['pickuplocation'] = 'กรุณาระบุชื่อ/ที่อยู่โรงแรมที่พัก';
        }
    }

    // 3. ชื่อผู้เดินทาง
    const guestFirstInputs = document.querySelectorAll('input[name="guestFirstname"]');
    const guestLastInputs  = document.querySelectorAll('input[name="guestLastname"]');
    let guestError = false;

    // 3.5 ★ ใหม่: เช็คว่าจำนวน input ที่ render ออกมาจริง ตรงกับจำนวนที่ควรจะเป็น
    //     (กัน Alpine x-for ยัง sync ไม่ทันตอนกด submit เร็วเกินไป)
    const expectedGuestFormCount = this.isBookingForOther ? this.numOfGuest : (this.numOfGuest - 1);
    if (guestFirstInputs.length !== expectedGuestFormCount) {
        errors['guests'] = 'ระบบกำลังอัปเดตแบบฟอร์ม กรุณารอสักครู่แล้วกดยืนยันอีกครั้ง';
    }

    guestFirstInputs.forEach(inp => {
        if (!inp.value.trim()) {
            inp.classList.add('is-invalid');
            inp.classList.remove('is-valid');
            guestError = true;
        } else {
            inp.classList.remove('is-invalid');
            inp.classList.add('is-valid');
        }
    });
    guestLastInputs.forEach(inp => {
        if (!inp.value.trim()) {
            inp.classList.add('is-invalid');
            inp.classList.remove('is-valid');
            guestError = true;
        } else {
            inp.classList.remove('is-invalid');
            inp.classList.add('is-valid');
        }
    });

    if (guestError && !errors['guests']) {
        errors['guests'] = 'กรุณากรอกชื่อ-นามสกุลผู้เดินทางให้ครบถ้วน';
    }

    // 4. เลขบัตรประชาชน (ประกันภาคบังคับ ต้องกรอกทุกครั้ง)
    if (this.hasInsuranceOption && this.wantInsurance) {
        const idcardInputs = document.querySelectorAll('input[name="guestIdcard"]');
        let idcardFormatError = false;
        const allValues = [];

        // รอบแรก: เช็ค format (ต้องเป็นตัวเลข 13 หลักเท่านั้น)
        idcardInputs.forEach(inp => {
            const val = inp.value.trim();
            allValues.push(val);
            if (!/^[0-9]{13}$/.test(val)) {
                inp.classList.add('is-invalid');
                inp.classList.remove('is-valid');
                idcardFormatError = true;
            }
        });

        // รอบสอง: ★ ใหม่ — เช็คเลขบัตรซ้ำกันระหว่างผู้เดินทางในการจองเดียวกัน
        const seenIdcards = new Map(); // idcard -> index แรกที่เจอ
        const dupIndexes = new Set();
        allValues.forEach((val, idx) => {
            if (!/^[0-9]{13}$/.test(val)) return; // ข้ามตัวที่ format ผิดอยู่แล้ว ไม่ต้องเช็คซ้ำซ้อน
            if (seenIdcards.has(val)) {
                dupIndexes.add(idx);
                dupIndexes.add(seenIdcards.get(val));
            } else {
                seenIdcards.set(val, idx);
            }
        });

        idcardInputs.forEach((inp, idx) => {
            if (dupIndexes.has(idx)) {
                inp.classList.add('is-invalid');
                inp.classList.remove('is-valid');
            } else if (/^[0-9]{13}$/.test(allValues[idx])) {
                inp.classList.remove('is-invalid');
                inp.classList.add('is-valid');
            }
        });

        if (dupIndexes.size > 0) {
            errors['idcards'] = 'พบเลขบัตรประชาชนซ้ำกัน กรุณาตรวจสอบ (แต่ละท่านต้องใช้เลขบัตรของตนเอง)';
        } else if (idcardFormatError) {
            errors['idcards'] = 'กรุณากรอกเลขบัตรประชาชน 13 หลักให้ครบทุกท่านเพื่อทำประกัน';
        }
    }

    return errors;
},

            showSuccessModal(redirectUrl) {
                const modal = document.getElementById('successModal');
                const fill = document.getElementById('progressFill');
                modal.classList.add('show');
                fill.style.animation = 'none';
                fill.offsetHeight;
                fill.style.animation = 'progress-drain 2.5s linear forwards';
                setTimeout(() => {
                    window.location.href = redirectUrl;
                }, 2500);
            },

            // ── ขั้นที่ 1: ตรวจสอบความถูกต้อง แล้วเปิด "modal ยืนยันก่อนจอง" (ยังไม่ส่งข้อมูลจริง) ──
            handleSubmit() {
                if (this.submitting || this.isSoldOut) return;

                this.submitted = true;
                this.fieldErrors = this.validate();

                const dateInp = document.getElementById('tourdate-input');
                if (this.fieldErrors['tourdate']) {
                    dateInp.classList.add('is-invalid');
                    dateInp.classList.remove('is-valid');
                } else {
                    dateInp.classList.remove('is-invalid');
                    dateInp.classList.add('is-valid');
                }

              if (Object.keys(this.fieldErrors).length > 0) {
    this.$nextTick(() => {
        const banner = document.getElementById('validationBanner');
        if (!banner) return;

      
        const navbar = document.querySelector('.navbar');
        const navbarHeight = navbar ? navbar.offsetHeight : 0;
        const extraGap = 16; 

        const bannerTop = banner.getBoundingClientRect().top + window.scrollY;
        const targetY = bannerTop - navbarHeight - extraGap;

        window.scrollTo({
            top: Math.max(targetY, 0),
            behavior: 'smooth'
        });
    });
    return;
}
                

                // ข้อมูลครบถ้วน — เปิด modal ให้ผู้ใช้ยืนยันก่อนส่งจริง
                document.getElementById('confirmModal').classList.add('show');
            },

            // ── ขั้นที่ 2: ผู้ใช้กดยืนยันใน modal แล้วค่อยส่งข้อมูลจริง ──
            confirmAndSubmit() {
                document.getElementById('confirmModal').classList.remove('show');
                this.submitting = true;

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
                    this.showSuccessModal(redirectUrl);
                })
                .catch(err => {
                    console.error('จองทัวร์ไม่สำเร็จ:', err);
                    this.submitting = false;
                    alert('เกิดข้อผิดพลาด: ' + err.message);
                });
            },

            closeConfirmModal() {
                document.getElementById('confirmModal').classList.remove('show');
            }
        }));
    });
    
    function toggleUserMenu() {
  document.getElementById('userMenuWrapper').classList.toggle('open');
}
document.addEventListener('click', function(e) {
  const w = document.getElementById('userMenuWrapper');
  if (w && !w.contains(e.target)) w.classList.remove('open');
});

document.addEventListener('input', function (e) {
  if (!e.target.matches('input[name="guestIdcard"]')) return;

  var cleaned = e.target.value.replace(/[^0-9]/g, '');
  if (cleaned !== e.target.value) {
    e.target.value = cleaned;
  }

  if (cleaned.length === 0) {
    e.target.classList.remove('is-invalid', 'is-valid');
  } else if (cleaned.length === 13) {
    e.target.classList.remove('is-invalid');
    e.target.classList.add('is-valid');
  } else {
    e.target.classList.add('is-invalid');
    e.target.classList.remove('is-valid');
  }
});

document.addEventListener('paste', function (e) {
  if (!e.target.matches('input[name="guestIdcard"]')) return;
  setTimeout(function () {
    e.target.dispatchEvent(new Event('input'));
  }, 0);
});

window.addEventListener('DOMContentLoaded', function() {
  const dateInput = document.getElementById('tourdate-input');
  if (dateInput && dateInput.value) {
    const parts = dateInput.value.split('-');
    document.getElementById('sidebar-date').textContent =
      parts[2] + '/' + parts[1] + '/' + (parseInt(parts[0]) + 543);
  }
  if (dateInput) {
    dateInput.addEventListener('change', function() {
      const val = this.value;
      if (val) {
        const parts = val.split('-');
        document.getElementById('sidebar-date').textContent =
          parts[2] + '/' + parts[1] + '/' + (parseInt(parts[0]) + 543);
      }
    });
  }
});