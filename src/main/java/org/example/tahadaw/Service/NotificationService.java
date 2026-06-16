package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.NotificationCreateDTOIn;
import org.example.tahadaw.DTO.IN.NotificationUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.NotificationDTOOut;
import org.example.tahadaw.Model.Notification;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.NotificationStatus;
import org.example.tahadaw.Repository.NotificationRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDTOOut create(Long userId, NotificationCreateDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setStatus(request.getStatus() != null ? request.getStatus() : NotificationStatus.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());

        return toDto(notificationRepository.save(notification));
    }

    public List<NotificationDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public NotificationDTOOut getOne(Long userId, Long notificationId) {
        return toDto(requireOwnedNotification(userId, notificationId));
    }

    @Transactional
    public NotificationDTOOut update(Long userId, Long notificationId, NotificationUpdateDTOIn request) {
        Notification notification = requireOwnedNotification(userId, notificationId);

        if (request.getTitle() != null) {
            notification.setTitle(request.getTitle());
        }
        if (request.getMessage() != null) {
            notification.setMessage(request.getMessage());
        }
        if (request.getType() != null) {
            notification.setType(request.getType());
        }
        if (request.getStatus() != null) {
            notification.setStatus(request.getStatus());
        }

        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void delete(Long userId, Long notificationId) {
        Notification notification = requireOwnedNotification(userId, notificationId);
        notificationRepository.delete(notification);
    }

    private Notification requireOwnedNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findNotificationById(notificationId)
                .orElseThrow(() -> new ApiException("Notification not found."));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException("Notification not found.");
        }
        return notification;
    }

    private NotificationDTOOut toDto(Notification notification) {
        return new NotificationDTOOut(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
