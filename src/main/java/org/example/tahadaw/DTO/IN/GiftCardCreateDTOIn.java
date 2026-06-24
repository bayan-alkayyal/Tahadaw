package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardCreateDTOIn {

    private Long giftMessageId;

    @NotBlank
    private String recipientName;

    @NotBlank
    private String senderName;

    private String cardSize;
    private String linkType;
    private String linkUrl;
    private String sentToEmail;
}
