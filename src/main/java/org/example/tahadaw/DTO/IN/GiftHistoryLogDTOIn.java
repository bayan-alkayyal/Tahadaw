package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User-written history fields attached to an already-selected product.
 * Facts (gift name, occasion, date, price, recipient) are derived from the selected product.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftHistoryLogDTOIn {

    private Boolean wasGifted;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer userRating;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
