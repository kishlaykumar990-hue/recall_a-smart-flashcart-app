package com.flashcard.scheduler.dto.review;

import java.time.LocalDate;
import java.util.UUID;

public record ReviewResponse(
        UUID cardId,
        int quality,
        double newEaseFactor,
        int newRepetitions,
        int newIntervalDays,
        LocalDate newDueDate
) {}
