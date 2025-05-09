document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const res = await fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
        credentials: "include"
    });

    const result = document.getElementById("result");
    const text = await res.text();
    result.textContent = text;

    if (res.ok) {
        result.className = "success";
        setTimeout(() => {
            window.location.href = "/dashboard";
        }, );
    } else {
        result.className = "error";
    }
});
