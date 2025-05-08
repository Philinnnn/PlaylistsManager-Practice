package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

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
    public String dashboard() {
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
}

