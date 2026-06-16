package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftPlanRepository extends JpaRepository<GiftPlan, Long> {

    Optional<GiftPlan> findGiftPlanById(Long id);
}
