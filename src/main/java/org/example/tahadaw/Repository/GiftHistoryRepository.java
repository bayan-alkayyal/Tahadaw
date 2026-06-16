package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftHistoryRepository extends JpaRepository<GiftHistory, Long> {

    Optional<GiftHistory> findGiftHistoryById(Long id);

    List<GiftHistory> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<GiftHistory> findByRecipient_IdAndUser_IdOrderByCreatedAtDesc(Long recipientId, Long userId);

    boolean existsByGiftIdeaRecommendation_Id(Long giftIdeaRecommendationId);
}
