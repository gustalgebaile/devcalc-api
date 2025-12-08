package com.devcalc;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/add", ctx -> {
            int a = Integer.parseInt(ctx.queryParam("a"));
            int b = Integer.parseInt(ctx.queryParam("b"));
            ctx.result(String.valueOf(a + b));
        });
    }
}
