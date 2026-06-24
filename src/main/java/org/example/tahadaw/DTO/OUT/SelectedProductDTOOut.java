package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SelectedProductDTOOut {

    private Long id;
    private String title;
    private Double priceMinor;
    private String currency;
    private String imageUrl;
    private String productUrl;
    private String sourceName;
    private Double rating;
}
