package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {

    public static Handler newUrl = ctx -> {
        ctx.attribute("url", "");
        ctx.render("index.html");
    };
}
