package ru.bmstu.ru;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import ru.bmstu.ru.Messages.GetRandomServerMessage;
import ru.bmstu.ru.Messages.RandomServerMessage;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class HttpServer {
    private static final int PORT = 8888;
    public static final String TEST_URL = "url";
    public static final String LOCALHOST = "localhost";
    public static final String COUNT = "count";
    public static final String STORAGE = "storage";
    public static final String SERVERS_PATH = "/servers";
    public static final String HTTP = "http://";

    private ActorSystem actorSystem;
    private ActorRef storage;
    private String host, path;
    private int port;
    private AsyncHttpClient client;
    private CompletionStage<ServerBinding> binding;

    public HttpServer(String host, int post) {
        this.host = host;
        this.port = post;
        actorSystem = ActorSystem.create("routes");
        storage = actorSystem.actorOf(Props.create(StorageActor.class), STORAGE);
    }

    public HttpServer(ActorRef storage, AsyncHttpClient client) {
        this.storage = storage;
        this.client = client;
    }

    public void run() {

        final Http http = Http.get(actorSystem);
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);
        AsyncHttpClient client = asyncHttpClient();

        NodeHandler nodeHandler = new NodeHandler(host, port, storage, SERVERS_PATH);
        nodeHandler.start(LOCALHOST + ":" + PORT, host, String.valueOf(port));

        final HttpServer instance = new HttpServer(storage, client);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = instance.createRoute()
                .flow(actorSystem, actorMaterializer);

        binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, port),
                actorMaterializer
        );
        System.out.println("Listening...  " + port);
    }

    public void end() {
        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> actorSystem.terminate());
    }

    public Route createRoute() {
        return route(
                get(() -> parameter(TEST_URL, url ->
                            parameter(COUNT, count ->
                                handleRequest(url, Integer.parseInt(count))
                            )
                        )
                )
        );
    }

    private Route handleRequest(String url, int count) {
        CompletionStage<Response> response = count > 0 ?
                transferRequest(url, count)
                :
                doRequest(client.prepareGet(url).build());

        return completeOKWithFutureString(response.thenApply(Response::getResponseBody));
    }

    private CompletionStage<Response> transferRequest(String url, int count) {
        return Patterns.ask(storage, new GetRandomServerMessage(), Duration.ofSeconds(2))
                .thenApply(randomServer -> ((RandomServerMessage)randomServer).getServer())
                .thenCompose(msg -> doRequest(makeRequest(msg, url, count - 1)));
    }

    private CompletionStage<Response> doRequest(Request request) {
        System.out.println("send request to " + request.getUrl());
        return client.executeRequest(request).toCompletableFuture();
    }

    private Request makeRequest(String serverUrl, String testUrl, int count) {
        return client.prepareGet(HTTP + serverUrl)
                .addQueryParam(TEST_URL, testUrl)
                .addQueryParam(COUNT, String.valueOf(count))
                .build();
    }
}
