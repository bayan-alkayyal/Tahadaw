package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GroupGiftDTOOut {

    private Long id;
    private Long recipientId;
    private String title;
    private String description;
    private LocalDate giftGivingDate;
    private LocalDateTime votingDeadline;
    private Boolean votingOpen;
    private Long winningOptionId;
}
