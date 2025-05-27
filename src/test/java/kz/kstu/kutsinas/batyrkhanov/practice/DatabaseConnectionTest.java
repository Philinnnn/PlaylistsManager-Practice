package kz.kstu.kutsinas.batyrkhanov.practice;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    private Map<String, String> loadEnv() {
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
            fail("Не удалось прочитать .env файл: " + e.getMessage());
        }
        return env;
    }

    @Test
    public void testDatabaseConnection() {
        Map<String, String> env = loadEnv();

        String url = env.get("DB_URL");
        String user = env.get("DB_USERNAME");
        String password = env.get("DB_PASSWORD");
        String driver = env.get("DB_DRIVER_CLASS_NAME");

        assertNotNull(url, "DB_URL не задан в .env");
        assertNotNull(user, "DB_USERNAME не задан в .env");
        assertNotNull(password, "DB_PASSWORD не задан в .env");
        assertNotNull(driver, "DB_DRIVER_CLASS_NAME не задан в .env");

        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            assertFalse(connection.isClosed(), "Соединение не должно быть закрыто");
            System.out.println("✅ Успешное подключение к базе данных");
            connection.close();
        } catch (Exception e) {
            fail("Ошибка подключения к БД: " + e.getMessage());
        }
    }
}