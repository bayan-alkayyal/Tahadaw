package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.OccasionType;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftHistoryUpdateDTOIn {

    private String giftName;
    private OccasionType occasionType;
    private LocalDate giftDate;
    private Long priceMinor;
    private Boolean wasGifted;
    private Integer userRating;
    private String notes;
}
