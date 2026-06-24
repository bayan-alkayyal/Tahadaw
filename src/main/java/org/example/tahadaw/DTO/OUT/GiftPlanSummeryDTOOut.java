package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GiftPlanSummeryDTOOut {

    private Long id;
    private String occasionType;
    private LocalDate occasionDate;
    private RecipientDTOOut recipient;
    private Long budget;
    private SelectedProductSummeryDTOOut selectedProduct;
    private String message;
}
