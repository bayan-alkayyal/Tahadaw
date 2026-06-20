package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.GroupGiftOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupGiftOptionRepository extends JpaRepository<GroupGiftOption, Long> {

    Optional<GroupGiftOption> findGroupGiftOptionById(Long id);

    List<GroupGiftOption> findAllByGroupGift_Id(Long groupGiftId);
}
