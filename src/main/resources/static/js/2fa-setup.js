window.addEventListener('DOMContentLoaded', async function() {
    const qrBlock = document.getElementById('qr-block');
    const result = document.getElementById('result');
    // Получаем QR-код и секрет
    const res = await fetch('/auth/2fa/setup', { method: 'POST', credentials: 'include' });
    if (res.ok) {
        const data = await res.json();
        qrBlock.innerHTML = `<img src='${data.qrUrl}' alt='QR-код'><p>Секрет: <b>${data.secret}</b></p>`;
    } else {
        qrBlock.innerHTML = 'Ошибка генерации QR-кода';
    }

    document.getElementById('enable2faForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const code = document.getElementById('code').value;
        const resp = await fetch('/auth/2fa/enable', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ code })
        });
        if (resp.ok) {
            result.textContent = '2FA успешно включена!';
            result.className = 'success';
        } else {
            result.textContent = 'Неверный код!';
            result.className = 'error';
        }
    });
});

