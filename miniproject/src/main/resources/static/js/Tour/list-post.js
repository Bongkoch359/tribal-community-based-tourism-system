 // ─── Filter tabs (ตามทัวร์) ───
        function toggleTourColumn(type) {
            // ซ่อนคอลัมน์ "เชื่อมโยงกับทัวร์" ทั้งคอลัมน์เมื่ออยู่แท็บข่าวสารทั่วไป
            const show = type !== 'NEWS';
            const header = document.getElementById('tourColHeader');
            if (header) header.style.display = show ? '' : 'none';
            document.querySelectorAll('.tour-col').forEach(td => {
                td.style.display = show ? '' : 'none';
            });
        }

        function filterPosts(type, btn) {
            // active tab
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // show/hide rows
            document.querySelectorAll('.post-row').forEach(row => {
                const hasTour = row.getAttribute('data-hastour') === 'true';
                let show = true;
                if (type === 'TOUR') show = hasTour;
                else if (type === 'NEWS') show = !hasTour;
                row.style.display = show ? '' : 'none';
            });

            toggleTourColumn(type);
        }

        // แสดงผลตาม tab เริ่มต้น ("มีทัวร์") ตอนโหลดหน้า
        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('.post-row').forEach(row => {
                const hasTour = row.getAttribute('data-hastour') === 'true';
                row.style.display = hasTour ? '' : 'none';
            });
            toggleTourColumn('TOUR');
        });

        // ─── Delete confirm modal ───
        let formToSubmit = null;

        function confirmDelete(btn) {
            formToSubmit = btn.closest('.delete-form');
            document.getElementById('deleteModal').classList.add('show');
        }

        function closeDeleteModal() {
            document.getElementById('deleteModal').classList.remove('show');
            formToSubmit = null;
        }

        document.getElementById('modalConfirmBtn').addEventListener('click', function () {
            if (formToSubmit) formToSubmit.submit();
        });

        // ปิด modal เมื่อคลิกนอกกล่อง
        document.getElementById('deleteModal').addEventListener('click', function (e) {
            if (e.target === this) closeDeleteModal();
        });
        // ─── นับจำนวนโพสต์แต่ละประเภท แล้วเติมลง badge ───
        function computePostCounts() {
            let tourCount = 0;
            let newsCount = 0;
            document.querySelectorAll('.post-row').forEach(row => {
                const hasTour = row.getAttribute('data-hastour') === 'true';
                if (hasTour) tourCount++;
                else newsCount++;
            });
            const tourBadge = document.getElementById('count-tour');
            const newsBadge = document.getElementById('count-news');
            if (tourBadge) tourBadge.textContent = tourCount;
            if (newsBadge) newsBadge.textContent = newsCount;
        }

        // แสดงผลตาม tab เริ่มต้น ("มีทัวร์") ตอนโหลดหน้า
        document.addEventListener('DOMContentLoaded', function () {
            computePostCounts();
            document.querySelectorAll('.post-row').forEach(row => {
                const hasTour = row.getAttribute('data-hastour') === 'true';
                row.style.display = hasTour ? '' : 'none';
            });
            toggleTourColumn('TOUR');
        });