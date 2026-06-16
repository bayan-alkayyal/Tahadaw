package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftMessageGenerateDTOIn {

    private String tone;
    private String language;
    private String dialect;
}
