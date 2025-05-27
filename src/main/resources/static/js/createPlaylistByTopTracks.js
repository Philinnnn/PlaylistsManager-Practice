document.getElementById("topTracksForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const name = document.getElementById("playlistName").value;

    const res = await fetch("/llama/generate-by-top-tracks");
    const tracks = await res.json();

    const body = {
    name,
    description: "Плейлист на основе ваших любимых треков",
    isPublic: true,
    trackRequests: tracks
};

    const response = await fetch("/create", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
});

    const result = document.getElementById("result");
    if (response.ok) {
    const id = await response.text();
    result.innerHTML = `<p>Плейлист создан!</p>
        <iframe src="https://open.spotify.com/embed/playlist/${id}" width="100%" height="360" frameborder="0" allowtransparency="true" allow="encrypted-media"></iframe>`;
} else {
    result.textContent = "Ошибка при создании плейлиста.";
}
});