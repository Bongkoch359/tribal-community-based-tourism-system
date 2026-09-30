  function openLightbox(src) {
            document.getElementById('lightbox-img').src = src;
            document.getElementById('lightbox').classList.add('open');
        }
        function closeLightbox() {
            document.getElementById('lightbox').classList.remove('open');
        }
        function toggleUserMenu() {
            document.getElementById('userMenuWrapper').classList.toggle('open');
        }
        document.addEventListener('click', function (e) {
            const wrapper = document.getElementById('userMenuWrapper');
            if (wrapper && !wrapper.contains(e.target)) wrapper.classList.remove('open');
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') closeLightbox();
        });
        /* ════ Gallery Carousel ════ */
        let galleryIndex = 0;
        function galleryUpdate() {
            const track = document.getElementById('galleryTrack');
            if (!track) return;
            const total = track.children.length;
            if (galleryIndex < 0) galleryIndex = total - 1;
            if (galleryIndex >= total) galleryIndex = 0;
            track.style.transform = `translateX(-${galleryIndex * 100}%)`;

            const counter = document.getElementById('galleryCounter');
            if (counter) counter.textContent = galleryIndex + 1;

            document.querySelectorAll('#galleryDots .gallery-dot').forEach(function (dot, i) {
                dot.classList.toggle('active', i === galleryIndex);
            });
        }
        function galleryMove(dir) {
            galleryIndex += dir;
            galleryUpdate();
        }
        function galleryGoTo(idx) {
            galleryIndex = idx;
            galleryUpdate();
        }