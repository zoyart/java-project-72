package hexlet.code.controllers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.App;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.BaseRepository;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.Unirest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UrlControllerTest {

    private static Javalin app;
    private static String baseUrl;

    @BeforeAll
    public static void beforeAll() throws SQLException, IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @Test
    void getApp() throws SQLException, IOException {
        Assertions.assertNotNull(App.getApp());
    }


    @BeforeEach
    void beforeEach() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");

        var dataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource("migration_test.sql");
        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
                .collect(Collectors.joining("\n"));

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;
    }

    @Test
    void testUrls() throws SQLException {
        UrlRepository.save(new Url("https://roman.com"));
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        String content = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://roman.com");
    }

    @Test
    void testShowUrl() throws SQLException {
        HttpResponse<String> responsePost = Unirest.post(baseUrl + "/urls")
                .field("url", "https://bazzara.com")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        Url actualUrl = UrlRepository.findByName("https://bazzara.com").get();
        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo("https://bazzara.com");

        String id = String.valueOf(actualUrl.getId());

        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void checkUrl() throws Exception {
        MockWebServer server = new MockWebServer();

        File file = new File(
                getClass().getClassLoader().getResource("good.html").getFile()
        );

        String content = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream()
                .collect(Collectors.joining());

        server.enqueue(new MockResponse().setBody(content));

        server.start();
        HttpUrl goodUrl = server.url("");

        Url url = new Url(goodUrl.toString());
        UrlRepository.save(url);

        List<UrlCheck> urlChecks = UrlCheckRepository.getEntities();
        assertThat(urlChecks.isEmpty()).isTrue();
        HttpResponse<String> responseCreateCheck = Unirest.post(baseUrl + "/urls/" + url.getId() + "/checks")
                .asString();
        assertThat(responseCreateCheck.getStatus()).isEqualTo(HttpStatus.FOUND);

        urlChecks = UrlCheckRepository.findByUrlId(url.getId());

        assertThat(urlChecks.isEmpty()).isFalse();
        UrlCheck urlCheck = UrlCheckRepository.findByUrlId(url.getId()).stream().findFirst().get();

        assertThat(urlCheck.getDescription()).isEqualTo("description");
        assertThat(urlCheck.getH1()).isEqualTo("Header1");
        assertThat(urlCheck.getTitle()).isEqualTo("title");

        server.shutdown();
    }

    @Test
    void testNewUrlValid() throws SQLException {
        // Выполняем POST запрос при помощи агента Unirest
        HttpResponse<String> responsePost = Unirest
                // POST запрос на URL
                .post(baseUrl + "/urls")
                // Устанавливаем значения полей
                .field("url", "https://bazzara.it")
                // Выполняем запрос и получаем тело ответ с телом в виде строки
                .asString();

        // Проверяем статус ответа
        assertThat(responsePost.getStatus()).isEqualTo(302);

        Url actualUrl = UrlRepository.findByName("https://bazzara.it").get();

        assertThat(actualUrl).isNotNull();

        assertThat(actualUrl.getName()).isEqualTo("https://bazzara.it");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();

        String content = response.getBody();
        assertThat(content).contains("Страница успешно добавлена");
    }

    @Test
    void testNewUrlNotValid() throws SQLException {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", "isError")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(422);

        var isEmpty = UrlRepository.findByName("isError").isEmpty();

        assertThat(isEmpty).isTrue();

        String content = responsePost.getBody();
        assertThat(content).contains("Некорректный URL");
        assertThat(content).doesNotContain("isError");
    }

    @Test
    void testNewDuplicationUrlNotValid() {
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl
                        + "/urls")
                .field("url", "https://bazzara.de")
                .asString();
        assertThat(responsePost.getStatus()).isEqualTo(302);

        responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", "https://bazzara.de")
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        String location = responsePost.getHeaders().get("Location").<String>get(0);
        HttpResponse<String> responseGet = Unirest.get(baseUrl + location).asString();

        String content = responseGet.getBody();
        assertThat(content).contains("Страница уже существует");
    }
}
