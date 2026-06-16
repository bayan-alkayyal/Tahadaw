package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tahadaw.Model.enums.NotificationStatus;
import org.example.tahadaw.Model.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDTOOut {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
