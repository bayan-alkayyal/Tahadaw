package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiftPlanSummeryDTOOut {

    private Long id;
    private String occasionType;
    private LocalDate occasionDate;
    private RecipientDTOOut recipient;
    private Long budgetMinor;
    private SelectedProductSummeryDTOOut selectedGiftIdeaId;

}
