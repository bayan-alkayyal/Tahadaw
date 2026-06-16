package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.NotificationStatus;
import org.example.tahadaw.Model.enums.NotificationType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateDTOIn {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationType type;

    private NotificationStatus status;
}
