package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepo appUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final UserSession session;

    @Autowired
    public AuthController(AppUserRepo appUserRepo,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authManager,
                          UserSession session) {
        this.appUserRepo = appUserRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.session = session;
    }

    /**
     * Регистрация нового пользователя приложения
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String rawPassword = request.get("password");

        if (username == null || rawPassword == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        if (appUserRepo.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        appUserRepo.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Вход в систему
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // сохраняем в сессию
            httpRequest.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            session.setUsername(username);

            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    /**
     * Выход из системы
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate(); // сбрасываем сессию
        SecurityContextHolder.clearContext(); // сбрасываем контекст безопасности
        return ResponseEntity.ok("Logged out successfully");
    }
}
