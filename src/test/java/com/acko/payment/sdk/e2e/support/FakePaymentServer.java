package com.acko.payment.sdk.e2e.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.acko.payment.sdk.e2e.support.MockResponses.json;

public final class FakePaymentServer {

    private final BlockingQueue<ScriptedResponse> responses = new LinkedBlockingQueue<>();
    private final CopyOnWriteArrayList<RecordedRequest> requests = new CopyOnWriteArrayList<>();
    private HttpServer httpServer;

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/", this::handle);
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();
    }

    public void enqueue(ScriptedResponse response) {
        responses.add(response);
    }

    public String url(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return "http://127.0.0.1:" + httpServer.getAddress().getPort() + normalizedPath;
    }

    public List<RecordedRequest> requests() {
        return new ArrayList<>(requests);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String path = exchange.getRequestURI().getRawPath();
        String query = exchange.getRequestURI().getRawQuery();
        String pathWithQuery = query == null ? path : path + "?" + query;
        requests.add(new RecordedRequest(
                exchange.getRequestMethod(),
                pathWithQuery,
                exchange.getRequestHeaders(),
                body));

        ScriptedResponse response = responses.poll();
        if (response == null) {
            response = json(500, "{\"error\":\"no scripted response\"}");
        }
        if (response.delayMs() > 0) {
            sleep(response.delayMs());
        }

        byte[] responseBody = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().put("Content-Type", List.of("application/json"));
        try {
            exchange.sendResponseHeaders(response.status(), responseBody.length);
            exchange.getResponseBody().write(responseBody);
        } catch (IOException ignored) {
            // The client may intentionally time out before a delayed response is written.
        } finally {
            exchange.close();
        }
    }

    private static void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
