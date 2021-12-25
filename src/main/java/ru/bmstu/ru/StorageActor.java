package ru.bmstu.ru;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import ru.bmstu.ru.Messages.GetRandomServerMessage;
import ru.bmstu.ru.Messages.RandomServerMessage;
import ru.bmstu.ru.Messages.ServersListMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StorageActor extends AbstractActor {

    private List<String> storage = new ArrayList<>();
    private Random random = new Random();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetRandomServerMessage.class, msg ->
                        sender().tell(new RandomServerMessage(storage.get(random.nextInt(storage.size()))), ActorRef.noSender()))
                .match(ServersListMessage.class, msg -> storage.addAll(msg.getServers()))
                .build();
    }
}
