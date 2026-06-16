package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GroupGiftInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupGiftInviteRepository extends JpaRepository<GroupGiftInvite, Long> {

    Optional<GroupGiftInvite> findGroupGiftInviteById(Long id);

    Optional<GroupGiftInvite> findByToken(String token);
}
