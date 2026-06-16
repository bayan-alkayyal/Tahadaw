package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.PremiumAccess;
import org.example.tahadaw.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PremiumAccessRepository extends JpaRepository<PremiumAccess, Long> {

    Optional<PremiumAccess> findPremiumAccessById(Long id);

    Optional<PremiumAccess> findByUser(User user);
}
