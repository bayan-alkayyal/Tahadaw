package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.DTO.OUT.GiftIdeaRecommendationDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.*;
import org.example.tahadaw.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GiftRecommendationService {

    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final GiftHistoryRepository giftHistoryRepository;
    private final AiService aiService;
    private final AiAnswerService aiAnswerService;

    public List<GiftIdeaRecommendationDTOOut> listRecommendations(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);
        return giftIdeaRecommendationRepository.findByGiftPlan(giftPlan).stream()
                .map(ResponseMapper::toGiftIdeaRecommendationDto)
                .toList();
    }

    @Transactional
    public void selectRecommendation(Long userId, Long recommendationId) {
        GiftIdeaRecommendation recommendation = giftIdeaRecommendationRepository.findGiftIdeaRecommendationById(recommendationId)
                .orElseThrow(() -> new ApiException("Gift idea recommendation not found."));

        GiftPlan giftPlan = recommendation.getGiftPlan();
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan is not yours");
        }
        Optional<GiftIdeaRecommendation> selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan);

        if (selectedIdea.isPresent()) {
            throw new ApiException("You already selected a gift idea.");
        }

        for (GiftIdeaRecommendation idea : giftIdeaRecommendationRepository.findByGiftPlan(giftPlan)) {
            idea.setIsSelected(idea.getId().equals(recommendationId));
            giftIdeaRecommendationRepository.save(idea);
        }
        giftPlan.setSelectedGiftIdea(recommendation);
        if (GiftPlanStatus.canSelectGiftIdea(giftPlan.getStatus())) {
            giftPlan.setStatus(GiftPlanStatus.GIFT_IDEA_SELECTED);
        }
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }

    @Transactional
    public void unselectRecommendation(Long userId, Long recommendationId) {
        GiftIdeaRecommendation recommendation = giftIdeaRecommendationRepository.findGiftIdeaRecommendationById(recommendationId)
                .orElseThrow(() -> new ApiException("Gift idea recommendation not found."));

        GiftPlan giftPlan = recommendation.getGiftPlan();
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan is not yours");
        }

        if (recommendation.getIsSelected() == null || !recommendation.getIsSelected()) {
            throw new ApiException("This gift idea is not selected.");
        }

        if (GiftPlanStatus.PRODUCT_SELECTED.equals(giftPlan.getStatus()) || giftPlan.getSelectedProduct() != null) {
            throw new ApiException("A product is already selected. Remove the selected product before changing the gift idea.");
        }

        recommendation.setIsSelected(false);
        giftIdeaRecommendationRepository.save(recommendation);

        giftPlan.setSelectedGiftIdea(null);
        giftPlan.setStatus(GiftPlanStatus.RECOMMENDATIONS_GENERATED);
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }

    @Transactional
    public List<GiftIdeaRecommendationDTOOut> generateGiftRecommendation(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (!GiftPlanStatus.AI_QUESTIONS_ANSWERED.equals(giftPlan.getStatus())) {
            throw new ApiException("You have to answer all AI questions before generating gift recommendations.");
        }

        List<GiftIdeaRecommendation> existing = giftIdeaRecommendationRepository.findByGiftPlan(giftPlan);
        if (!existing.isEmpty()) {
            return existing.stream().map(ResponseMapper::toGiftIdeaRecommendationDto).toList();
        }

        List<GiftIdeaRecommendation> recommendations = callAiForRecommendations(giftPlan);
        LocalDateTime now = LocalDateTime.now();

        for (GiftIdeaRecommendation giftRecommendation : recommendations) {
            giftRecommendation.setIsSelected(false);
            giftRecommendation.setGiftPlan(giftPlan);
            giftRecommendation.setCreatedAt(now);
            giftIdeaRecommendationRepository.save(giftRecommendation);
        }

        giftPlan.setStatus(GiftPlanStatus.RECOMMENDATIONS_GENERATED);
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return recommendations.stream().map(ResponseMapper::toGiftIdeaRecommendationDto).toList();
    }

    @Transactional
    public List<GiftIdeaRecommendationDTOOut> regenerateGiftRecommendation(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (giftPlan.getSelectedProduct() != null) {
            throw new ApiException("Remove the selected product before regenerating gift recommendations.");
        }

        List<GiftIdeaRecommendation> previousRecommendations =
                giftIdeaRecommendationRepository.findByGiftPlan(giftPlan);

        for (GiftIdeaRecommendation recommendation : previousRecommendations) {
            if (Boolean.TRUE.equals(recommendation.getIsSelected())) {
                throw new ApiException("You cannot regenerate recommendations after selecting a gift idea.");
            }
        }

        String prompt = buildRegeneratePrompt(giftPlan, previousRecommendations);
        List<GiftIdeaRecommendation> recommendations = callAiForRecommendationsWithPrompt(prompt, previousRecommendations);

        giftIdeaRecommendationRepository.deleteAll(previousRecommendations);

        LocalDateTime now = LocalDateTime.now();
        List<GiftIdeaRecommendation> saved = new ArrayList<>();
        for (GiftIdeaRecommendation giftRecommendation : recommendations) {
            giftRecommendation.setId(null);
            giftRecommendation.setIsSelected(false);
            giftRecommendation.setGiftPlan(giftPlan);
            giftRecommendation.setCreatedAt(now);
            saved.add(giftIdeaRecommendationRepository.save(giftRecommendation));
        }

        giftPlan.setSelectedGiftIdea(null);
        giftPlan.setStatus(GiftPlanStatus.RECOMMENDATIONS_GENERATED);
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return saved.stream().map(ResponseMapper::toGiftIdeaRecommendationDto).toList();
    }

    private List<GiftIdeaRecommendation> callAiForRecommendations(GiftPlan giftPlan) {
        return callAiForRecommendationsWithPrompt(buildPrompt(giftPlan), List.of());
    }

    private List<GiftIdeaRecommendation> callAiForRecommendationsWithPrompt(
            String prompt, List<GiftIdeaRecommendation> previousRecommendations) {
        String response = aiService.ask(prompt);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response);
        JsonNode recommendationsNode = rootNode.get("recommendations");

        if (recommendationsNode == null || !recommendationsNode.isArray() || recommendationsNode.isEmpty()) {
            throw new ApiException("AI did not return any gift Recommendation.");
        }

        List<GiftIdeaRecommendation> recommendations = mapper.convertValue(
                recommendationsNode,
                new TypeReference<List<GiftIdeaRecommendation>>() {}
        );

        if (!previousRecommendations.isEmpty()) {
            List<String> previousNames = new ArrayList<>();
            for (GiftIdeaRecommendation previous : previousRecommendations) {
                if (previous.getProductName() != null) {
                    previousNames.add(previous.getProductName().toLowerCase());
                }
            }

            boolean allRepeated = true;
            for (GiftIdeaRecommendation r : recommendations) {
                String name = r.getProductName();
                if (name == null || !previousNames.contains(name.toLowerCase())) {
                    allRepeated = false;
                    break;
                }
            }

            if (allRepeated) {
                throw new ApiException("AI returned the same gift ideas again. Please try regenerating once more.");
            }
        }

        return recommendations;
    }

    private String buildRegeneratePrompt(GiftPlan giftPlan, List<GiftIdeaRecommendation> previousRecommendations) {
        String basePrompt = buildPrompt(giftPlan);

        if (previousRecommendations.isEmpty()) {
            return basePrompt;
        }

        StringBuilder exclude = new StringBuilder();
        exclude.append("\nDo not repeat any of these previously suggested gifts:\n");
        for (GiftIdeaRecommendation previous : previousRecommendations) {
            exclude.append("- ").append(previous.getProductName()).append('\n');
        }
        exclude.append("Suggest different gift ideas this time.\n");

        return basePrompt + exclude;
    }

    public String buildPrompt(GiftPlan giftPlan) {
        Recipient recipient = giftPlan.getRecipient();
        List<RequiredQuestionAnswer> requiredAnswers =
                requiredQuestionAnswerRepository.findRequiredQuestionAnswerByGiftPlan(giftPlan);

        List<AiQuestionAnswerDTOOut> aiQuestionAndAnswer =
                aiAnswerService.listAnswers(giftPlan.getUser().getId(), giftPlan.getId());

        StringBuilder context = new StringBuilder();

        context.append("Recipient name: ").append(recipient.getName()).append('\n');
        if (recipient.getRelationship() != null) {
            context.append("Relationship: ").append(recipient.getRelationship()).append('\n');
        }
        if (recipient.getAge() != null) {
            context.append("Age: ").append(recipient.getAge()).append('\n');
        }
        if (recipient.getGender() != null) {
            context.append("Gender: ").append(recipient.getGender()).append('\n');
        }
        if (recipient.getInterests() != null) {
            context.append("Interests: ").append(recipient.getInterests()).append('\n');
        }
        if (recipient.getHobbies() != null) {
            context.append("Hobbies: ").append(recipient.getHobbies()).append('\n');
        }
        if (recipient.getDislikes() != null) {
            context.append("Dislikes: ").append(recipient.getDislikes()).append('\n');
        }
        if (recipient.getPersonalityStyle() != null) {
            context.append("Personality: ").append(recipient.getPersonalityStyle()).append('\n');
        }
        context.append("Occasion: ").append(giftPlan.getOccasionType()).append('\n');
        if (giftPlan.getOccasionDate() != null) {
            context.append("Occasion date: ").append(giftPlan.getOccasionDate()).append('\n');
        }
        context.append("Budget in SAR: ").append(giftPlan.getBudget()).append('\n');
        context.append("Currency: ").append(giftPlan.getCurrency()).append('\n');
        if (giftPlan.getPreferredGiftStyle() != null) {
            context.append("Preferred gift style: ").append(giftPlan.getPreferredGiftStyle()).append('\n');
        }

        if (!requiredAnswers.isEmpty()) {
            context.append("\nRequired question answers:\n");
            for (RequiredQuestionAnswer answer : requiredAnswers) {
                context.append("- ")
                        .append(answer.getRequiredQuestion().getQuestionText())
                        .append(": ")
                        .append(answer.getAnswerText())
                        .append('\n');
            }
        }
        if (!aiQuestionAndAnswer.isEmpty()) {
            context.append("\n helping question answers:\n");
            for (AiQuestionAnswerDTOOut qAndAnswer : aiQuestionAndAnswer) {
                context.append("- ")
                        .append(qAndAnswer.getQuestionText())
                        .append(": ")
                        .append(qAndAnswer.getAnswerText())
                        .append('\n');
            }
        }

        List<GiftHistory> pastGifts = giftHistoryRepository
                .findByRecipient_IdAndUser_IdOrderByCreatedAtDesc(recipient.getId(), giftPlan.getUser().getId());
        if (!pastGifts.isEmpty()) {
            context.append("\nGifts the recipient already received (do not suggest these again):\n");
            for (GiftHistory pastGift : pastGifts) {
                if (pastGift.getGiftName() == null || pastGift.getGiftName().isBlank()) {
                    continue;
                }
                context.append("- ").append(pastGift.getGiftName());
                if (pastGift.getOccasionType() != null && !pastGift.getOccasionType().isBlank()) {
                    context.append(" (").append(pastGift.getOccasionType()).append(')');
                }
                context.append('\n');
            }
        }

        return """
                 suggest gifts based on the context below.
                Return JSON only in this exact shape:
                {
                  "recommendations": [
                       {
                         "productName": "string" (should be a name for real product),
                         "category": "string",
                         "priceBand": "string (a realistic price RANGE as \"min - max CURRENCY\", e.g. \"350 - 450 SAR\")",
                         "reason": "string",
                         "emotionalFit": "string",
                         "practicalFit": "string",
                         "aiExplanation": "string",
                
                       }
                                  ]
                 }

                Rules:
                - Generate 3 to 5 suggested gifts
                - gifts must be relevant to the recipient and occasion
                - do not suggest any gift the recipient already received (see the list in the context)
                - the response should be in Arabic language
                - the suggested gifts should be real product
                - priceBand MUST be a realistic price RANGE (minimum - maximum) for that exact real product, based on its actual current retail market price
                - base the price on real-world prices for the named product; do NOT invent, guess randomly, or use placeholder/round fake numbers
                - the price range must be in the same currency given in the context, and stay within the recipient's budget
                - keep the range tight and credible (the max should not exceed the min by more than ~30%%)
                - the productName of the suggested gifts should be real product name like : iPhone 16 , book Agatha,headphone hyperx
                - priceBand should be in SAR
                Context:
                %s
                """.formatted(context);
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

    public GiftIdeaRecommendationDTOOut getSelectedRecommendation(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        return giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .map(ResponseMapper::toGiftIdeaRecommendationDto)
                .orElseThrow(() -> new ApiException("You have not select any Recommendation for this gift plan."));
    }
}
