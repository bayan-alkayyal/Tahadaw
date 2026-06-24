package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GiftPlanRepository extends JpaRepository<GiftPlan, Long> {

    Optional<GiftPlan> findGiftPlanById(Long id);

    List<GiftPlan> findGiftPlanByUserIdAndOccasionDateAfter(Long userId, LocalDate occasionDate);

    List<GiftPlan> findGiftPlanByUserIdAndOccasionDateBefore(Long userId, LocalDate occasionDate);

    List<GiftPlan> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT g FROM GiftPlan g
            WHERE g.user.id = :userId
              AND (g.occasionDate IS NULL OR g.occasionDate >= :today)
            ORDER BY g.createdAt DESC
            """)
    List<GiftPlan> findActivePlansByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("""
            SELECT g FROM GiftPlan g
            WHERE g.user.id = :userId
              AND g.occasionDate IS NOT NULL
              AND g.occasionDate < :today
            ORDER BY g.createdAt DESC
            """)
    List<GiftPlan> findPreviousPlansByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(g) FROM GiftPlan g
            WHERE g.user.id = :userId
              AND (g.occasionDate IS NULL OR g.occasionDate >= :today)
            """)
    long countActivePlansByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);
}
