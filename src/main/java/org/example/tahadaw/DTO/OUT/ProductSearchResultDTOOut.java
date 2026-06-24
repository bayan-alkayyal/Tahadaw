package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchResultDTOOut {

    private Long id;
    private String title;
    private Double price;
    private String currency;
    private String imageUrl;
    private String productUrl;
    private String sourceName;
    private Double rating;
}
