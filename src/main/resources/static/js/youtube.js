document.getElementById("youtubeForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const query = document.getElementById("query").value;
    const response = await fetch(`/api/youtube/search?q=${encodeURIComponent(query)}`);
    const data = await response.json();
    const container = document.getElementById("videos");
    container.innerHTML = "";

    if (Array.isArray(data)) {
        data.forEach(video => {
            const div = document.createElement("div");
            div.className = "video-item";
            div.innerHTML = `
          <iframe 
            src="https://www.youtube.com/embed/${video.videoId}" 
            frameborder="0" 
            allowfullscreen>
          </iframe>
          <p>${video.title}</p>
        `;
            container.appendChild(div);
        });
    } else {
        container.innerHTML = "<p>Ошибка при загрузке видео.</p>";
    }
});