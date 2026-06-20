package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupGiftVoteDTOIn {

    @NotNull(message = "Option id is required")
    private Long optionId;

}
