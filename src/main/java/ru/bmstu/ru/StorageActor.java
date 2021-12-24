package ru.bmstu.ru;

import akka.actor.AbstractActor;
import ru.bmstu.ru.Messages.GetRandomServerMessage;
import ru.bmstu.ru.Messages.RandomServerMessage;

import java.util.ArrayList;

public class StorageActor extends AbstractActor {

    private ArrayList<String> storage = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetRandomServerMessage.class, )
                .build();
    }

    private void sendRandomServerMessage() {
        sender().tell(new RandomServerMessage());
    }
}
