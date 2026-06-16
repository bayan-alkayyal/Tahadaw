package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {

    Optional<GiftCard> findGiftCardById(Long id);

    List<GiftCard> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<GiftCard> findByGiftPlan_Id(Long giftPlanId);

    boolean existsByGiftMessage_Id(Long giftMessageId);
}
