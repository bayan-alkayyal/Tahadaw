package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.GroupGiftStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupGiftUpdateDTOIn {

    private String title;
    private String description;
    private String responsiblePersonName;
    private String responsiblePersonEmail;
    private LocalDate giftGivingDate;
    private LocalDateTime votingDeadline;
    private GroupGiftStatus status;
}
