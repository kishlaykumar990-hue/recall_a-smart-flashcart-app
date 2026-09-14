package com.flashcard.scheduler.dto.stats;

import java.util.List;

public record DashboardStatsResponse(
        long totalCards,
        long dueToday,
        int currentStreak,
        int longestStreak,
        double retentionRate,
        List<DailyReviewCount> last14Days
) {}
