package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GiftPlanDTOOut {

    private Long id;
    private Long recipientId;
    private String occasionType;
    private LocalDate occasionDate;
    private Long budget;
    private String currency;
    private String preferredGiftStyle;
    private String language;
    private Long selectedGiftIdeaId;
    private Long selectedProductId;
}
