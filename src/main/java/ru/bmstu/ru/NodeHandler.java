package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;
import ru.bmstu.ru.Messages.ServersListMessage;

import java.util.stream.Collectors;

public class NodeHandler implements Watcher{
    public static final String LOCALHOST = "localhost";
    private static final int PORT = 8888;

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
            zooKeeper = new ZooKeeper(LOCALHOST + ":" + PORT, 2000, this);
            zooKeeper.create(path + "/" + name, (host + ":" + port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            watchChildren();
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

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
