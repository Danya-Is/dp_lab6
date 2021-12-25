package ru.bmstu.ru;

import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;

public class ZookeeperServer {

    private ActorRef storage;
    private String host, path;
    private int port;

    public ZookeeperServer(ActorRef storage, String path, String host, int port) {
        this.storage = storage;
        this.host = host;
        this.path = path;
        this.port = port;
    }



}
