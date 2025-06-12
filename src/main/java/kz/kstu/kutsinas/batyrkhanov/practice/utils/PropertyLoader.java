package kz.kstu.kutsinas.batyrkhanov.practice.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class PropertyLoader {
    public static Map<String, String> loadEnv() {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/.env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] pair = line.split("=", 2);
                    env.put(pair[0].trim(), pair[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading .env file: " + e.getMessage());
        }
        return env;
    }
}
