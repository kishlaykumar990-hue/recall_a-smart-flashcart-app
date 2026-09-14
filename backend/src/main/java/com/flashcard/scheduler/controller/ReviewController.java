package com.flashcard.scheduler.controller;

import com.flashcard.scheduler.dto.review.ReviewRequest;
import com.flashcard.scheduler.dto.review.ReviewResponse;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.service.impl.SchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final SchedulingService schedulingService;

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(schedulingService.submitReview(user, request));
    }
}
