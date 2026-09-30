
window.addEventListener('load', function () {
    const params = new URLSearchParams(window.location.search);
    if (params.get('print') === 'true') {
        setTimeout(function () { window.print(); }, 300);
    }

    const el = document.getElementById('bahtText');
    if (el) {
        const raw = parseFloat(el.getAttribute('data-amount'));
        el.textContent = isNaN(raw) ? '-' : bahtText(raw);
    }
});

function bahtText(number) {
    const txtNum = ['ศูนย์','หนึ่ง','สอง','สาม','สี่','ห้า','หก','เจ็ด','แปด','เก้า'];
    const txtDigit = ['','สิบ','ร้อย','พัน','หมื่น','แสน','ล้าน'];
    number = Math.round(number * 100) / 100;
    const parts = number.toFixed(2).split('.');
    let baht = parts[0];
    const satang = parseInt(parts[1], 10);

    function readNumber(numStr) {
        let result = '';
        const len = numStr.length;
        for (let i = 0; i < len; i++) {
            const digit = parseInt(numStr[i], 10);
            const pos = len - i - 1;
            if (digit === 0) continue;
            if (pos === 0 && digit === 1 && len > 1) { result += 'เอ็ด'; continue; }
            if (pos === 1 && digit === 2) { result += 'ยี่' + txtDigit[1]; continue; }
            if (pos === 1 && digit === 1) { result += txtDigit[1]; continue; }
            result += txtNum[digit] + txtDigit[pos];
        }
        return result;
    }

    let bahtWords = '';
    let remaining = baht.replace(/^0+(?=\d)/, '');
    const millionChunks = [];
    while (remaining.length > 6) {
        millionChunks.unshift(remaining.slice(-6));
        remaining = remaining.slice(0, -6);
    }
    millionChunks.unshift(remaining);
    for (let i = 0; i < millionChunks.length; i++) {
        bahtWords += readNumber(millionChunks[i]);
        if (i < millionChunks.length - 1) bahtWords += 'ล้าน';
    }
    if (bahtWords === '') bahtWords = 'ศูนย์';

    let result = bahtWords + 'บาท';
    result += satang === 0 ? 'ถ้วน' : readNumber(String(satang)) + 'สตางค์';
    return result;
}
function toggleUserMenu() {
    document.getElementById('userMenuWrapper').classList.toggle('open');
}
document.addEventListener('click', function(e) {
    const w = document.getElementById('userMenuWrapper');
    if (w && !w.contains(e.target)) w.classList.remove('open');
});