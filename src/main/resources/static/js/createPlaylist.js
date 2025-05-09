document.getElementById("createPlaylistForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const name = document.getElementById("playlistName").value;
    const genre = document.getElementById("genre").value;
    const region = document.getElementById("region").value;
    const mood = document.getElementById("mood").value;

    const resultDiv = document.getElementById("result");
    resultDiv.innerText = "Генерация...";
    resultDiv.className = "";

    try {
        // Получаем рекомендации с параметрами
        const query = new URLSearchParams({ genre, region, mood }).toString();
        const recommendationsRes = await fetch(`/lastfm/lastfm?${query}`);
        const trackRequests = await recommendationsRes.json();

        const body = {
            name,
            description: `Плейлист для настроения: ${mood || "любой"}`,
            isPublic: true,
            trackRequests
        };

        const response = await fetch("/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });

        const text = await response.text();
        resultDiv.innerText = text;
        resultDiv.className = response.ok ? "success" : "error";
    } catch (err) {
        resultDiv.innerText = "Ошибка: " + err;
        resultDiv.className = "error";
    }
});
