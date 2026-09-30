
function toggleUserMenu() {
    document.getElementById('userMenuWrapper').classList.toggle('open');
}
document.addEventListener('click', function(e) {
    const w = document.getElementById('userMenuWrapper');
    if (w && !w.contains(e.target)) w.classList.remove('open');
});

// highlight the active section in the sub-nav while scrolling
(function() {
    const links = Array.from(document.querySelectorAll('.subnav-link'));
    const sections = links
        .map(l => document.getElementById(l.dataset.target))
        .filter(Boolean);
    if (!sections.length) return;
    const obs = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            const link = document.querySelector('.subnav-link[data-target="' + entry.target.id + '"]');
            if (!link) return;
            if (entry.isIntersecting) {
                links.forEach(l => l.classList.remove('is-active'));
                link.classList.add('is-active');
            }
        });
    }, { rootMargin: '-40% 0px -50% 0px', threshold: 0 });
    sections.forEach(s => obs.observe(s));
})();