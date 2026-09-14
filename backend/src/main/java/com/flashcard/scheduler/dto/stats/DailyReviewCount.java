package com.flashcard.scheduler.dto.stats;

import java.time.LocalDate;

public record DailyReviewCount(
        LocalDate date,
        long reviewsCount,
        long correctCount
) {}
