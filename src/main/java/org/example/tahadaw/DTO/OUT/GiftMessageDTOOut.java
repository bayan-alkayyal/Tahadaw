package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GiftMessageDTOOut {

    private Long id;
    private String tone;
    private String language;
    private String messageText;
}
