package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.CardSize;
import org.example.tahadaw.Model.enums.GiftCardStatus;
import org.example.tahadaw.Model.enums.LinkType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardCreateDTOIn {

    @NotNull
    private Long giftPlanId;

    private Long giftMessageId;

    @NotBlank
    private String recipientName;

    @NotBlank
    private String senderName;

    private CardSize cardSize;
    private LinkType linkType;
    private String linkUrl;
    private String sentToEmail;
    private GiftCardStatus status;
}
