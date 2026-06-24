package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredQuestionAnswerDTOOut {

    private Long id;
    private Long requiredQuestionId;
    private String questionText;
    private String answerText;
}
