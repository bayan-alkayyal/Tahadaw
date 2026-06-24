package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiGeneratedQuestionDTOOut {

    private Long id;
    private String questionText;
    private String reasonForQuestion;
    private Integer displayOrder;
}
