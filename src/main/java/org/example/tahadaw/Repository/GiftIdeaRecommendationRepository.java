package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftIdeaRecommendationRepository extends JpaRepository<GiftIdeaRecommendation, Long> {

    Optional<GiftIdeaRecommendation> findGiftIdeaRecommendationById(Long id);

    Optional<GiftIdeaRecommendation> findByGiftPlanAndIsSelectedTrue(GiftPlan giftPlan);

    List<GiftIdeaRecommendation> findByGiftPlan(GiftPlan giftPlan);

    boolean existsByGiftPlan_IdAndIsSelectedTrueAndIdNot(Long giftPlanId, Long id);
}
