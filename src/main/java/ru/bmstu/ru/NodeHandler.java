package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;
import ru.bmstu.ru.Messages.ServersListMessage;

import java.util.stream.Collectors;

public class NodeHandler {
    private ZooKeeper zooKeeper;
    private ActorRef storage;
    private String host, path;
    private int port;

    public NodeHandler(String host, String port, ActorRef storage, String path) {
        this.path = path;
        this.host = host;
        this.port = port;
        this.zooKeeper = zooKeeper;
        this.storage = storage;
    }

    public void start(String name, String host, String port){
        try {
            zooKeeper.create(path + "/" + name, (host + ":" + port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (InterruptedException| KeeperException e) {
            throw new RuntimeException(e);
        }

    }

    private void watchChildren(WatchedEvent event) {
        try{
            storage.tell(new ServersListMessage(zooKeeper.getChildren(path, this::watchChildren).stream()
                    .map(subPath -> createPath(path, subPath))
                    .collect(Collectors.toList())), ActorRef.noSender());
        } catch (InterruptedException| KeeperException e) {
            throw new RuntimeException(e);
        }

    }

    private String createPath(String path, String subPath) {
        return path + "/" + subPath;
    }
}
