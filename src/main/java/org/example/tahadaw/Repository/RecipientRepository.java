package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Recipient findRecipientById(Long id);
}
