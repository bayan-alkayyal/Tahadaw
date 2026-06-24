package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GiftCardDTOOut {

    private Long id;
    private Long giftMessageId;
    private String recipientName;
    private String senderName;
    private String cardSize;
    private String linkType;
    private String linkUrl;
    private String sentToEmail;
}
