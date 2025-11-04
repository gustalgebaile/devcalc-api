package com.devcalc;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        CalculatorService calculatorService = new CalculatorService();

        app.get("/add", ctx -> {
            int a = Integer.parseInt(ctx.queryParam("a"));
            int b = Integer.parseInt(ctx.queryParam("b"));
            ctx.result(String.valueOf(calculatorService.add(a, b)));
        });

        app.get("/subtract", ctx -> {
            int a = Integer.parseInt(ctx.queryParam("a"));
            int b = Integer.parseInt(ctx.queryParam("b"));
            ctx.result(String.valueOf(calculatorService.subtract(a, b)));
        });

        app.get("/multiply", ctx -> {
            int a = Integer.parseInt(ctx.queryParam("a"));
            int b = Integer.parseInt(ctx.queryParam("b"));
            ctx.result(String.valueOf(calculatorService.multiply(a, b)));
        });

        app.get("/divide", ctx -> {
            int a = Integer.parseInt(ctx.queryParam("a"));
            int b = Integer.parseInt(ctx.queryParam("b"));
            try {
                ctx.result(String.valueOf(calculatorService.divide(a, b)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            }
        });
    }
}
