package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;
import ru.bmstu.ru.Messages.ServersListMessage;

public class NodeHandler {
    private ZooKeeper zooKeeper;
    private ActorRef storage;
    private String path;

    public NodeHandler(ZooKeeper zooKeeper, ActorRef storage, String path) {
        this.path = path;
        this.zooKeeper = zooKeeper;
        this.storage = storage;
    }

    private void watchChildren() {
        storage.tell(new ServersListMessage(zooKeeper.getChildren(path, event -> watchChildren(event))));
    }
}
