package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findRecipientById(Long id);
}
