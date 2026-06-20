package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GiftPlanRepository extends JpaRepository<GiftPlan, Long> {

    Optional<GiftPlan> findGiftPlanById(Long id);

    List<GiftPlan> findGiftPlanByUserIdAndOccasionDateAfter(Long userId, LocalDate occasionDate);

    List<GiftPlan> findGiftPlanByUserIdAndOccasionDateBefore(Long userId, LocalDate occasionDate);

    List<GiftPlan> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
