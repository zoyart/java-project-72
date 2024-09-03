package hexlet.code.repository;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hexlet.code.model.UrlCheck;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckRepository extends BaseRepository {

    public static void saveCheckedUrl(UrlCheck urlCheck) throws SQLException {
        String sql = "INSERT INTO url_checks "
                + "(status_code, title, h1, description, url_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, urlCheck.getStatusCode());
            pst.setString(2, urlCheck.getTitle());
            pst.setString(3, urlCheck.getH1());
            pst.setString(4, urlCheck.getDescription());
            pst.setLong(5, urlCheck.getUrlId());
            pst.setTimestamp(6, urlCheck.getCreatedAt());
            pst.executeUpdate();
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static List<UrlCheck> getListCheck(long id) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";
        List<UrlCheck> listChecks = new ArrayList<>();
        try (var conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet resultSet = pst.executeQuery();
            while (resultSet.next()) {
                var idCheck = resultSet.getLong("id");
                var urlId = resultSet.getLong("url_id");
                var status = resultSet.getInt("status_code");
                var title = resultSet.getString("title");
                var h1 = resultSet.getString("h1");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at");
                var urlCheck = new UrlCheck(status, title, h1, description, createdAt);
                urlCheck.setUrlId(urlId);
                urlCheck.setId(idCheck);
                listChecks.add(urlCheck);
            }
            return listChecks;
        }
    }

    public static Map<Long, UrlCheck> getListLastCheck() throws SQLException {
        String sql = "SELECT DISTINCT ON (url_id) * FROM url_checks "
                + " ORDER BY url_id DESC, id DESC";
        Map<Long, UrlCheck> listChecks = new HashMap<>();
        try (var conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            ResultSet resultSet = pst.executeQuery();
            while (resultSet.next()) {
                var idCheck = resultSet.getLong("id");
                var urlId = resultSet.getLong("url_id");
                var status = resultSet.getInt("status_code");
                var title = resultSet.getString("title");
                var h1 = resultSet.getString("h1");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at");
                var urlCheck = new UrlCheck(status, title, h1, description, createdAt);
                urlCheck.setUrlId(urlId);
                urlCheck.setId(idCheck);
                listChecks.put(urlId, urlCheck);
            }
            return listChecks;
        }
    }

    private static final Map<String, UrlCheck> CASH = new HashMap<>();

    public static UrlCheck parsingURL(String urlModel) throws UnirestException {
        Timestamp date = new Timestamp(System.currentTimeMillis());
        if (CASH.containsKey(urlModel)) {
            CASH.get(urlModel).setCreatedAt(date);
            return CASH.get(urlModel);
        }

        HttpResponse<String> response = Unirest.get(urlModel).asString();
        int statusCode = response.getCode();
        Document document = Jsoup.parse(response.getBody());
        Elements titleElement = document.select("head > title");
        Elements h1Element = document.select("h1");
        Elements descriptionMeta = document.select("meta[name=description]");
        String title = titleElement.text();
        String h1 = h1Element.text();
        String description = descriptionMeta.attr("content");
        var urlChecks = new UrlCheck(statusCode, title, h1, description, date);
        CASH.put(urlModel, urlChecks);
        return urlChecks;
    }
}
