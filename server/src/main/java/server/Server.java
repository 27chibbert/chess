package server;

import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import dataaccess.InvalidLoginException;
import io.javalin.*;
import io.javalin.http.Context;
import com.google.gson.Gson;
import records.*;
import service.ServiceProvider;

public class Server {

    private final Javalin javalin;
    private ServiceProvider service;
    private static Gson serializer = new Gson();

    public Server() {
        service = new ServiceProvider();

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        // Register your endpoints and exception handlers here - exception handlers handled in functions
        javalin.post("/user", ctx -> postUser(ctx));
        javalin.post("/session", ctx -> postSession(ctx));
        javalin.post("/game", ctx -> ctx.result("{}"));
        javalin.delete("/db", ctx -> ctx.result("{}"));
        javalin.delete("/session", ctx -> ctx.result("{}"));
        javalin.put("/game", ctx -> ctx.result("{}"));
        javalin.get("/game", ctx -> ctx.result("{}"));
        //
    }

    private void postUser(Context ctx) {
        UserDAO userData;
        try {
            userData = serializer.fromJson(ctx.body(), UserDAO.class);
        } catch (JsonSyntaxException e) {
            badRequest(ctx);
            return;
        }
        AuthDAO authData;
        try {
            authData = service.registerUser(userData.username(), userData.password(), userData.email());
        } catch (DataAccessException e) {
            ctx.status(403);
            ctx.result("{ \"message\":\"Error: already taken\" }");
            return;
        } catch (Exception e) {
            serverError(ctx, e);
            return;
        }
        ctx.result(serializer.toJson(authData));
    }

    private void postSession(Context ctx) {
        UserLogin login;
        try {
            login = serializer.fromJson(ctx.body(), UserLogin.class);
        } catch (JsonSyntaxException e) {
            badRequest(ctx);
            return;
        }
        AuthDAO authData;
        try {
            authData = service.loginUser(login.username(), login.password());
        } catch (InvalidLoginException e) {
            unauthorized(ctx);
            return;
        } catch (Exception e) {
            serverError(ctx, e);
            return;
        }
        ctx.result(serializer.toJson(authData));
    }

    private void badRequest(Context ctx) {
        ctx.status(400);
        ctx.result("{ \"message\":\"Error: bad request\" }");
    }

    private void unauthorized(Context ctx) {
        ctx.status(401);
        ctx.result("{\"message\":\"Error: unauthorized\"}");
    }

    private void serverError(Context ctx, Exception e) {
        ctx.status(500);
        String error = "{ \"message\":\"Error:" + e.getMessage() + "\"}";
        ctx.result(error);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
