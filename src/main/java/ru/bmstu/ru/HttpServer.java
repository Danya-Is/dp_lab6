package ru.bmstu.ru;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;

public class HttpServer {
    ActorSystem actorSystem = ActorSystem.create("routes");
    final Http http = Http.get(actorSystem);
    final ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
    
}
