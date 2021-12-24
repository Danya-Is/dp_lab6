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
    private static final String LOCALHOST = "";
    ActorSystem actorSystem = ActorSystem.create("routes");
    final Http http = Http.get(actorSystem);
    final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
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
