package kz.kstu.kutsinas.batyrkhanov.practice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import kz.kstu.kutsinas.batyrkhanov.practice.entities.AppUser;
import kz.kstu.kutsinas.batyrkhanov.practice.repositories.AppUserRepo;
import kz.kstu.kutsinas.batyrkhanov.practice.services.AuthService;
import kz.kstu.kutsinas.batyrkhanov.practice.utils.TOTPUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorController {
    private final AppUserRepo appUserRepo;
    private final AuthService authService;

    @PostMapping("/setup")
    public ResponseEntity<?> setup2FA(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = appUserRepo.findByUsername(username).orElseThrow();
        try {
            String secret = TOTPUtil.generateSecret();
            user.setTotpSecret(secret);
            appUserRepo.save(user);
            String qrUrl = TOTPUtil.getQRBarcodeURL(username, secret);
            return ResponseEntity.ok(Map.of("qrUrl", qrUrl, "secret", secret));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка генерации секрета");
        }
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable2FA(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = appUserRepo.findByUsername(username).orElseThrow();
        String code = body.get("code");
        try {
            System.out.println("[2FA] Проверка кода: " + code + ", секрет: " + user.getTotpSecret());
            if (TOTPUtil.verifyCode(user.getTotpSecret(), code)) {
                user.setTwoFactorEnabled(true);
                appUserRepo.save(user);
                return ResponseEntity.ok("2FA enabled");
            } else {
                System.out.println("[2FA] Неверный код! Секрет: " + user.getTotpSecret() + ", код: " + code);
                return ResponseEntity.status(401).body("Invalid code");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка проверки кода");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify2FA(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String code = body.get("code");
        AppUser user = appUserRepo.findByUsername(username).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ResponseEntity.status(404).body("2FA not enabled");
        }
        try {
            if (TOTPUtil.verifyCode(user.getTotpSecret(), code)) {
                authService.finishLoginAfter2fa(username, request);
                return ResponseEntity.ok("2FA success");
            } else {
                return ResponseEntity.status(401).body("Invalid code");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка проверки кода");
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = appUserRepo.findByUsername(username).orElseThrow();
        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        appUserRepo.save(user);
        return ResponseEntity.ok("2FA disabled");
    }

    @RequestMapping("/status")
    public ResponseEntity<?> status2FA() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = appUserRepo.findByUsername(username).orElse(null);
        boolean enabled = user != null && Boolean.TRUE.equals(user.getTwoFactorEnabled());
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }
}
