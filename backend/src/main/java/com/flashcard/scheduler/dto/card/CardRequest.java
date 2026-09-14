package com.flashcard.scheduler.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record CardRequest(
        @NotNull UUID deckId,
        @NotBlank String front,
        @NotBlank String back,
        String hint,
        Set<String> tags
) {}
