package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftMessageGenerateDTOIn {

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    private String relationship;

    // What the message is for, e.g. "graduation", "birthday".
    @NotBlank(message = "Occasion is required")
    private String occasion;

    // What the gift is, e.g. "smart watch".
    private String giftName;

    // How the user wants it to sound, e.g. "warm and proud".
    private String tone;

    private String language;
    private String dialect;
}
