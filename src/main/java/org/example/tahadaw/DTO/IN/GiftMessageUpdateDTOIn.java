package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update for an existing gift message. Only non-null fields are applied.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftMessageUpdateDTOIn {

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String messageText;

    private String tone;
    private String language;
}
