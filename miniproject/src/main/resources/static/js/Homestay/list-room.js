
        function blockAddRoom(e) {
            e.preventDefault();
            alert('กรุณากรอกข้อมูลบัญชีธนาคารให้ครบก่อน จึงจะสามารถเพิ่มห้องพักได้');
            return false;
        }
        // mapping icon ตามสถานะ 
        const statusIcons = {
            'เปิดจอง': 'fa-circle-check',
            'เต็ม': 'fa-ban',
            'ปิดปรับปรุง': 'fa-screwdriver-wrench',
        };

        // mapping สี active ปุ่ม ตามสถานะ
        const statusActiveClass = {
            'เปิดจอง': 'active-available',
            'เต็ม': 'active-full',
            'ปิดปรับปรุง': 'active-maintenance',
        };

        function buildFilterButtons() {
            const statusList = Object.keys(statusIcons);
            const bar = document.getElementById('filterBar');
            bar.innerHTML = '';

            statusList.forEach((status, idx) => {
                const icon = statusIcons[status] || 'fa-circle';
                const btn = document.createElement('button');
                btn.className = 'filter-btn' + (idx === 0 ? ' active ' + statusActiveClass[status] : '');
                btn.innerHTML = `<i class="fas ${icon}"></i> ${status}`;
                btn.onclick = function () { filterRooms(status, this); };
                bar.appendChild(btn);
            });

            if (statusList.length > 0) {
                filterByStatus(statusList[0]);
            }
        }

        function filterRooms(status, btn) {
            document.querySelectorAll('.filter-btn').forEach(b => {
                b.classList.remove('active', 'active-available', 'active-full', 'active-maintenance');
            });
            btn.classList.add('active', statusActiveClass[status]);
            filterByStatus(status);
        }
        function filterByStatus(status) {
            const rows = document.querySelectorAll('#roomBody tr[data-status]');
            let visibleCount = 0;
            rows.forEach(row => {
                const rowStatus = row.getAttribute('data-status');
                const match = rowStatus === status;
                row.style.display = match ? '' : 'none';
                if (match) visibleCount++;
            });

            
            const oldNotice = document.getElementById('noStatusRow');
            if (oldNotice) oldNotice.remove();

            // ถ้าไม่มีห้องพักในสถานะที่เลือก ให้ขึ้นข้อความแจ้งเตือน
            if (visibleCount === 0) {
                const tbody = document.getElementById('roomBody');
                const tr = document.createElement('tr');
                tr.id = 'noStatusRow';
                tr.innerHTML = `
                    <td colspan="7">
                        <div class="empty-state">
                            <i class="fas fa-door-open"></i>
                            <p>ไม่มีห้องพักในสถานะ "${status}"</p>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            }
        }

        document.addEventListener('DOMContentLoaded', buildFilterButtons);