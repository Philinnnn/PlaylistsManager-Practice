document.getElementById("createPlaylistForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const body = {
        name: document.getElementById("playlistName").value,
        description: "Создан с фронта",
        isPublic: true,
        trackRequests: [
            { trackName: "девочка с каре", artistName: "МУККА" },
            { trackName: "клятвы", artistName: "Pyrokinesis" },
            { trackName: "ПОЛВТОРОГО", artistName: "KEER" },
            { trackName: "я приду к тебе с клубникой в декабре", artistName: "Pyrokinesis" },
        ]
            // Пока сделал заглушку, если хочешь, можешь фронт сделать
    };

    fetch("/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
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
