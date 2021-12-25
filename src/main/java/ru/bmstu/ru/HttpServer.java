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
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import ru.bmstu.ru.Messages.GetRandomServerMessage;
import ru.bmstu.ru.Messages.RandomServerMessage;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static akka.http.javadsl.server.Directives.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class HttpServer {
    private static final int PORT = 8888;
    public static final String TEST_URL = "testUrl";
    public static final String LOCALHOST = "localhost";
    public static final String COUNT = "count";
    public static final String DEFAULT_COUNT = "1";
    public static final String STORAGE = "storage";
    public static final String SERVERS_PATH = "/servers";

    private ActorSystem actorSystem;
    private ActorRef storage;
    private String host;
    private int port;
    private ZooKeeper zooKeeper;
    private AsyncHttpClient client;

    private Logger logger = Logger.getLogger(HttpServer.class.getName());

    public HttpServer(String host, int post) {
        this.host = host;
        this.port = post;
        actorSystem = ActorSystem.create("routes");
        storage = actorSystem.actorOf(Props.create(StorageActor.class), STORAGE);
    }

    public HttpServer(ActorRef storage, AsyncHttpClient client, ZooKeeper zooKeeper) {
        this.storage = storage;
        this.client = client;
        this.zooKeeper = zooKeeper;
    }

    public void run() throws IOException {

        final Http http = Http.get(actorSystem);
        final ZooKeeper zooKeeper = new ZooKeeper(LOCALHOST + ":" + PORT, 2000, w -> logger.info(w.toString()));
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);
        AsyncHttpClient client = asyncHttpClient();

        NodeHandler nodeHandler = new NodeHandler(zooKeeper, storage, SERVERS_PATH);
        nodeHandler.start(LOCALHOST + ":" + port, host, String.valueOf(port));

        final HttpServer instance = new HttpServer(storage, client, zooKeeper);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = instance.createRoute(actorSystem)
                .flow(actorSystem, actorMaterializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, PORT),
                actorMaterializer
        );
        System.out.println("Listening...  " + PORT);
        System.in.read();

        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> actorSystem.terminate());
    }

    public Route createRoute(ActorSystem actorSystem) {
        return route(
                get(() -> parameter("url", url ->
                            parameter("count", count ->
                                handleRequest(url, Integer.parseInt(count))
                            )
                        )
                )
        );
    }

    private Route handleRequest(String url, int count) {
        CompletionStage<Response> response = count > 1 ?
                doRequest(client.prepareGet(url).build()) :
                transferRequest(url, count);

        return completeOKWithFutureString(response.thenApply(Response::getResponseBody));
    }

    private CompletionStage<Response> transferRequest(String url, int count) {
        return Patterns.ask(storage, new GetRandomServerMessage(), Duration.ofSeconds(2))
                .thenApply(randomServer -> ((RandomServerMessage)randomServer).getServer())
                .thenCompose(msg -> doRequest(makeRequest(getServerUrl(msg), url, count - 1)));
    }

    private CompletionStage<Response> doRequest(Request request) {
        return client.executeRequest(request).toCompletableFuture();
    }

    private Request makeRequest(String serverUrl, String testUrl, int count) {
        return client.prepareGet(serverUrl)
                .addQueryParam(TEST_URL, testUrl)
                .addQueryParam(COUNT, String.valueOf(count))
                .build();
    }

    private String getServerUrl(String path){
        try {
            return Arrays.toString(zooKeeper.getData(path, false, null));
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }
}
