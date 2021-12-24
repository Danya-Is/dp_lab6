package ru.bmstu.ru;

import java.io.IOException;

public class AnonymizeApp {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: <Host> <Port>");
            System.exit(1);
        }

        HttpServer server = new HttpServer();
        server.run();
    }
}
