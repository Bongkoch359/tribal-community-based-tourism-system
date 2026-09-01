function toggleCustomRange() {
    const form = document.getElementById('customRangeForm');
    form.style.display = form.style.display === 'none' ? 'flex' : 'none';
}
function switchTrendTab(target) {
    document.getElementById('trendPanel-revenue').style.display = target === 'revenue' ? 'flex' : 'none';
    document.getElementById('trendPanel-bookingCount').style.display = target === 'bookingCount' ? 'flex' : 'none';

    document.querySelectorAll('.trend-tab').forEach(btn => {
        btn.classList.toggle('trend-tab-active', btn.dataset.target === target);
    });
}