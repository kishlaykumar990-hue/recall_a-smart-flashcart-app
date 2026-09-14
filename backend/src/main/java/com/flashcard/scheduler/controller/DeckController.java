package com.flashcard.scheduler.controller;

import com.flashcard.scheduler.dto.deck.DeckRequest;
import com.flashcard.scheduler.dto.deck.DeckResponse;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.service.impl.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @PostMapping
    public ResponseEntity<DeckResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody DeckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(user, request));
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deckService.listDecks(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(deckService.getDeck(user, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> update(@AuthenticationPrincipal User user, @PathVariable UUID id,
                                                @Valid @RequestBody DeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(user, id, request));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deckService.archiveDeck(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deckService.deleteDeck(user, id);
        return ResponseEntity.noContent().build();
    }
}
