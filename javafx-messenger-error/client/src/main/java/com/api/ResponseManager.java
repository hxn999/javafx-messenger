package com.api;

import com.server.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ResponseManager {
    private static final Map<String, CompletableFuture<Response>> pending = new ConcurrentHashMap<>();
    private static final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();
    private static final long TIMEOUT_SECONDS = 30; // 30-second timeout

    /**
     * Register a future to wait for a response with the given request ID
     */
    public static void register(String requestId, CompletableFuture<Response> future) {
        if (requestId == null || future == null) {
            System.err.println("❌ Cannot register null request ID or future");
            return;
        }

        pending.put(requestId, future);
        requestTimestamps.put(requestId, System.currentTimeMillis());

        System.out.println("📝 Registered request: " + requestId + " (Total pending: " + pending.size() + ")");

        // Set up timeout handling
        future.orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .whenComplete((response, throwable) -> {
                    // Clean up when completed or timed out
                    pending.remove(requestId);
                    requestTimestamps.remove(requestId);

                    if (throwable != null) {
                        System.err.println("⏰ Request timed out or failed: " + requestId);
                    }
                });
    }

    /**
     * Complete a registered future with the response
     */
    public static void complete(String requestId, Response response) {
        if (requestId == null) {
            System.err.println("❌ Cannot complete request with null ID");
            return;
        }

        CompletableFuture<Response> future = pending.remove(requestId);
        requestTimestamps.remove(requestId);

        if (future != null) {
            if (response != null) {
                System.out.println("✅ Completing request: " + requestId +
                        " with status: " + response.getStatusCode());
                future.complete(response);
            } else {
                System.err.println("❌ Received null response for request: " + requestId);
                future.completeExceptionally(new RuntimeException("Received null response"));
            }
        } else {
            System.err.println("⚠️ No pending request found for ID: " + requestId);
            System.err.println("   Current pending requests: " + pending.keySet());
        }
    }

    /**
     * Check if a request is still pending
     */
    public static boolean isPending(String requestId) {
        return pending.containsKey(requestId);
    }

    /**
     * Get the number of pending requests
     */
    public static int getPendingCount() {
        return pending.size();
    }

    /**
     * Cancel a pending request
     */
    public static boolean cancel(String requestId) {
        CompletableFuture<Response> future = pending.remove(requestId);
        requestTimestamps.remove(requestId);

        if (future != null) {
            boolean cancelled = future.cancel(true);
            System.out.println("🚫 Cancelled request: " + requestId + " (success: " + cancelled + ")");
            return cancelled;
        }
        return false;
    }

    /**
     * Clean up old requests (called periodically to prevent memory leaks)
     */
    public static void cleanupOldRequests() {
        long now = System.currentTimeMillis();
        long timeoutMs = TIMEOUT_SECONDS * 1000;

        requestTimestamps.entrySet().removeIf(entry -> {
            String requestId = entry.getKey();
            long timestamp = entry.getValue();

            if (now - timestamp > timeoutMs) {
                CompletableFuture<Response> future = pending.remove(requestId);
                if (future != null) {
                    future.completeExceptionally(new RuntimeException("Request timed out"));
                }
                System.out.println("🧹 Cleaned up old request: " + requestId);
                return true;
            }
            return false;
        });
    }

    /**
     * Debug method to print current state
     */
    public static void debugPrintState() {
        System.out.println("=== ResponseManager State ===");
        System.out.println("Pending requests: " + pending.size());
        System.out.println("Request IDs: " + pending.keySet());
        System.out.println("============================");
    }

    /**
     * Clear all pending requests (use with caution)
     */
    public static void clearAll() {
        System.out.println("🧹 Clearing all pending requests: " + pending.size());
        pending.forEach((id, future) -> {
            future.completeExceptionally(new RuntimeException("Cleared by system"));
        });
        pending.clear();
        requestTimestamps.clear();
    }
}