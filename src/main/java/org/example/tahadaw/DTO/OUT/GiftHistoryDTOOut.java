package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GiftHistoryDTOOut {

    private Long id;
    private Long recipientId;
    private String giftName;
    private String occasionType;
    private LocalDate giftDate;
    private Double priceMinor;
    private Boolean wasGifted;
    private Integer userRating;
    private String notes;
}
