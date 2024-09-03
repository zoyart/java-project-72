package hexlet.code.controllers;

import hexlet.code.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

class RootControllerTest {
    private static Javalin app;

    @BeforeAll
    public static void beforeAll() throws SQLException, IOException {
        // Получаем инстанс приложения
        app = App.getApp();
        // Запускаем приложение на рандомном порту
        app.start(0);
        // Получаем порт, на которм запустилось приложение
        int port = app.port();
    }

    @AfterAll
    public static void afterAll() {
        // Останавливаем приложение
        app.stop();
    }

    @Test
    void getApp() throws SQLException, IOException {
        Assertions.assertNotNull(App.getApp());
    }
}
