package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tahadaw.Model.enums.OccasionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GiftHistoryDTOOut {

    private Long id;
    private Long userId;
    private Long recipientId;
    private Long giftIdeaRecommendationId;
    private String giftName;
    private OccasionType occasionType;
    private LocalDate giftDate;
    private Long priceMinor;
    private Boolean wasGifted;
    private Integer userRating;
    private String notes;
    private LocalDateTime createdAt;
}
