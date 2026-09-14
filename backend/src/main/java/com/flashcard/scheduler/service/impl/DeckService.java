package com.flashcard.scheduler.service.impl;

import com.flashcard.scheduler.dto.deck.DeckRequest;
import com.flashcard.scheduler.dto.deck.DeckResponse;
import com.flashcard.scheduler.entity.Deck;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.exception.ResourceNotFoundException;
import com.flashcard.scheduler.repository.CardRepository;
import com.flashcard.scheduler.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    @Transactional
    public DeckResponse createDeck(User owner, DeckRequest request) {
        Deck deck = Deck.builder()
                .owner(owner)
                .name(request.name())
                .description(request.description())
                .subject(request.subject())
                .build();
        return toResponse(deckRepository.save(deck));
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> listDecks(User owner) {
        return deckRepository.findByOwnerAndArchivedFalseOrderByUpdatedAtDesc(owner)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeckResponse getDeck(User owner, UUID deckId) {
        return toResponse(findOwnedDeck(owner, deckId));
    }

    @Transactional
    public DeckResponse updateDeck(User owner, UUID deckId, DeckRequest request) {
        Deck deck = findOwnedDeck(owner, deckId);
        deck.setName(request.name());
        deck.setDescription(request.description());
        deck.setSubject(request.subject());
        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    public void archiveDeck(User owner, UUID deckId) {
        Deck deck = findOwnedDeck(owner, deckId);
        deck.setArchived(true);
        deckRepository.save(deck);
    }

    @Transactional
    public void deleteDeck(User owner, UUID deckId) {
        Deck deck = findOwnedDeck(owner, deckId);
        deckRepository.delete(deck);
    }

    private Deck findOwnedDeck(User owner, UUID deckId) {
        return deckRepository.findByIdAndOwner(deckId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
    }

    private DeckResponse toResponse(Deck deck) {
        int total = cardRepository.findByDeck(deck).size();
        long due = cardRepository.findDueCardsForDeck(deck, LocalDate.now()).size();
        return new DeckResponse(deck.getId(), deck.getName(), deck.getDescription(), deck.getSubject(),
                deck.isArchived(), total, (int) due, deck.getCreatedAt(), deck.getUpdatedAt());
    }
}
