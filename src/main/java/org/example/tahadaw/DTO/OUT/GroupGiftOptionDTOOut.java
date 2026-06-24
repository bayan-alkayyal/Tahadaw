package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GroupGiftOptionDTOOut {

    private Long id;
    private String giftName;
    private String description;
    private String priceBand;
    private String reason;
}
