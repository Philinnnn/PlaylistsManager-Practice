let tracks = [];
let index = 0;
const likedTracks = [];

async function fetchTracks() {
    toggleLoading(true);
    const response = await fetch("/sound-pick/tracks");
    if (response.ok) {
        tracks = await response.json();
        index = 0;
        toggleLoading(false);
        showNextTrack();
    } else {
        document.getElementById("result").textContent = "Не удалось загрузить треки";
        document.getElementById("result").className = "error";
        toggleLoading(false);
    }
}

function toggleLoading(isLoading) {
    document.getElementById("loader").style.display = isLoading ? "block" : "none";
    document.getElementById("track-info").style.display = isLoading ? "none" : "block";
    document.getElementById("voteForm").style.display = isLoading ? "none" : "flex";
}

function showNextTrack() {
    if (index >= tracks.length) {
        finishVoting();
        return;
    }

    const track = tracks[index];
    document.getElementById("artist").textContent = track.artistName;
    document.getElementById("track").textContent = track.trackName;
    document.getElementById("trackName").value = track.trackName;
    document.getElementById("artistName").value = track.artistName;

    const player = document.getElementById("spotifyPlayer");
    if (track.spotifyId) {
        player.innerHTML = `
            <iframe style="border-radius:12px" src="https://open.spotify.com/embed/track/${track.spotifyId}?utm_source=generator"
                width="100%" height="80" frameBorder="0" allowtransparency="true"
                allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture" loading="lazy">
            </iframe>`;
    } else {
        player.innerHTML = `<p style="text-align: center; color: #888;">Превью недоступно</p>`;
    }
}

async function sendVote(vote) {
    const trackName = document.getElementById("trackName").value;
    const artistName = document.getElementById("artistName").value;

    await fetch("/sound-pick/vote", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ trackName, artistName, vote })
    });

    if (vote === "like") {
        likedTracks.push({ trackName, artistName });
    }

    index++;
    showNextTrack();
}

async function finishVoting() {
    document.getElementById("track-container").innerHTML = `
        <h2>Голосование завершено</h2>
        <p>Вы выбрали ${likedTracks.length} трек(ов).</p>
    `;

    if (likedTracks.length > 0) {
        const request = {
            name: "SoundPick Playlist",
            description: "Плейлист из понравившихся треков через SoundPick",
            isPublic: false,
            trackRequests: likedTracks // ← ключевое исправление
        };

        const response = await fetch("/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });

        if (response.ok) {
            const playlistId = await response.text();
            const url = `https://open.spotify.com/playlist/${playlistId}`;
            document.getElementById("track-container").innerHTML += `<p>✅ <a href="${url}" target="_blank">Плейлист создан</a></p>`;
        } else {
            document.getElementById("track-container").innerHTML += `<p style="color:red;">⚠ Не удалось создать плейлист</p>`;
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("likeBtn").addEventListener("click", () => sendVote("like"));
    document.getElementById("dislikeBtn").addEventListener("click", () => sendVote("dislike"));
    document.getElementById("stopBtn").addEventListener("click", () => finishVoting());

    fetchTracks();
});
