document.getElementById("registerForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const res = await fetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    });

    const text = await res.text();
    const statusElement = document.getElementById("registerStatus");
    statusElement.textContent = text;

    if (res.ok) {
        statusElement.style.color = "green";
        setTimeout(() => {
            window.location.href = "/login";
        }, );
    } else {
        statusElement.style.color = "red";
    }
});
