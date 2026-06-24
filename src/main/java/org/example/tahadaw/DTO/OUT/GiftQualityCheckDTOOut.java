package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GiftQualityCheckDTOOut {

    private Long id;
    private String giftName;
    private String giftDescription;
    private Double price;
    private String occasionType;
    private String suitability;
    private String strengths;
    private String weaknesses;
    private String aiAdvice;
}
