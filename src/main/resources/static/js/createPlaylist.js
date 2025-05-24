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
        const query = new URLSearchParams({ genre, region, mood }).toString();
        const recommendationsRes = await fetch(`/llama/generate-recommendations?${query}`);
        const trackRequests = await recommendationsRes.json();

        console.log("Запрос: " + query);
        console.log("Рекомендации от Last.fm:", recommendationsRes);
        console.log("Запрос в Spotify:", trackRequests);

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

        const playlistId = await response.text();
        resultDiv.className = response.ok ? "success" : "error";

        if (response.ok) {
            resultDiv.innerHTML = `
        <p>Плейлист успешно создан!</p>
        <iframe
            title="Spotify Embed: Recommendation Playlist"
            src="https://open.spotify.com/embed/playlist/${playlistId}?utm_source=generator&theme=0"
            width="100%"
            height="360"
            frameBorder="0"
            allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
            loading="lazy"
        ></iframe>
    `;
        } else {
            resultDiv.innerText = playlistId; // здесь текст ошибки
        }

    } catch (err) {
        resultDiv.innerText = "Ошибка: " + err;
        resultDiv.className = "error";
    }
});
