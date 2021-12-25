package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;
import ru.bmstu.ru.Messages.ServersListMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NodeHandler implements Watcher{

    private ZooKeeper zooKeeper;
    private ActorRef storage;
    private String host, path;
    private int port;

    public NodeHandler(String host, int port, ActorRef storage, String path) {
        this.path = path;
        this.host = host;
        this.port = port;
        this.storage = storage;
    }

    public void start(String name, String host, String port){
        try {
            zooKeeper = new ZooKeeper(name, 2000, this);
            zooKeeper.create(path + "/" + host + ":"  + port, (host + ":" + port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            watchChildren(null);
        } catch (InterruptedException | KeeperException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void watchChildren(WatchedEvent event) {
        try{
            storage.tell(new ServersListMessage(new ArrayList<>(zooKeeper.getChildren("/" + path, this::watchChildren))), ActorRef.noSender());
        } catch (InterruptedException| KeeperException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        watchChildren(null);
    }
}
