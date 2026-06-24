package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GiftIdeaRecommendationDTOOut {

    private Long id;
    private String productName;
    private String category;
    private String priceBand;
    private String reason;
    private String emotionalFit;
    private String practicalFit;
    private String aiExplanation;
    private boolean selected;
}
