function openLightbox(src) {
            document.getElementById('lightboxImg').src = src;
            document.getElementById('lightbox').classList.add('open');
        }
        function closeLightbox() {
            document.getElementById('lightbox').classList.remove('open');
        }
        document.addEventListener('keydown', e => { if (e.key === 'Escape') closeLightbox(); });

        let currentTypeFilter = 'all';
        let currentStarFilter = 0;
        let currentPage = 1;
        const PAGE_SIZE = 6;

        function applyFilters() {
            const cards = Array.from(document.querySelectorAll('#reviewList .review-card'));
            const matched = cards.filter(card => {
                const t = card.dataset.type;
                const r = parseInt(card.dataset.rating);
                const typeMatch = currentTypeFilter === 'all' || t === currentTypeFilter;
                const starMatch = currentStarFilter === 0 || r === currentStarFilter;
                return typeMatch && starMatch;
            });
            renderPage(cards, matched);
        }

        function renderPage(allCards, matched) {
            const totalPages = Math.max(1, Math.ceil(matched.length / PAGE_SIZE));
            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            allCards.forEach(card => card.style.display = 'none');

            const noResults = document.getElementById('noFilterResults');
            const pagination = document.getElementById('paginationControls');

            if (matched.length === 0) {
                noResults.style.display = '';
                pagination.style.display = 'none';
                document.getElementById('pageInfo').textContent = 'หน้า 0 / 0';
                return;
            }
            noResults.style.display = 'none';

            const start = (currentPage - 1) * PAGE_SIZE;
            const end = start + PAGE_SIZE;
            matched.slice(start, end).forEach(card => card.style.display = '');

            pagination.style.display = totalPages > 1 ? 'flex' : 'none';
            document.getElementById('pageInfo').textContent = 'หน้า ' + currentPage + ' / ' + totalPages;
            document.getElementById('prevPageBtn').classList.toggle('disabled', currentPage <= 1);
            document.getElementById('nextPageBtn').classList.toggle('disabled', currentPage >= totalPages);
        }

        function goToPage(delta) {
            currentPage += delta;
            applyFilters();
        }

        function filterByType(type, chipEl) {
            currentTypeFilter = type;
            currentPage = 1;
            document.querySelectorAll('#typeFilterList .filter-chip').forEach(c => c.classList.remove('active'));
            if (chipEl) chipEl.classList.add('active');
            applyFilters();
        }

        function filterByStar(star, chipEl) {
            currentStarFilter = parseInt(star);
            currentPage = 1;
            document.querySelectorAll('#starFilterList .filter-chip').forEach(c => c.classList.remove('active'));
            if (chipEl) chipEl.classList.add('active');
            applyFilters();
        }

        document.addEventListener('DOMContentLoaded', applyFilters);