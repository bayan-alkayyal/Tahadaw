package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipientDTOOut {

    private Long id;
    private String name;
    private String relationship;
    private Integer age;
    private String gender;
    private String interests;
    private String hobbies;
    private String favoriteColors;
    private String favoriteBrands;
    private String dislikes;
    private String personalityStyle;
    private String sizeInfo;
    private String notes;
}
