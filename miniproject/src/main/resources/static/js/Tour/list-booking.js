  const bookingStatusClassMap = {
            'WAITING_APPROVAL': 'status-waiting',
            'CONFIRMED': 'status-confirmed',
            'COMPLETED': 'status-completed',
            'CANCEL': 'status-cancel'
        };

        // สลับเปิด/ปิด Dropdown (ใส่ stopPropagation เพื่อไม่ให้ window click ปิดทันที)
        function toggleBookingDropdown(e) {
            if (e) e.stopPropagation();
            const dropdown = document.getElementById('bookingStatusDropdown');
            dropdown.classList.toggle('open');
        }

        // ปิดเมื่อคลิกนอกพื้นที่
        window.addEventListener('click', function (e) {
            const dropdown = document.getElementById('bookingStatusDropdown');
            if (dropdown && !dropdown.contains(e.target)) {
                dropdown.classList.remove('open');
            }
        });

        // ฟังก์ชันเลือกตัวกรอง
        function selectBookingFilter(statusKey, itemEl) {
            const triggerBtn = document.getElementById('bookingDropdownTriggerBtn');
            const selectedTextContainer = triggerBtn.querySelector('.selected-text');

            const iconClass = itemEl.querySelector('i').className;
            const label = itemEl.querySelector('.label-text').textContent;
            const badgeCount = itemEl.querySelector('.count-badge').textContent;

            selectedTextContainer.innerHTML = `
                <i class="${iconClass}"></i>
                <span>${label}</span>
                <span class="count-badge">${badgeCount}</span>
            `;

            triggerBtn.className = 'filter-dropdown-trigger ' + (bookingStatusClassMap[statusKey] || 'status-waiting');

            document.querySelectorAll('#bookingDropdownMenu .dropdown-item').forEach(el => el.classList.remove('active'));
            itemEl.classList.add('active');

            document.getElementById('bookingStatusDropdown').classList.remove('open');

            filterBookings(statusKey);
        }

        // ฟังก์ชันซ่อน/แสดงแถวตาราง
        function filterBookings(statusKey) {
            const rows = document.querySelectorAll('#tourBookingsTbody tr[data-status]');
            const noResults = document.getElementById('noFilterResults');
            let visibleCount = 0;

            rows.forEach(r => {
                const match = (r.getAttribute('data-status') === statusKey);
                r.style.display = match ? '' : 'none';
                if (match) visibleCount++;
            });

            if (noResults) {
                // ซ่อนข้อความถ้ามีแถวแสดงอยู่ หรือถ้าไม่มีข้อมูลในระบบตั้งแต่แรก (ให้ tr.empty-row ทำงานแทน)
                noResults.style.display = (visibleCount === 0 && rows.length > 0) ? 'block' : 'none';
            }
        }

        // กำหนดสถานะเริ่มต้นเมื่อโหลดหน้า
        document.addEventListener('DOMContentLoaded', () => {
            const defaultItem = document.querySelector('#bookingDropdownMenu .dropdown-item[data-status="WAITING_APPROVAL"]');
            if (defaultItem) {
                selectBookingFilter('WAITING_APPROVAL', defaultItem);
            } else {
                filterBookings('WAITING_APPROVAL');
            }
        });