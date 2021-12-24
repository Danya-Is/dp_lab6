package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import ru.bmstu.ru.Messages.ServersListMessage;

import java.util.stream.Collectors;

public class NodeHandler {
    private ZooKeeper zooKeeper;
    private ActorRef storage;
    private String path;

    public NodeHandler(ZooKeeper zooKeeper, ActorRef storage, String path) {
        this.path = path;
        this.zooKeeper = zooKeeper;
        this.storage = storage;
    }

    private void watchChildren(WatchedEvent event) throws InterruptedException, KeeperException {
        storage.tell(new ServersListMessage(zooKeeper.getChildren(path, this::watchChildren).stream()
                .map(subPath -> createPath(path, subPath))
                .collect(Collectors.toList())), ActorRef.noSender());
    }

    private String createPath(String path, String subPath) {
        return path + "/" + subPath;
    }
}
