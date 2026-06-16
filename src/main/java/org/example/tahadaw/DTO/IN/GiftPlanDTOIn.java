package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.GiftStyle;
import org.example.tahadaw.Model.enums.OccasionType;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftPlanDTOIn {

    @NotNull
    private OccasionType occasionType;

    @NotNull
    @FutureOrPresent
    private LocalDate occasionDate;

    @NotNull
    @Positive
    private Long budgetMinor;

    @NotEmpty(message = "Currency cannot be empty")
    @Size(min = 3, max = 3)
    private String currency;

    private GiftStyle preferredGiftStyle;

    @Size(max = 10)
    private String language;
}
