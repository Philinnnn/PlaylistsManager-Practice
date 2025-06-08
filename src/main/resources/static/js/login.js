document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const rememberMe = document.getElementById("rememberMe").checked;

    const res = await fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password, rememberMe }),
        credentials: "include"
    });

    const result = document.getElementById("result");
    const text = await res.text();
    result.textContent = text;

    if (res.ok) {
        if (text === '2fa_required') {
            localStorage.setItem('2fa_username', username);
            window.location.href = '/2fa-verify';
            return;
        }
        result.className = "success";
        setTimeout(() => {
            window.location.href = "/dashboard";
        }, 500);
    } else {
        result.className = "error";
    }
});
