package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GiftCardDTOOut {

    private Long id;
    private Long userId;
    private Long giftMessageId;
    private String recipientName;
    private String senderName;
    private String cardSize;
    private String linkType;
    private String linkUrl;
    private String sentToEmail;
    private String status;
    private LocalDateTime createdAt;
}
