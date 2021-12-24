package ru.bmstu.ru;

public class AnonymizeApp {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <Host> <Port>");
            System.exit(1);
        }
    }
}
