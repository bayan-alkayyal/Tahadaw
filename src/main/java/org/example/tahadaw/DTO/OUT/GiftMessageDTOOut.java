package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GiftMessageDTOOut {

    private Long id;
    private Long giftPlanId;
    private String tone;
    private String language;
    private String messageText;
    private LocalDateTime createdAt;
}
