package com.flashcard.scheduler.service.impl;

import com.flashcard.scheduler.dto.card.CardRequest;
import com.flashcard.scheduler.dto.card.CardResponse;
import com.flashcard.scheduler.entity.Card;
import com.flashcard.scheduler.entity.Deck;
import com.flashcard.scheduler.entity.Tag;
import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.exception.ResourceNotFoundException;
import com.flashcard.scheduler.repository.CardRepository;
import com.flashcard.scheduler.repository.DeckRepository;
import com.flashcard.scheduler.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final TagRepository tagRepository;

    @Transactional
    public CardResponse createCard(User owner, CardRequest request) {
        Deck deck = deckRepository.findByIdAndOwner(request.deckId(), owner)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + request.deckId()));

        Card card = Card.builder()
                .deck(deck)
                .front(request.front())
                .back(request.back())
                .hint(request.hint())
                .tags(resolveTags(owner, request.tags()))
                .build();

        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> listCardsInDeck(User owner, UUID deckId) {
        Deck deck = deckRepository.findByIdAndOwner(deckId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        return cardRepository.findByDeck(deck).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getDueCards(User owner) {
        return cardRepository.findDueCardsForUser(owner, LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> search(User owner, String term, Pageable pageable) {
        return cardRepository.searchByOwnerAndTerm(owner, term, pageable).map(this::toResponse);
    }

    @Transactional
    public CardResponse updateCard(User owner, UUID cardId, CardRequest request) {
        Card card = findOwnedCard(owner, cardId);
        card.setFront(request.front());
        card.setBack(request.back());
        card.setHint(request.hint());
        card.setTags(resolveTags(owner, request.tags()));
        return toResponse(cardRepository.save(card));
    }

    @Transactional
    public void deleteCard(User owner, UUID cardId) {
        Card card = findOwnedCard(owner, cardId);
        cardRepository.delete(card);
    }

    private Card findOwnedCard(User owner, UUID cardId) {
        return cardRepository.findByIdAndDeckOwner(cardId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
    }

    private Set<Tag> resolveTags(User owner, Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return new HashSet<>();
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            String normalized = name.trim().toLowerCase();
            if (normalized.isEmpty()) continue;
            Tag tag = tagRepository.findByOwnerAndName(owner, normalized)
                    .orElseGet(() -> tagRepository.save(Tag.builder().owner(owner).name(normalized).build()));
            tags.add(tag);
        }
        return tags;
    }

    private CardResponse toResponse(Card card) {
        Set<String> tagNames = card.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        return new CardResponse(card.getId(), card.getDeck().getId(), card.getFront(), card.getBack(),
                card.getHint(), tagNames, card.getEaseFactor(), card.getRepetitions(), card.getIntervalDays(),
                card.getDueDate(), card.isDue(), card.getTotalReviews(), card.getLapses(), card.getLastReviewedAt());
    }
}
