  /* ═══════════════════════════════════════════
           โหลดรูป thumbnail จาก data-b64 ผ่าน JS
           หลีกเลี่ยง HTML attribute ยาวเกิน limit
        ═══════════════════════════════════════════ */
        document.querySelectorAll('.lazy-thumb').forEach(function (img) {
            const raw = img.getAttribute('data-src') || '';
            if (!raw) return;

            // ใช้รูปแรกจาก filename1.jpg||filename2.jpg
            const firstName = raw.split('||')[0].trim();
            img.src = '/uploads/tours/' + firstName;

            img.removeAttribute('data-src');
        });

        /* ═══════════════════════════════════════════
    นับจำนวนทัวร์แต่ละสถานะ แล้วเติมลง badge
 ═══════════════════════════════════════════ */
        function computeStatusCounts() {
            const rows = document.querySelectorAll('.tour-row');
            const counts = {};
            rows.forEach(row => {
                const s = row.getAttribute('data-status') || '';
                counts[s] = (counts[s] || 0) + 1;
            });

            const badgeMap = {
                'เปิดรับจอง': 'count-open',
                'เต็ม': 'count-full',
                'ปิด': 'count-closed',
            };

            Object.keys(badgeMap).forEach(status => {
                const el = document.getElementById(badgeMap[status]);
                if (el) el.textContent = counts[status] || 0;
            });
        }

        /* ═══════════════════════════════════════════
           FILTER TABS — กรองตามสถานะ
        ═══════════════════════════════════════════ */
        function filterTours(status, btn) {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const rows = document.querySelectorAll('.tour-row');
            let visible = 0;

            rows.forEach(row => {
                const rowStatus = row.getAttribute('data-status') || '';
                if (rowStatus === status) {
                    row.style.display = '';
                    visible++;
                } else {
                    row.style.display = 'none';
                }
            });

            const emptyState = document.getElementById('emptyState');
            if (emptyState) {
                emptyState.style.display = visible === 0 ? 'block' : 'none';
            }
        }

        // แสดงผลตาม tab เริ่มต้น ("เปิดรับจอง") ตอนโหลดหน้า + นับจำนวนแต่ละสถานะ
        document.addEventListener('DOMContentLoaded', function () {
            computeStatusCounts();
            filterTours('เปิดรับจอง', document.getElementById('tab-open'));
        });
        // แสดงผลตาม tab เริ่มต้น ("เปิดรับจอง") ตอนโหลดหน้า
        document.addEventListener('DOMContentLoaded', function () {
            filterTours('เปิดรับจอง', document.getElementById('tab-open'));
        });

        /* ═══════════════════════════════════════════
           AUTO-HIDE SUCCESS BANNER หลัง 3 วินาที
        ═══════════════════════════════════════════ */
        (function () {
            const banners = document.querySelectorAll('.alert-success');
            if (banners.length === 0) return;
            banners.forEach(function (el) {
                el.style.transition = 'opacity 0.5s ease, max-height 0.5s ease, margin 0.5s ease, padding 0.5s ease';
                setTimeout(function () {
                    el.style.opacity = '0';
                    el.style.maxHeight = '0';
                    el.style.marginBottom = '0';
                    el.style.padding = '0';
                    el.style.overflow = 'hidden';
                }, 3000);
            });
        })();