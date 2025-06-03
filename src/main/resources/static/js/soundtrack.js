document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("soundtrackForm");
    const trackInput = document.getElementById("trackName");
    const artistInput = document.getElementById("artistName");
    const resultContainer = document.getElementById("resultContainer");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const track = trackInput.value.trim();
        const artist = artistInput.value.trim();

        if (!track || !artist) {
            alert("Введите название трека и исполнителя.");
            return;
        }

        resultContainer.innerHTML = '<button disabled>⏳ Поиск...</button>';

        try {
            const res = await fetch(`/soundtrack/search?track=${encodeURIComponent(track)}&artist=${encodeURIComponent(artist)}`);
            const results = await res.json();

            resultContainer.innerHTML = "";

            if (results.length === 0) {
                resultContainer.innerHTML = '<button disabled>Ничего не найдено 😢</button>';
                return;
            }

            const url = results[0];
            const button = document.createElement("button");
            button.innerHTML = '<span style="display:inline-flex; align-items:center;"><span style="margin-right:8px;">🎬</span> <span>Открыть IMDb</span></span>';
            button.className = "playlist-link-btn";
            button.onclick = () => window.open(url, "_blank");

            resultContainer.appendChild(button);
        } catch (err) {
            resultContainer.innerHTML = '<button disabled>Ошибка при поиске 😞</button>';
            console.error(err);
        }
    });
});
