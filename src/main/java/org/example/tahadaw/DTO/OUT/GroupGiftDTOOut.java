package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tahadaw.Model.enums.GroupGiftStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GroupGiftDTOOut {

    private Long id;
    private Long ownerId;
    private Long recipientId;
    private String title;
    private String description;
    private String responsiblePersonName;
    private String responsiblePersonEmail;
    private LocalDate giftGivingDate;
    private LocalDateTime votingDeadline;
    private Long winningOptionId;
    private GroupGiftStatus status;
    private LocalDateTime createdAt;
}
