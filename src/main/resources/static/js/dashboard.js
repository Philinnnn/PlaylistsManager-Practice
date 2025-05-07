document.addEventListener("DOMContentLoaded", function () {
    const logoutForm = document.querySelector("form[method='post']");
    if (logoutForm) {
        logoutForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            await fetch("/auth/logout", { method: "POST" });
            window.location.href = "/login";
        });
    }
});
