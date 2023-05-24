package com.github.yi.midjourney;

import java.util.LinkedList;
import java.util.Queue;

public class UserQueue {
    private static final Queue<String> queue = new LinkedList<>();

    public static void main(String[] args) {
        addUser("Alice");
        addUser("Bob");
        addUser("Charlie");

        System.out.println("Current queue: " + queue);

        int position = getPosition("Bob");
        System.out.println("Bob is at position " + position);

        processUser(); // Process the first user
        System.out.println("Current queue: " + queue);

        position = getPosition("Charlie");
        System.out.println("Charlie is at position " + position);
    }

    public static void addUser(String user) {
        queue.add(user);
    }

    public static int getPosition(String user) {
        int position = 0;
        for (String u : queue) {
            position++;
            if (u.equals(user)) {
                return position;
            }
        }
        return -1; // User not found in queue
    }

    public static void processUser() {
        if (!queue.isEmpty()) {
            String user = queue.poll();
            System.out.println("Processing user: " + user);
        }
    }
}
