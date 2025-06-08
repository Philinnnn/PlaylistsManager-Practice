package kz.kstu.kutsinas.batyrkhanov.practice.utils;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;

public class TOTPUtil {
    private static final String ISSUER = "PlaylistsManager";
    private static final Duration STEP = Duration.ofSeconds(30);

    public static String generateSecret() {
        byte[] buffer = new byte[10]; // 80 бит = 16 символов base32
        new java.security.SecureRandom().nextBytes(buffer);
        Base32 base32 = new Base32();
        return base32.encodeToString(buffer).replace("=", "");
    }

    public static String getQRBarcodeURL(String username, String secret) {
        String otpAuth = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                ISSUER,
                URLEncoder.encode(username, StandardCharsets.UTF_8),
                secret,
                ISSUER
        );
        return "https://api.qrserver.com/v1/create-qr-code/?data=" + URLEncoder.encode(otpAuth, StandardCharsets.UTF_8) + "&size=200x200";
    }

    public static boolean verifyCode(String secret, String code) throws Exception {
        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator(STEP);
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(secret);
        Key key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA1");
        Instant now = Instant.now();
        int validCodeNow = totp.generateOneTimePassword(key, now);
        int validCodePrev = totp.generateOneTimePassword(key, now.minus(STEP));
        String codeStr = String.format("%06d", validCodeNow);
        String codePrevStr = String.format("%06d", validCodePrev);
        return codeStr.equals(code) || codePrevStr.equals(code);
    }
}
