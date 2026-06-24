package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardUpdateDTOIn {

    private String recipientName;
    private String senderName;
    private String cardSize;
    private String linkType;
    private String linkUrl;
    private String sentToEmail;
}
