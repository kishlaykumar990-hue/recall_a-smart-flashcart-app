package com.flashcard.scheduler.controller;

import com.flashcard.scheduler.dto.stats.DashboardStatsResponse;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.service.impl.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> dashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getDashboardStats(user));
    }
}
