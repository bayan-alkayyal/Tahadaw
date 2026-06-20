package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GroupGift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupGiftRepository extends JpaRepository<GroupGift, Long> {

    Optional<GroupGift> findGroupGiftById(Long id);

    List<GroupGift> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);


}
