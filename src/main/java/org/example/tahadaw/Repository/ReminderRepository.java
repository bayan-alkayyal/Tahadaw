package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Reminder findReminderById(Long id);
}
