package com.flashcard.scheduler.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error payload returned to clients for every 4xx/5xx response.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {}
