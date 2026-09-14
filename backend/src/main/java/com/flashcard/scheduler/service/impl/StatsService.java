package com.flashcard.scheduler.service.impl;

import com.flashcard.scheduler.dto.stats.DailyReviewCount;
import com.flashcard.scheduler.dto.stats.DashboardStatsResponse;
import com.flashcard.scheduler.entity.ReviewLog;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.repository.CardRepository;
import com.flashcard.scheduler.repository.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates review history into dashboard-friendly analytics: retention rate,
 * due-card counts, streaks, and a 14-day review activity series for charting.
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final CardRepository cardRepository;
    private final ReviewLogRepository reviewLogRepository;

    public DashboardStatsResponse getDashboardStats(User user) {
        long totalCards = cardRepository.findDueCardsForUser(user, LocalDate.now().plusYears(50)).size();
        long dueToday = cardRepository.countDueForUser(user, LocalDate.now());

        Instant since = LocalDate.now().minusDays(14).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<ReviewLog> recent = reviewLogRepository.findRecentByUser(user, since);

        Map<LocalDate, List<ReviewLog>> byDay = recent.stream()
                .collect(Collectors.groupingBy(r -> r.getReviewedAt().atZone(ZoneId.systemDefault()).toLocalDate()));

        List<DailyReviewCount> last14 = byDay.entrySet().stream()
                .map(e -> new DailyReviewCount(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().filter(r -> r.getQuality() >= 3).count()))
                .sorted(Comparator.comparing(DailyReviewCount::date))
                .toList();

        double retention = recent.isEmpty() ? 0.0
                : (double) recent.stream().filter(r -> r.getQuality() >= 3).count() / recent.size() * 100.0;

        return new DashboardStatsResponse(totalCards, dueToday, user.getCurrentStreak(),
                user.getLongestStreak(), Math.round(retention * 10.0) / 10.0, last14);
    }
}
