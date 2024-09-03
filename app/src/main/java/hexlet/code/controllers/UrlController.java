package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public final class UrlController {
    public static final String NEW_URL_FORM_PARAM = "url";
    public static Handler listUrls = ctx -> {
        List<Url> urls = UrlRepository.getEntities();

        Map<Long, UrlCheck> urlChecks = UrlCheckRepository.findLatestChecks();

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls.html");
    };


    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = UrlRepository.findById(id).get();
        var urlChecks = UrlCheckRepository.findByUrlId(url.getId());
        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("show.html");
    };

    public static Handler createUrl = ctx -> {
        String newUrl = ctx.formParam(NEW_URL_FORM_PARAM);
        URL javaNetUrl;
        try {
            javaNetUrl = new URI(newUrl).toURL();
        } catch (RuntimeException e) {
            ctx.sessionAttribute("error", "Некорректный URL");
            ctx.attribute(NEW_URL_FORM_PARAM, "");
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            ctx.render("index.html");
            return;
        }

        String urlToSave = new URL(javaNetUrl.getProtocol(), javaNetUrl.getHost(), javaNetUrl.getPort(), "")
                .toURI()
                .toString();

        if (UrlRepository.findByName(urlToSave).isPresent()) {
            ctx.sessionAttribute("error", "Страница уже существует");
            ctx.attribute(NEW_URL_FORM_PARAM, newUrl);
            ctx.redirect("/urls");
            return;
        }

        Url url = new Url(urlToSave);
        UrlRepository.save(url);
        ctx.sessionAttribute("success", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };


    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = UrlRepository.findById(id).get();
        HttpResponse<String> responseUrl;

        try {
            responseUrl = Unirest.get(url.getName()).asString();
            int statusCode = responseUrl.getStatus();
            Document doc = Jsoup.parse(responseUrl.getBody());
            String title = doc.title();
            String h1 = Optional.ofNullable(doc.select("h1").first()).orElse(new Element("h1")).text();
            String description = Optional.ofNullable(doc.select("meta[name='description']").first())
                    .orElse(new Element("meta")).attr("content");

            UrlCheck newUrlCheck = new UrlCheck(statusCode, title, h1, description, url.getId());
            UrlCheckRepository.save(newUrlCheck);

            ctx.sessionAttribute("success", "Страница успешно проверена");

        } catch (Exception e) {
            ctx.sessionAttribute("error", "Некорректная страница");
        }

        ctx.redirect("/urls/" + url.getId());
    };
}
