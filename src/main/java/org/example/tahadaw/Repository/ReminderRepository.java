package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Optional<Reminder> findReminderById(Long id);
}
