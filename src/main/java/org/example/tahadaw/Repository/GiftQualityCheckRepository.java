package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GiftQualityCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftQualityCheckRepository extends JpaRepository<GiftQualityCheck, Long> {

    Optional<GiftQualityCheck> findGiftQualityCheckById(Long id);
}
