package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;

public class NodeHandler {
    private ZooKeeper zoo;
    private ActorRef storage;
    private String path;

    public NodeHandler(ZooKeeper zoo, ActorRef storage, String path) {
        this.path = path;
        this.zoo = zoo;
        this.storage = storage;
    }
}
