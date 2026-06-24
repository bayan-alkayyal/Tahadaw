package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.SurprisePlanGenerateDTOIn;
import org.example.tahadaw.DTO.IN.SurprisePlanUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.SurprisePlanDTOOut;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Model.SurprisePlan;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.SurprisePlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SurprisePlanService {

    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final SurprisePlanRepository surprisePlanRepository;
    private final PremiumService premiumService;
    private final AiService aiService;

    @Transactional
    public SurprisePlanDTOOut generate(Long userId, Long giftPlanId, SurprisePlanGenerateDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);
        premiumService.requirePremium(giftPlan.getUser());

        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select an AI gift idea before generating a surprise plan."));

        if (surprisePlanRepository.findByGiftPlan_Id(giftPlanId).isPresent()) {
            throw new ApiException("Surprise plan already generated for this gift plan.");
        }

        SurprisePlan surprisePlan = new SurprisePlan();
        surprisePlan.setGiftPlan(giftPlan);
        surprisePlan.setCreatedAt(LocalDateTime.now());
        applyAiFields(surprisePlan, giftPlan, selectedIdea, resolveLanguage(request, giftPlan));

        return toDto(surprisePlanRepository.save(surprisePlan));
    }

    /**
     * Re-run the AI for an existing plan (or create one if none exists yet).
     */
    @Transactional
    public SurprisePlanDTOOut regenerate(Long userId, Long giftPlanId, SurprisePlanGenerateDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);
        premiumService.requirePremium(giftPlan.getUser());

        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select an AI gift idea before generating a surprise plan."));

        SurprisePlan surprisePlan = surprisePlanRepository.findByGiftPlan_Id(giftPlanId)
                .orElseGet(() -> {
                    SurprisePlan created = new SurprisePlan();
                    created.setGiftPlan(giftPlan);
                    created.setCreatedAt(LocalDateTime.now());
                    return created;
                });

        applyAiFields(surprisePlan, giftPlan, selectedIdea, resolveLanguage(request, giftPlan));

        return toDto(surprisePlanRepository.save(surprisePlan));
    }

    /**
     * Manual edit of an existing surprise plan (no AI). Only non-null fields are applied.
     */
    @Transactional
    public SurprisePlanDTOOut update(Long userId, Long giftPlanId, SurprisePlanUpdateDTOIn request) {
        requireOwnedGiftPlan(userId, giftPlanId);
        SurprisePlan surprisePlan = surprisePlanRepository.findByGiftPlan_Id(giftPlanId)
                .orElseThrow(() -> new ApiException("No surprise plan found for this gift plan."));

        if (request.getPlanTitle() != null && !request.getPlanTitle().isBlank()) {
            surprisePlan.setPlanTitle(request.getPlanTitle().trim());
        }
        if (request.getSteps() != null && !request.getSteps().isBlank()) {
            surprisePlan.setSteps(request.getSteps().trim());
        }
        if (request.getRequiredItems() != null) {
            surprisePlan.setRequiredItems(request.getRequiredItems());
        }
        if (request.getTimingSuggestion() != null) {
            surprisePlan.setTimingSuggestion(request.getTimingSuggestion());
        }
        if (request.getBackupPlan() != null) {
            surprisePlan.setBackupPlan(request.getBackupPlan());
        }
        if (request.getAiExplanation() != null) {
            surprisePlan.setAiExplanation(request.getAiExplanation());
        }

        return toDto(surprisePlanRepository.save(surprisePlan));
    }

    @Transactional
    public void delete(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);
        SurprisePlan surprisePlan = surprisePlanRepository.findByGiftPlan_Id(giftPlanId)
                .orElseThrow(() -> new ApiException("No surprise plan found for this gift plan."));
        surprisePlanRepository.delete(surprisePlan);
    }

    private void applyAiFields(SurprisePlan surprisePlan, GiftPlan giftPlan,
                               GiftIdeaRecommendation selectedIdea, String language) {
        String prompt = buildPrompt(giftPlan, selectedIdea, language);
        JsonNode aiResponse = AiJsonParser.parseObject(aiService.ask(prompt));

        surprisePlan.setPlanTitle(AiJsonParser.requireText(aiResponse, "planTitle"));
        surprisePlan.setSteps(AiJsonParser.requireText(aiResponse, "steps"));
        surprisePlan.setRequiredItems(AiJsonParser.optionalText(aiResponse, "requiredItems"));
        surprisePlan.setTimingSuggestion(AiJsonParser.optionalText(aiResponse, "timingSuggestion"));
        surprisePlan.setBackupPlan(AiJsonParser.optionalText(aiResponse, "backupPlan"));
        surprisePlan.setAiExplanation(AiJsonParser.optionalText(aiResponse, "aiExplanation"));
    }

    public SurprisePlanDTOOut getByGiftPlan(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        SurprisePlan surprisePlan = surprisePlanRepository.findByGiftPlan_Id(giftPlanId)
                .orElseThrow(() -> new ApiException("No surprise plan found for this gift plan."));
        return toDto(surprisePlan);
    }

    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        return giftPlan;
    }

    private String resolveLanguage(SurprisePlanGenerateDTOIn request, GiftPlan giftPlan) {
        if (request != null && request.getLanguage() != null && !request.getLanguage().isBlank()) {
            return request.getLanguage().trim();
        }
        if (giftPlan.getLanguage() != null && !giftPlan.getLanguage().isBlank()) {
            return giftPlan.getLanguage().trim();
        }
        return "en";
    }

    private String buildPrompt(GiftPlan giftPlan, GiftIdeaRecommendation selectedIdea, String language) {
        Recipient recipient = giftPlan.getRecipient();
        SelectedProduct selectedProduct = selectedIdea.getSelectedProducts().stream().findFirst().orElse(null);

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
        context.append("Selected gift idea: ").append(selectedIdea.getProductName()).append('\n');
        if (selectedIdea.getReason() != null) {
            context.append("Why this gift: ").append(selectedIdea.getReason()).append('\n');
        }
        if (selectedProduct != null) {
            context.append("Selected product: ").append(selectedProduct.getProductName()).append('\n');
        }

        return """
                Design a memorable surprise plan for delivering the gift below.
                Return JSON only in this exact shape:
                {
                  "planTitle": "string",
                  "steps": "numbered, step-by-step plan as one string",
                  "requiredItems": "string",
                  "timingSuggestion": "string",
                  "backupPlan": "string",
                  "aiExplanation": "string"
                }

                Rules:
                - Write ALL text fields in language code: %s
                - Make the plan practical and tailored to the recipient and occasion
                - steps must be concrete and ordered
                - backupPlan covers what to do if the surprise risks being spoiled
                - aiExplanation briefly justifies why this surprise fits the recipient
                - Do not mention that AI wrote the plan

                Context:
                %s
                """.formatted(language, context);
    }

    private SurprisePlanDTOOut toDto(SurprisePlan surprisePlan) {
        return new SurprisePlanDTOOut(
                surprisePlan.getId(),
                surprisePlan.getPlanTitle(),
                surprisePlan.getSteps(),
                surprisePlan.getRequiredItems(),
                surprisePlan.getTimingSuggestion(),
                surprisePlan.getBackupPlan(),
                surprisePlan.getAiExplanation()
        );
    }
}
