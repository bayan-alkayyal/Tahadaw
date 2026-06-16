package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tahadaw.Model.enums.CardSize;
import org.example.tahadaw.Model.enums.GiftCardStatus;
import org.example.tahadaw.Model.enums.LinkType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GiftCardDTOOut {

    private Long id;
    private Long userId;
    private Long giftPlanId;
    private Long giftMessageId;
    private String recipientName;
    private String senderName;
    private CardSize cardSize;
    private LinkType linkType;
    private String linkUrl;
    private String sentToEmail;
    private GiftCardStatus status;
    private LocalDateTime createdAt;
}
