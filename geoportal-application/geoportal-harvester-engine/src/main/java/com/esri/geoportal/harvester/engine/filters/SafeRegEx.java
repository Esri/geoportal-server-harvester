package com.esri.geoportal.harvester.engine.filters;

import java.util.concurrent.*;
import java.util.regex.Pattern;

public class SafeRegEx {
    private static final int MAX_PATTERN_LENGTH = 500;  // 🚧 guard input regex size
    private static final long TIMEOUT_MS = 10000;         // compilation budget

    private static final ExecutorService pool =
        Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "pattern-compiler");
            t.setDaemon(true);
            return t;
        });

    /**
     * Safely compile an untrusted regex with input size check + timeout.
     */
    public static Pattern compile(String regex)
            throws TimeoutException, ExecutionException, InterruptedException {

        if (regex == null) {
            throw new IllegalArgumentException("Regex cannot be null");
        }

        // 🚧 Reject oversized regex strings immediately
        if (regex.length() > MAX_PATTERN_LENGTH) {
            throw new IllegalArgumentException("Regex too long: " + regex.length());
        }

        Future<Pattern> f = pool.submit(() -> Pattern.compile(regex));

        try {
            return f.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            f.cancel(true);
            throw te;
        }
    }

    public static void shutdown() {
        pool.shutdownNow();
    }

    
}
