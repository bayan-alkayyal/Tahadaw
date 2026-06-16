package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftPlanRepository extends JpaRepository<GiftPlan, Long> {
    GiftPlan findGiftPlanById(Long giftId);
}
