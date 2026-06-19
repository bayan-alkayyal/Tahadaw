package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftHistoryRepository extends JpaRepository<GiftHistory, Long> {

    List<GiftHistory> findByRecipient_IdAndUser_IdOrderByCreatedAtDesc(Long recipientId, Long userId);

    boolean existsBySelectedProduct_Id(Long selectedProductId);

    Optional<GiftHistory> findBySelectedProduct_Id(Long selectedProductId);

}
