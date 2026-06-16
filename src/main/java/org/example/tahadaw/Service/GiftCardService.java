package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftCardCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftCardUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftCardDTOOut;
import org.example.tahadaw.Model.GiftCard;
import org.example.tahadaw.Model.GiftMessage;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.GiftCardStatus;
import org.example.tahadaw.Repository.GiftCardRepository;
import org.example.tahadaw.Repository.GiftMessageRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final UserRepository userRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final GiftMessageRepository giftMessageRepository;

    @Transactional
    public GiftCardDTOOut create(Long userId, GiftCardCreateDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, request.getGiftPlanId());

        if (giftCardRepository.findByGiftPlan_Id(giftPlan.getId()).isPresent()) {
            throw new ApiException("A gift card already exists for this gift plan.");
        }

        GiftCard giftCard = new GiftCard();
        giftCard.setUser(user);
        giftCard.setGiftPlan(giftPlan);
        giftCard.setRecipientName(request.getRecipientName());
        giftCard.setSenderName(request.getSenderName());
        giftCard.setCardSize(request.getCardSize());
        giftCard.setLinkType(request.getLinkType());
        giftCard.setLinkUrl(request.getLinkUrl());
        giftCard.setSentToEmail(request.getSentToEmail());
        giftCard.setStatus(request.getStatus() != null ? request.getStatus() : GiftCardStatus.DRAFT);
        giftCard.setCreatedAt(LocalDateTime.now());

        if (request.getGiftMessageId() != null) {
            GiftMessage giftMessage = giftMessageRepository.findGiftMessageById(request.getGiftMessageId())
                    .orElseThrow(() -> new ApiException("Gift message not found."));
            if (!giftMessage.getGiftPlan().getId().equals(giftPlan.getId())) {
                throw new ApiException("Gift message must belong to the same gift plan.");
            }
            if (giftCardRepository.existsByGiftMessage_Id(giftMessage.getId())) {
                throw new ApiException("Gift message is already used by another gift card.");
            }
            giftCard.setGiftMessage(giftMessage);
        }

        return toDto(giftCardRepository.save(giftCard));
    }

    public List<GiftCardDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return giftCardRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public GiftCardDTOOut getOne(Long userId, Long giftCardId) {
        return toDto(requireOwnedGiftCard(userId, giftCardId));
    }

    @Transactional
    public GiftCardDTOOut update(Long userId, Long giftCardId, GiftCardUpdateDTOIn request) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);

        if (request.getRecipientName() != null) {
            giftCard.setRecipientName(request.getRecipientName());
        }
        if (request.getSenderName() != null) {
            giftCard.setSenderName(request.getSenderName());
        }
        if (request.getCardSize() != null) {
            giftCard.setCardSize(request.getCardSize());
        }
        if (request.getLinkType() != null) {
            giftCard.setLinkType(request.getLinkType());
        }
        if (request.getLinkUrl() != null) {
            giftCard.setLinkUrl(request.getLinkUrl());
        }
        if (request.getSentToEmail() != null) {
            giftCard.setSentToEmail(request.getSentToEmail());
        }
        if (request.getStatus() != null) {
            giftCard.setStatus(request.getStatus());
        }

        return toDto(giftCardRepository.save(giftCard));
    }

    @Transactional
    public void delete(Long userId, Long giftCardId) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);
        giftCardRepository.delete(giftCard);
    }

    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        if (!giftPlan.getRecipient().getUser().getId().equals(userId)) {
            throw new ApiException("Recipient must belong to the gift plan owner.");
        }
        return giftPlan;
    }

    private GiftCard requireOwnedGiftCard(Long userId, Long giftCardId) {
        GiftCard giftCard = giftCardRepository.findGiftCardById(giftCardId)
                .orElseThrow(() -> new ApiException("Gift card not found."));
        if (!giftCard.getUser().getId().equals(userId)) {
            throw new ApiException("Gift card not found.");
        }
        return giftCard;
    }

    private GiftCardDTOOut toDto(GiftCard giftCard) {
        return new GiftCardDTOOut(
                giftCard.getId(),
                giftCard.getUser().getId(),
                giftCard.getGiftPlan().getId(),
                giftCard.getGiftMessage() != null ? giftCard.getGiftMessage().getId() : null,
                giftCard.getRecipientName(),
                giftCard.getSenderName(),
                giftCard.getCardSize(),
                giftCard.getLinkType(),
                giftCard.getLinkUrl(),
                giftCard.getSentToEmail(),
                giftCard.getStatus(),
                giftCard.getCreatedAt()
        );
    }
}
