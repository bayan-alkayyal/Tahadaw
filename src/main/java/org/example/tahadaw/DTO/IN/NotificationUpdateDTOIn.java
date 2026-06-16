package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.NotificationStatus;
import org.example.tahadaw.Model.enums.NotificationType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationUpdateDTOIn {

    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
}
