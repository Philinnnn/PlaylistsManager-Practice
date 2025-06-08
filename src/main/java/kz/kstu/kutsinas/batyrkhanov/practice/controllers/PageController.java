package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final AppUserRepo appUserRepo;
    private final UserSession userSession;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean spotifyLinked = false;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            AppUser user = appUserRepo.findByUsername(username).orElse(null);
            if (user != null && user.getSpotifyUser() != null && user.getSpotifyUser().getId() != null) {
                spotifyLinked = true;
            }
        }
        model.addAttribute("spotifyLinked", spotifyLinked);
        return "dashboard";
    }

    @GetMapping("/create-playlist")
    public String createPlaylist() {
        return "createPlaylist";
    }

    @GetMapping("/spotify/create-playlist-page")
    public String createPlaylistPage() {
        return "createPlaylist";
    }

    @GetMapping("/create-playlist-by-top-tracks")
    public String createPlaylistByTopTracks() {
        return "createPlaylistByTopTracks";
    }

    @GetMapping("/sound-pick")
    public String soundPick() {
        return "soundPick";
    }

    @GetMapping("/soundtrack/page")
    public String showSoundtrackPage() {
        return "soundtrack";
    }

    @GetMapping("/youtube")
    public String showYouTubePage() {
        return "youtube";
    }

    @GetMapping("/2fa-verify")
    public String twofaVerify() {
        return "2fa-verify";
    }
}
