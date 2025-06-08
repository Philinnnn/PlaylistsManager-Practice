window.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('verify2faForm');
    const result = document.getElementById('result');
    const username = localStorage.getItem('2fa_username');
    if (!username) {
        window.location.href = '/login';
        return;
    }
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const code = document.getElementById('code').value;
        const res = await fetch('/auth/2fa/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, code })
        });
        if (res.ok) {
            localStorage.removeItem('2fa_username');
            window.location.href = '/dashboard';
        } else {
            result.textContent = 'Неверный код!';
            result.className = 'error';
        }
    });
});
