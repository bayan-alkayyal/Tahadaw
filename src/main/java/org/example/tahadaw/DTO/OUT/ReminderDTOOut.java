package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReminderDTOOut {

    private Long id;
    private Long recipientId;
    private Long giftPlanId;
    private Long groupGiftId;
    private LocalDateTime reminderDate;
    private String message;
}
