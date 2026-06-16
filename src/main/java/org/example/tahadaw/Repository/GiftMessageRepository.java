package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftMessageRepository extends JpaRepository<GiftMessage, Long> {

    Optional<GiftMessage> findGiftMessageById(Long id);

    List<GiftMessage> findByGiftPlan_IdOrderByCreatedAtDesc(Long giftPlanId);
}
