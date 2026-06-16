package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GroupGiftVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupGiftVoteRepository extends JpaRepository<GroupGiftVote, Long> {

    Optional<GroupGiftVote> findGroupGiftVoteById(Long id);
}
