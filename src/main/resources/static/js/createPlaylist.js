document.getElementById("createPlaylistForm").addEventListener("submit", function(e) {
    e.preventDefault();
    const playlistName = document.getElementById("playlistName").value;
    const genre = document.getElementById("genre").value;
    const region = document.getElementById("region").value;

    fetch("/spotify/create-playlist", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ playlistName, genre, region })
    })
        .then(response => response.text())
        .then(data => {
            const resultDiv = document.getElementById("result");
            resultDiv.innerText = data;
            resultDiv.className = "success";
        })
        .catch(error => {
            const resultDiv = document.getElementById("result");
            resultDiv.innerText = "Ошибка: " + error;
            resultDiv.className = "error";
        });
});