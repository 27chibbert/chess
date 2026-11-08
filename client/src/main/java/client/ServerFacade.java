package client;

import records.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import com.google.gson.Gson;

public class ServerFacade {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final String host = "localhost";
    private final int port = 8080;
    private static Gson serializer = new Gson();
    public ServerFacade() {

    }

    public enum Responses {
        AUTH,
        GAME,
        GAMES,
        ERROR
    }
    public ServerMessage register(UserDAO user) {
        try {
            var body = serializer.toJson(user);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(uriStr("/user")))
                    .timeout(java.time.Duration.ofMillis(5000))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                AuthDAO auth = serializer.fromJson(res.body(), AuthDAO.class);
                return new ServerMessage(auth);
            } else {
                return errorHandler(res);
            }
        } catch (Exception e) {
            return new ServerMessage("Timeout or Client-side Error");
        }
    }

    public ServerMessage login(UserLogin login) {
        try {
            var body = serializer.toJson(login);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(uriStr("/session")))
                    .timeout(java.time.Duration.ofMillis(5000))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                AuthDAO auth = serializer.fromJson(res.body(), AuthDAO.class);
                return new ServerMessage(auth);
            } else {
                return errorHandler(res);
            }
        } catch (Exception e) {
            return new ServerMessage("Timeout or Client-side Error");
        }
    }

    public ServerMessage logout(String authToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(uriStr("/session")))
                    .timeout(java.time.Duration.ofMillis(5000))
                    .DELETE()
                    .header("authorization", authToken)
                    .build();
            var res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                return new ServerMessage(null);
            } else {
                return errorHandler(res);
            }
        } catch (Exception e) {
            return new ServerMessage("Timeout or Client-side Error");
        }
    }

    private String uriStr(String path) {
        return String.format(Locale.getDefault(), "http://%s:%d%s", host, port, path);
    }

    private ServerMessage errorHandler(HttpResponse res) {
        switch (res.statusCode()) {
            case 0 -> {
                return new ServerMessage("Recieved no response");
            }
            case 400 -> {
                return new ServerMessage("Bad Request");
            }
            case 401 -> {
                return new ServerMessage("Unauthorized");
            }
            case 403 -> {
                return new ServerMessage("Already Taken");
            }
            case 500 -> {
                return new ServerMessage(res.body());
            }
            default -> {
                return new ServerMessage("Something has inexplicably broken.");
            }
        }
    }
}
