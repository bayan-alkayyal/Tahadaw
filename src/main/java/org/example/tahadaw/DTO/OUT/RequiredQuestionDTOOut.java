package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.QuestionType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredQuestionDTOOut {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Integer displayOrder;
}
