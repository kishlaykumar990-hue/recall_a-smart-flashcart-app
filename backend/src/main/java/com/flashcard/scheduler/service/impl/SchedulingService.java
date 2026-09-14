package com.flashcard.scheduler.service.impl;

import com.flashcard.scheduler.dto.review.ReviewRequest;
import com.flashcard.scheduler.dto.review.ReviewResponse;
import com.flashcard.scheduler.entity.Card;
import com.flashcard.scheduler.entity.ReviewLog;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.exception.ResourceNotFoundException;
import com.flashcard.scheduler.repository.CardRepository;
import com.flashcard.scheduler.repository.ReviewLogRepository;
import com.flashcard.scheduler.service.Sm2Algorithm;
import com.flashcard.scheduler.service.Sm2Algorithm.SchedulingResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Bridges the pure {@link Sm2Algorithm} domain logic to the persistence layer: loads a card's
 * current scheduling state, applies one review via the algorithm, persists the updated state
 * on the Card, writes an immutable ReviewLog row, and updates the learner's study streak.
 *
 * Keeping this orchestration separate from Sm2Algorithm itself is a deliberate architectural
 * choice: the algorithm stays 100% pure and unit-testable without a database, while all
 * transactional/side-effecting concerns live here.
 */
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final CardRepository cardRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final StreakService streakService;

    @Transactional
    public ReviewResponse submitReview(User user, ReviewRequest request) {
        Card card = cardRepository.findByIdAndDeckOwner(request.cardId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.cardId()));

        double easeBefore = card.getEaseFactor();
        int repsBefore = card.getRepetitions();
        int intervalBefore = card.getIntervalDays();

        SchedulingResult result = Sm2Algorithm.schedule(
                request.quality(), easeBefore, repsBefore, intervalBefore);

        LocalDate newDueDate = LocalDate.now().plusDays(result.intervalDays());

        card.setEaseFactor(result.easeFactor());
        card.setRepetitions(result.repetitions());
        card.setIntervalDays(result.intervalDays());
        card.setDueDate(newDueDate);
        card.setLastReviewedAt(Instant.now());
        card.setTotalReviews(card.getTotalReviews() + 1);
        if (request.quality() < 3) {
            card.setLapses(card.getLapses() + 1);
        }
        cardRepository.save(card);

        ReviewLog logEntry = ReviewLog.builder()
                .card(card)
                .user(user)
                .quality(request.quality())
                .easeFactorBefore(easeBefore)
                .easeFactorAfter(result.easeFactor())
                .intervalBefore(intervalBefore)
                .intervalAfter(result.intervalDays())
                .repetitionsBefore(repsBefore)
                .repetitionsAfter(result.repetitions())
                .responseTimeMs(request.responseTimeMs())
                .build();
        reviewLogRepository.save(logEntry);

        streakService.recordStudyActivity(user);

        log.debug("Card {} reviewed with quality={}: EF {}->{}, reps {}->{}, interval {}->{}",
                card.getId(), request.quality(), easeBefore, result.easeFactor(),
                repsBefore, result.repetitions(), intervalBefore, result.intervalDays());

        return new ReviewResponse(card.getId(), request.quality(), result.easeFactor(),
                result.repetitions(), result.intervalDays(), newDueDate);
    }
}
