package com.api;

import com.server.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseManager {
    private static final Map<String, CompletableFuture<Response>> pending = new ConcurrentHashMap<>();

    public static void register(String id, CompletableFuture<Response> future) {
        pending.put(id, future);
    }

    public static void complete(String id, Response response) {
        CompletableFuture<Response> future = pending.remove(id);
        if (future != null) {
            future.complete(response); // Wake up waiting code
//            System.out.println("Reached in the completion queue");
        }
    }
}
