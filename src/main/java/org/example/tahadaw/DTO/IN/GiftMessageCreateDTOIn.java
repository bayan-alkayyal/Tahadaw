package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for a user-written (manual) gift message — no AI involved.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftMessageCreateDTOIn {

    @NotBlank(message = "Message text is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String messageText;
}
