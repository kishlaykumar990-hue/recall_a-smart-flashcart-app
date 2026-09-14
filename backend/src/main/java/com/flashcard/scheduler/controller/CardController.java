package com.flashcard.scheduler.controller;

import com.flashcard.scheduler.dto.card.CardRequest;
import com.flashcard.scheduler.dto.card.CardResponse;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.service.impl.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(user, request));
    }

    @GetMapping("/deck/{deckId}")
    public ResponseEntity<List<CardResponse>> listByDeck(@AuthenticationPrincipal User user, @PathVariable UUID deckId) {
        return ResponseEntity.ok(cardService.listCardsInDeck(user, deckId));
    }

    @GetMapping("/due")
    public ResponseEntity<List<CardResponse>> due(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.getDueCards(user));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CardResponse>> search(@AuthenticationPrincipal User user,
                                                       @RequestParam String term, Pageable pageable) {
        return ResponseEntity.ok(cardService.search(user, term, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> update(@AuthenticationPrincipal User user, @PathVariable UUID id,
                                                @Valid @RequestBody CardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        cardService.deleteCard(user, id);
        return ResponseEntity.noContent().build();
    }
}
