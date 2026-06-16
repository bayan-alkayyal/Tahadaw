package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftMessageGenerateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftMessageDTOOut;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftMessage;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftMessageRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftMessageService {

    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final GiftMessageRepository giftMessageRepository;
    private final AiService aiService;

    @Transactional
    public GiftMessageDTOOut generate(Long userId, Long giftPlanId, GiftMessageGenerateDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);
        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select one AI gift idea before generating a message."));

        String tone = resolveTone(request, giftPlan);
        String language = resolveLanguage(request, giftPlan);
        String dialect = request.getDialect();

        String prompt = buildPrompt(giftPlan, selectedIdea, tone, language, dialect);
        JsonNode aiResponse = AiJsonParser.parseObject(aiService.ask(prompt));
        String messageText = AiJsonParser.requireText(aiResponse, "messageText");

        GiftMessage giftMessage = new GiftMessage();
        giftMessage.setGiftPlan(giftPlan);
        giftMessage.setTone(tone);
        giftMessage.setLanguage(language);
        giftMessage.setMessageText(messageText);
        giftMessage.setCreatedAt(LocalDateTime.now());

        return toDto(giftMessageRepository.save(giftMessage));
    }

    public List<GiftMessageDTOOut> listByGiftPlan(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        return giftMessageRepository.findByGiftPlan_IdOrderByCreatedAtDesc(giftPlanId).stream()
                .map(this::toDto)
                .toList();
    }

    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        return giftPlan;
    }

    private String resolveTone(GiftMessageGenerateDTOIn request, GiftPlan giftPlan) {
        if (request.getTone() != null && !request.getTone().isBlank()) {
            return request.getTone().trim();
        }
        return "warm";
    }

    private String resolveLanguage(GiftMessageGenerateDTOIn request, GiftPlan giftPlan) {
        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            return request.getLanguage().trim();
        }
        if (giftPlan.getLanguage() != null && !giftPlan.getLanguage().isBlank()) {
            return giftPlan.getLanguage().trim();
        }
        return "en";
    }

    private String buildPrompt(GiftPlan giftPlan,
                               GiftIdeaRecommendation selectedIdea,
                               String tone,
                               String language,
                               String dialect) {
        Recipient recipient = giftPlan.getRecipient();
        SelectedProduct selectedProduct = selectedIdea.getSelectedProduct();

        StringBuilder context = new StringBuilder();
        context.append("Recipient name: ").append(recipient.getName()).append('\n');
        if (recipient.getRelationship() != null) {
            context.append("Relationship: ").append(recipient.getRelationship()).append('\n');
        }
        if (recipient.getAge() != null) {
            context.append("Age: ").append(recipient.getAge()).append('\n');
        }
        if (recipient.getInterests() != null) {
            context.append("Interests: ").append(recipient.getInterests()).append('\n');
        }
        if (recipient.getPersonalityStyle() != null) {
            context.append("Personality: ").append(recipient.getPersonalityStyle()).append('\n');
        }
        context.append("Occasion: ").append(giftPlan.getOccasionType()).append('\n');
        if (giftPlan.getOccasionDate() != null) {
            context.append("Occasion date: ").append(giftPlan.getOccasionDate()).append('\n');
        }
        context.append("Selected gift idea: ").append(selectedIdea.getTitle()).append('\n');
        if (selectedIdea.getReason() != null) {
            context.append("Why this gift: ").append(selectedIdea.getReason()).append('\n');
        }
        if (selectedProduct != null) {
            context.append("Selected product: ").append(selectedProduct.getTitle()).append('\n');
        }

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
                giftMessage.getGiftPlan().getId(),
                giftMessage.getTone(),
                giftMessage.getLanguage(),
                giftMessage.getMessageText(),
                giftMessage.getCreatedAt()
        );
    }
}
