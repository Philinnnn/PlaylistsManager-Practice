window.addEventListener('DOMContentLoaded', async function() {
    try {
        const res = await fetch('/auth/check', { credentials: 'include' });
        if (res.ok) {
            window.location.href = '/dashboard';
        }
    } catch (e) {
    }
});

