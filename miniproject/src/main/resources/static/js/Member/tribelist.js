
function toggleUserMenu() {
    document.getElementById('userMenuWrapper').classList.toggle('open');
}
document.addEventListener('click', function(e) {
    const w = document.getElementById('userMenuWrapper');
    if (w && !w.contains(e.target)) w.classList.remove('open');
});

function filterByTribe(val) {
    document.querySelectorAll('.tribe-card').forEach(card => {
        const name = card.getAttribute('data-name') || '';
        card.style.display = (val === 'all' || name === val) ? '' : 'none';
    });
}


let heroIndex = 0;
const heroSlides = document.querySelectorAll('.hero-slide');
const heroDots = document.querySelectorAll('.hero-dot');
let heroTimer = null;

function heroShow(i) {
    heroSlides.forEach(s => s.classList.remove('active'));
    heroDots.forEach(d => d.classList.remove('active'));
    heroIndex = (i + heroSlides.length) % heroSlides.length;
    heroSlides[heroIndex].classList.add('active');
    heroDots[heroIndex].classList.add('active');
}
function heroSlide(dir) { heroShow(heroIndex + dir); resetHeroTimer(); }
function heroGoTo(i) { heroShow(i); resetHeroTimer(); }
function resetHeroTimer() {
    clearInterval(heroTimer);
    heroTimer = setInterval(() => heroShow(heroIndex + 1), 6000);
}
if (heroSlides.length > 1) resetHeroTimer();