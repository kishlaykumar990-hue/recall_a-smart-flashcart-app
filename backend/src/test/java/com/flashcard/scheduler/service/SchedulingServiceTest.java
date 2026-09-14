package com.flashcard.scheduler.service;

import com.flashcard.scheduler.dto.review.ReviewRequest;
import com.flashcard.scheduler.dto.review.ReviewResponse;
import com.flashcard.scheduler.entity.Card;
import com.flashcard.scheduler.entity.Deck;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.exception.ResourceNotFoundException;
import com.flashcard.scheduler.repository.CardRepository;
import com.flashcard.scheduler.repository.ReviewLogRepository;
import com.flashcard.scheduler.service.impl.SchedulingService;
import com.flashcard.scheduler.service.impl.StreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private ReviewLogRepository reviewLogRepository;
    @Mock private StreakService streakService;

    private SchedulingService schedulingService;

    private User owner;
    private Card card;

    @BeforeEach
    void setUp() {
        schedulingService = new SchedulingService(cardRepository, reviewLogRepository, streakService);

        owner = User.builder().id(UUID.randomUUID()).email("learner@example.com").build();
        Deck deck = Deck.builder().id(UUID.randomUUID()).owner(owner).name("Test Deck").build();
        card = Card.builder()
                .id(UUID.randomUUID())
                .deck(deck)
                .front("Q")
                .back("A")
                .easeFactor(2.5)
                .repetitions(0)
                .intervalDays(0)
                .dueDate(LocalDate.now())
                .totalReviews(0)
                .lapses(0)
                .build();
    }

    @Test
    void submitReview_appliesSm2AndPersistsUpdatedCard() {
        when(cardRepository.findByIdAndDeckOwner(card.getId(), owner)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest request = new ReviewRequest(card.getId(), 5, 1200L);
        ReviewResponse response = schedulingService.submitReview(owner, request);

        assertEquals(1, response.newRepetitions());
        assertEquals(1, response.newIntervalDays());
        assertEquals(2.6, response.newEaseFactor(), 0.0001);
        assertEquals(LocalDate.now().plusDays(1), response.newDueDate());

        verify(cardRepository).save(card);
        verify(reviewLogRepository).save(any());
        verify(streakService).recordStudyActivity(owner);

        assertEquals(1, card.getTotalReviews());
        assertEquals(0, card.getLapses());
    }

    @Test
    void submitReview_onFailure_incrementsLapseCounter() {
        card.setRepetitions(4);
        card.setIntervalDays(20);
        card.setEaseFactor(2.6);
        when(cardRepository.findByIdAndDeckOwner(card.getId(), owner)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest request = new ReviewRequest(card.getId(), 1, null);
        ReviewResponse response = schedulingService.submitReview(owner, request);

        assertEquals(0, response.newRepetitions());
        assertEquals(1, response.newIntervalDays());
        assertEquals(1, card.getLapses());
    }

    @Test
    void submitReview_cardNotOwnedByUser_throwsNotFound() {
        when(cardRepository.findByIdAndDeckOwner(any(), eq(owner))).thenReturn(Optional.empty());
        ReviewRequest request = new ReviewRequest(UUID.randomUUID(), 4, null);

        assertThrows(ResourceNotFoundException.class, () -> schedulingService.submitReview(owner, request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void submitReview_logsCorrectBeforeAfterSnapshot() {
        when(cardRepository.findByIdAndDeckOwner(card.getId(), owner)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        schedulingService.submitReview(owner, new ReviewRequest(card.getId(), 4, 500L));

        ArgumentCaptor<com.flashcard.scheduler.entity.ReviewLog> captor =
                ArgumentCaptor.forClass(com.flashcard.scheduler.entity.ReviewLog.class);
        verify(reviewLogRepository).save(captor.capture());

        var logEntry = captor.getValue();
        assertEquals(2.5, logEntry.getEaseFactorBefore(), 0.0001);
        assertEquals(0, logEntry.getRepetitionsBefore());
        assertEquals(1, logEntry.getRepetitionsAfter());
        assertEquals(500L, logEntry.getResponseTimeMs());
    }
}
