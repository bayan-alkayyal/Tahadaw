package org.example.tahadaw.Repository;

import jakarta.transaction.Transactional;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.SelectedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SelectedProductRepository extends JpaRepository<SelectedProduct, Long> {


    SelectedProduct findSelectedProductById(Long id);

    List<SelectedProduct> findSelectedProductByGiftIdeaRecommendationAndIsSelectedTrue(GiftIdeaRecommendation giftIdeaRecommendation);

    @Modifying
    @Transactional
    @Query("DELETE FROM SelectedProduct WHERE giftIdeaRecommendation = ?1 And isSelected = false")
    void deleteSelectedProductsByGiftIdeaRecommendation( GiftIdeaRecommendation giftIdeaRecommendation);

    SelectedProduct findSelectedProductByGiftPlan(GiftPlan giftPlan);
    
    List<SelectedProduct> findSelectedProductByGiftIdeaRecommendation(GiftIdeaRecommendation giftIdeaRecommendation);

    List<SelectedProduct> findByUser_IdAndIsSelectedTrueOrderByCreatedAtDesc(Long userId);

    List<SelectedProduct> findByRecipient_IdAndIsSelectedTrueOrderByCreatedAtDesc(Long recipientId);

}
