package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.CardSize;
import org.example.tahadaw.Model.enums.GiftCardStatus;
import org.example.tahadaw.Model.enums.LinkType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftCardUpdateDTOIn {

    private String recipientName;
    private String senderName;
    private CardSize cardSize;
    private LinkType linkType;
    private String linkUrl;
    private String sentToEmail;
    private GiftCardStatus status;
}
