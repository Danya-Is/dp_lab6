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
import akka.http.javadsl.model.Query;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class HttpServer {
    private static final int PORT = 8888;
    public static final String TEST_URL = "testUrl";
    public static final String LOCALHOST = "localhost";
    public static final String COUNT = "count";
    public static final String DEFAULT_COUNT = "1";
    public static final String STORAGE = "storage";

    private ActorSystem actorSystem;
    private ActorRef storage;
    private String host;
    private int port;

    public HttpServer(String host, int post) {
        this.host = host;
        this.port = post;
        actorSystem = ActorSystem.create("routes");
        storage = actorSystem.actorOf(Props.create(StorageActor.class), STORAGE);
    }

    public void run() throws IOException {

        final Http http = Http.get(actorSystem);
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);
        AsyncHttpClient client = asyncHttpClient();
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createFlow(casher, actorMaterializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, PORT),
                actorMaterializer
        );
        System.out.println("Listening...  " + PORT);
        System.in.read();

        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> actorSystem.terminate());
    }
}
