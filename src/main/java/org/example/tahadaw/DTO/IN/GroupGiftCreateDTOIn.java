package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupGiftCreateDTOIn {

    @NotNull
    private Long recipientId;

    @NotBlank
    private String title;

    private String description;
    private String responsiblePersonName;
    private String responsiblePersonEmail;
    private LocalDate giftGivingDate;
    private LocalDateTime votingDeadline;
}
