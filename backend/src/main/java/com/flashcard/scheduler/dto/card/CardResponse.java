package com.flashcard.scheduler.dto.card;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID deckId,
        String front,
        String back,
        String hint,
        Set<String> tags,
        double easeFactor,
        int repetitions,
        int intervalDays,
        LocalDate dueDate,
        boolean due,
        int totalReviews,
        int lapses,
        Instant lastReviewedAt
) {}
