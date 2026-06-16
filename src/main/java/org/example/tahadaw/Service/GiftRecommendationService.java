package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.enums.GiftPlanStatus;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GiftRecommendationService {

    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final GiftPlanRepository giftPlanRepository;

    @Transactional
    public void selectRecommendation(Long userId, Long recommendationId) {
        GiftIdeaRecommendation recommendation = giftIdeaRecommendationRepository
                .findGiftIdeaRecommendationById(recommendationId)
                .orElseThrow(() -> new ApiException("Gift idea recommendation not found."));

        GiftPlan giftPlan = recommendation.getGiftPlan();
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift idea recommendation not found.");
        }

        for (GiftIdeaRecommendation idea : giftIdeaRecommendationRepository.findByGiftPlan(giftPlan)) {
            idea.setIsSelected(idea.getId().equals(recommendationId));
            giftIdeaRecommendationRepository.save(idea);
        }

        giftPlan.setSelectedGiftIdea(recommendation);
        if (giftPlan.getStatus() == GiftPlanStatus.RECOMMENDATIONS_GENERATED) {
            giftPlan.setStatus(GiftPlanStatus.GIFT_IDEA_SELECTED);
        }
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }
}
