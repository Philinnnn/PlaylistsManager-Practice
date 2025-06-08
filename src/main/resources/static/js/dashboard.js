document.addEventListener("DOMContentLoaded", function () {
    const logoutForm = document.querySelector("form[method='post']");
    if (logoutForm) {
        logoutForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            await fetch("/auth/logout", { method: "POST" });
            window.location.href = "/login";
        });
    }

    async function render2faBlock() {
        const area = document.getElementById('twofa-status-area');
        area.innerHTML = 'Загрузка...';
        // Получаем статус 2FA
        const res = await fetch('/auth/2fa/status', { credentials: 'include' });
        if (res.ok) {
            const data = await res.json();
            if (data.enabled) {
                area.innerHTML = `<div style='margin:20px 0;'>
                    <span style='color:green;font-weight:bold;'>2FA включена</span><br>
                    <button id='disable2faBtn' style='margin-top:10px;'>Отключить 2FA</button>
                </div>`;
                document.getElementById('disable2faBtn').onclick = async function() {
                    if (confirm('Вы уверены, что хотите отключить двухфакторную аутентификацию?')) {
                        const resp = await fetch('/auth/2fa/disable', { method: 'POST', credentials: 'include' });
                        if (resp.ok) render2faBlock();
                    }
                };
            } else {
                area.innerHTML = `<button id='setup2faBtn'>🔒 Подключить Google Authenticator</button><div id='twofa-setup-area'></div>`;
                document.getElementById('setup2faBtn').onclick = async function() {
                    this.disabled = true;
                    const twofaArea = document.getElementById('twofa-setup-area');
                    twofaArea.innerHTML = 'Загрузка...';
                    const res = await fetch('/auth/2fa/setup', { method: 'POST', credentials: 'include' });
                    if (res.ok) {
                        const data = await res.json();
                        twofaArea.innerHTML = `<div style='margin:30px 0 20px 0;'><img src='${data.qrUrl}' alt='QR-код' style='display:block;margin:0 auto 20px auto;width:200px;height:200px;'></div>` +
                            `<form id='enable2faForm' style='margin-top:10px;display:flex;flex-direction:column;align-items:center;'>
                                <input type='text' id='code' placeholder='6-значный код' maxlength='6' required style='width:120px;text-align:center;margin-bottom:10px;'>
                                <button type='submit'>Включить 2FA</button>
                            </form>
                            <div id='twofa-result'></div>`;
                        document.getElementById('enable2faForm').addEventListener('submit', async function(e) {
                            e.preventDefault();
                            const code = document.getElementById('code').value;
                            const resp = await fetch('/auth/2fa/enable', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                credentials: 'include',
                                body: JSON.stringify({ code })
                            });
                            const resultDiv = document.getElementById('twofa-result');
                            if (resp.ok) {
                                resultDiv.textContent = '2FA успешно включена!';
                                resultDiv.className = 'success';
                                setTimeout(render2faBlock, 1000);
                            } else {
                                resultDiv.textContent = 'Неверный код!';
                                resultDiv.className = 'error';
                            }
                        });
                    } else {
                        twofaArea.innerHTML = 'Ошибка генерации QR-кода';
                    }
                };
            }
        } else {
            area.innerHTML = 'Ошибка получения статуса 2FA';
        }
    }

    render2faBlock();
});
