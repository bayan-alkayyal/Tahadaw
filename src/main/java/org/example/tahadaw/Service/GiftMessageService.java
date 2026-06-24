package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftMessageCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftMessageFromPlanDTOIn;
import org.example.tahadaw.DTO.IN.GiftMessageGenerateDTOIn;
import org.example.tahadaw.DTO.IN.GiftMessageUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftMessageDTOOut;
import org.example.tahadaw.Model.GiftMessage;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.GiftMessageRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftMessageService {

    private final GiftMessageRepository giftMessageRepository;
    private final UserRepository userRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final AiService aiService;

    /**
     * AI writes a message from the context the user supplies (no gift plan involved).
     */
    @Transactional
    public GiftMessageDTOOut generate(Long userId, GiftMessageGenerateDTOIn request) {
        User user = requireUser(userId);

        String tone = request.getTone() != null && !request.getTone().isBlank()
                ? request.getTone().trim()
                : "warm";
        String language = request.getLanguage() != null && !request.getLanguage().isBlank()
                ? request.getLanguage().trim()
                : "en";

        String prompt = buildPrompt(request, tone, language);
        JsonNode aiResponse = AiJsonParser.parseObject(aiService.ask(prompt));
        String messageText = AiJsonParser.requireText(aiResponse, "messageText");

        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setUser(user);
        giftMessage.setTone(tone);
        giftMessage.setLanguage(language);
        giftMessage.setMessageText(messageText);
        giftMessage.setCreatedAt(LocalDateTime.now());

        return toDto(giftMessageRepository.save(giftMessage));
    }

    /**
     * AI writes a message using the details pulled from an existing gift plan
     * (recipient, occasion and the chosen gift). Mirrors {@link #generate} but the
     * context is derived from the plan instead of the request body.
     */
    @Transactional
    public GiftMessageDTOOut generateFromPlan(Long userId, Long giftPlanId, GiftMessageFromPlanDTOIn request) {
        User user = requireUser(userId);

        GiftPlan plan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (plan.getUser() == null || !plan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }

        Recipient recipient = plan.getRecipient();
        if (recipient == null || recipient.getName() == null || recipient.getName().isBlank()) {
            throw new ApiException("This gift plan has no recipient to address the message to.");
        }
        if (plan.getOccasionType() == null || plan.getOccasionType().isBlank()) {
            throw new ApiException("This gift plan has no occasion to base the message on.");
        }

        String tone = request != null && request.getTone() != null && !request.getTone().isBlank()
                ? request.getTone().trim()
                : "warm";
        String language;
        if (request != null && request.getLanguage() != null && !request.getLanguage().isBlank()) {
            language = request.getLanguage().trim();
        } else if (plan.getLanguage() != null && !plan.getLanguage().isBlank()) {
            language = plan.getLanguage().trim();
        } else {
            language = "en";
        }

        GiftMessageGenerateDTOIn context = new GiftMessageGenerateDTOIn();
        context.setRecipientName(recipient.getName());
        context.setRelationship(recipient.getRelationship());
        context.setOccasion(plan.getOccasionType());
        context.setGiftName(resolveGiftName(plan));
        context.setTone(tone);
        context.setLanguage(language);
        context.setDialect(request != null ? request.getDialect() : null);

        String prompt = buildPrompt(context, tone, language);
        JsonNode aiResponse = AiJsonParser.parseObject(aiService.ask(prompt));
        String messageText = AiJsonParser.requireText(aiResponse, "messageText");

        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setUser(user);
        giftMessage.setGiftPlan(plan);
        giftMessage.setTone(tone);
        giftMessage.setLanguage(language);
        giftMessage.setMessageText(messageText);
        giftMessage.setCreatedAt(LocalDateTime.now());

        return toDto(giftMessageRepository.save(giftMessage));
    }

    /**
     * User writes their own message (no AI, no gift plan). The body carries only the text.
     */
    @Transactional
    public GiftMessageDTOOut createManual(Long userId, GiftMessageCreateDTOIn request) {
        User user = requireUser(userId);

        String text = request.getMessageText() == null ? "" : request.getMessageText().trim();
        if (text.isBlank()) {
            throw new ApiException("Message text is required.");
        }

        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setUser(user);
        giftMessage.setTone("custom");
        giftMessage.setLanguage("en");
        giftMessage.setMessageText(text);
        giftMessage.setCreatedAt(LocalDateTime.now());

        return toDto(giftMessageRepository.save(giftMessage));
    }

    /**
     * Edit an existing message (AI or manual). Blocked once the message is locked into a gift card.
     */
    @Transactional
    public GiftMessageDTOOut update(Long userId, Long messageId, GiftMessageUpdateDTOIn request) {
        GiftMessage giftMessage = requireOwnedMessage(userId, messageId);
        if (giftMessage.getGiftCard() != null) {
            throw new ApiException("This message is already used by a gift card and can't be edited. "
                    + "Create a new message instead.");
        }

        if (request.getMessageText() != null) {
            if (request.getMessageText().isBlank()) {
                throw new ApiException("Message text cannot be blank.");
            }
            giftMessage.setMessageText(request.getMessageText().trim());
        }
        if (request.getTone() != null && !request.getTone().isBlank()) {
            giftMessage.setTone(request.getTone().trim());
        }
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            giftMessage.setLanguage(request.getLanguage().trim());
        }

        return toDto(giftMessageRepository.save(giftMessage));
    }

    public List<GiftMessageDTOOut> listMine(Long userId) {
        requireUser(userId);
        return giftMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public GiftMessageDTOOut getOne(Long userId, Long messageId) {
        return toDto(requireOwnedMessage(userId, messageId));
    }

    /**
     * Picks the best gift name available on the plan: the selected product first,
     * then the selected gift idea. Returns null when nothing has been chosen yet.
     */
    private String resolveGiftName(GiftPlan plan) {
        if (plan.getSelectedProduct() != null
                && plan.getSelectedProduct().getProductName() != null
                && !plan.getSelectedProduct().getProductName().isBlank()) {
            return plan.getSelectedProduct().getProductName().trim();
        }
        if (plan.getSelectedGiftIdea() != null
                && plan.getSelectedGiftIdea().getProductName() != null
                && !plan.getSelectedGiftIdea().getProductName().isBlank()) {
            return plan.getSelectedGiftIdea().getProductName().trim();
        }
        return null;
    }

    private User requireUser(Long userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
    }

    private GiftMessage requireOwnedMessage(Long userId, Long messageId) {
        GiftMessage giftMessage = giftMessageRepository.findGiftMessageById(messageId)
                .orElseThrow(() -> new ApiException("Gift message not found."));
        if (giftMessage.getUser() == null || !giftMessage.getUser().getId().equals(userId)) {
            throw new ApiException("Gift message not found.");
        }
        return giftMessage;
    }

    private String buildPrompt(GiftMessageGenerateDTOIn request, String tone, String language) {
        StringBuilder context = new StringBuilder();
        context.append("Recipient name: ").append(request.getRecipientName()).append('\n');
        if (request.getRelationship() != null && !request.getRelationship().isBlank()) {
            context.append("Relationship: ").append(request.getRelationship().trim()).append('\n');
        }
        context.append("Occasion / reason: ").append(request.getOccasion()).append('\n');
        if (request.getGiftName() != null && !request.getGiftName().isBlank()) {
            context.append("Gift: ").append(request.getGiftName().trim()).append('\n');
        }

        String dialect = request.getDialect();
        String dialectLine = dialect != null && !dialect.isBlank()
                ? "- Use " + dialect.trim() + " dialect.\n"
                : "";

        return """
                Write a personal gift message for the context below.
                Return JSON only in this exact shape:
                {"messageText": "the full gift message as one string"}

                Context:
                %s
                Requirements:
                - Tone: %s
                - Language code: %s
                %s- 3 to 5 sentences, natural and heartfelt
                - Address the recipient by name when appropriate
                - Do not mention that AI wrote the message
                """.formatted(context, tone, language, dialectLine);
    }

    private GiftMessageDTOOut toDto(GiftMessage giftMessage) {
        return new GiftMessageDTOOut(
                giftMessage.getId(),
                giftMessage.getTone(),
                giftMessage.getLanguage(),
                giftMessage.getMessageText()
        );
    }
}
