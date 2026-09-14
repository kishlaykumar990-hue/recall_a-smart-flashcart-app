package com.flashcard.scheduler.dto.deck;

import java.time.Instant;
import java.util.UUID;

public record DeckResponse(
        UUID id,
        String name,
        String description,
        String subject,
        boolean archived,
        int totalCards,
        int dueCards,
        Instant createdAt,
        Instant updatedAt
) {}
