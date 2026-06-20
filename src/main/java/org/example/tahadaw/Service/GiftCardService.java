package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftCardCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftCardUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftCardDTOOut;
import org.example.tahadaw.Model.GiftCard;
import org.example.tahadaw.Model.GiftMessage;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.GiftCardRepository;
import org.example.tahadaw.Repository.GiftMessageRepository;
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
    private final GiftMessageRepository giftMessageRepository;
    private final PremiumService premiumService;
    private final QrCodeService qrCodeService;
    private final GiftCardImageService giftCardImageService;
    private final EmailService emailService;

    private static final String DEFAULT_CARD_MESSAGE =
            "أطيب التهاني والأمنيات، هذه هدية خاصة لك بمناسبة يومك المميّز.";

    @Transactional
    public GiftCardDTOOut create(Long userId, GiftCardCreateDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
        premiumService.requirePremium(user);

        GiftCard giftCard = new GiftCard();
        giftCard.setUser(user);
        // Names come straight from the request body — the card is not tied to a gift plan.
        giftCard.setRecipientName(request.getRecipientName());
        giftCard.setSenderName(request.getSenderName());
        giftCard.setCardSize(request.getCardSize());
        giftCard.setLinkType(request.getLinkType());
        giftCard.setLinkUrl(request.getLinkUrl());
        giftCard.setSentToEmail(request.getSentToEmail());
        giftCard.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        giftCard.setCreatedAt(LocalDateTime.now());

        if (request.getGiftMessageId() != null) {
            GiftMessage giftMessage = giftMessageRepository.findGiftMessageById(request.getGiftMessageId())
                    .orElseThrow(() -> new ApiException("Gift message not found."));
            if (giftMessage.getUser() == null
                    || !giftMessage.getUser().getId().equals(userId)) {
                throw new ApiException("Gift message not found.");
            }
            if (giftCardRepository.existsByGiftMessage_Id(giftMessage.getId())) {
                throw new ApiException("Gift message is already used by another gift card.");
            }
            giftCard.setGiftMessage(giftMessage);
        }

        renderImages(giftCard);

        // Auto-deliver the rendered card to the owner so they can download it straight from their inbox.
        // Email is optional in local/dev: if it fails, the card is still created and downloadable via /image.
        String target = firstNonBlank(giftCard.getSentToEmail(), user.getEmail());
        if (target != null) {
            try {
                emailService.sendGiftCardEmail(giftCard, user, target);
                giftCard.setSentToEmail(target);
                giftCard.setStatus("SENT");
            } catch (ApiException ignored) {
                // keep the card as DRAFT; it can be re-sent via the send-email endpoint.
            }
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

        boolean imageRelevantChange = false;

        if (request.getRecipientName() != null) {
            giftCard.setRecipientName(request.getRecipientName());
            imageRelevantChange = true;
        }
        if (request.getSenderName() != null) {
            giftCard.setSenderName(request.getSenderName());
            imageRelevantChange = true;
        }
        if (request.getCardSize() != null) {
            giftCard.setCardSize(request.getCardSize());
            imageRelevantChange = true;
        }
        if (request.getLinkType() != null) {
            giftCard.setLinkType(request.getLinkType());
        }
        if (request.getLinkUrl() != null) {
            giftCard.setLinkUrl(request.getLinkUrl());
            imageRelevantChange = true;
        }
        if (request.getSentToEmail() != null) {
            giftCard.setSentToEmail(request.getSentToEmail());
        }
        if (request.getStatus() != null) {
            giftCard.setStatus(request.getStatus());
        }

        if (imageRelevantChange) {
            renderImages(giftCard);
        }

        return toDto(giftCardRepository.save(giftCard));
    }

    @Transactional
    public GiftCardDTOOut sendEmail(Long userId, Long giftCardId, String email) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);

        String target = firstNonBlank(email, giftCard.getSentToEmail(), giftCard.getUser().getEmail());
        if (target == null) {
            throw new ApiException("No destination email provided.");
        }

        emailService.sendGiftCardEmail(giftCard, giftCard.getUser(), target);

        giftCard.setSentToEmail(target);
        giftCard.setStatus("SENT");
        return toDto(giftCardRepository.save(giftCard));
    }

    @Transactional
    public GiftCardDTOOut regenerate(Long userId, Long giftCardId) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);
        renderImages(giftCard);
        return toDto(giftCardRepository.save(giftCard));
    }

    @Transactional
    public byte[] getCardImage(Long userId, Long giftCardId) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);
        if (giftCard.getGiftCardImage() == null || giftCard.getGiftCardImage().length == 0) {
            renderImages(giftCard);
            giftCardRepository.save(giftCard);
        }
        return giftCard.getGiftCardImage();
    }

    private void renderImages(GiftCard giftCard) {
        if (giftCard.getLinkUrl() != null && !giftCard.getLinkUrl().isBlank()) {
            giftCard.setQrCodeImage(qrCodeService.generateQrCodePng(giftCard.getLinkUrl()));
        } else {
            giftCard.setQrCodeImage(null);
        }

        String linkedMessage = giftCard.getGiftMessage() != null
                ? giftCard.getGiftMessage().getMessageText()
                : null;
        String messageText = (linkedMessage != null && !linkedMessage.isBlank())
                ? linkedMessage
                : DEFAULT_CARD_MESSAGE;

        giftCard.setGiftCardImage(giftCardImageService.renderCard(
                giftCard.getCardSize(),
                giftCard.getRecipientName(),
                giftCard.getSenderName(),
                messageText,
                giftCard.getQrCodeImage()
        ));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    @Transactional
    public void delete(Long userId, Long giftCardId) {
        GiftCard giftCard = requireOwnedGiftCard(userId, giftCardId);
        giftCardRepository.delete(giftCard);
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
