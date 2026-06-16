package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findRecipientById(Long recipientId);
}
