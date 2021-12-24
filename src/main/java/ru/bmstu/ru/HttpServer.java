package ru.bmstu.ru;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.concurrent.CompletionStage;

public class HttpServer {
    private static final int PORT = 8888;
    private static final String LOCALHOST = "localhost";
    ActorSystem actorSystem = ActorSystem.create("routes");

    public HttpServer() {
        final Http http = Http.get(actorSystem);
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);
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

    private static Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorRef casher, ActorMaterializer actorMaterializer) {
        return Flow.of(HttpRequest.class)
                .map(request -> {
                    Query query = request.getUri().query();
                    String url = query.get(TEST_URL).orElse(LOCALHOST);
                    int count = Integer.parseInt(query.get(COUNT).orElse(DEFAULT_COUNT));
                    return new Pair<>(url, count);
                })
                .mapAsync(4, (pair -> Patterns.ask(casher, pair.first(), Duration.ofSeconds(5)).thenCompose(time -> {
                    if ((float) time >= 0) {
                        return CompletableFuture.completedFuture(new Pair<>(pair.first(), (float)time));
                    }
                    return Source.from(Collections.singletonList(pair))
                            .toMat(createSink(pair.second()), Keep.right())
                            .run(actorMaterializer)
                            .thenApply(t -> new Pair<>(pair.first(), (float)t/pair.second()));
                })))
                .map(result -> {
                    casher.tell(new Response(result.first(), result.second()), ActorRef.noSender());
                    return HttpResponse.create().withEntity("RESULT " + result.first() + ": " + result.second() + "\n");
                });
    }
}
