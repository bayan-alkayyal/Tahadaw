package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.SurprisePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurprisePlanRepository extends JpaRepository<SurprisePlan, Long> {

    Optional<SurprisePlan> findSurprisePlanById(Long id);
}
