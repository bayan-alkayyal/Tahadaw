package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findNotificationById(Long id);

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
