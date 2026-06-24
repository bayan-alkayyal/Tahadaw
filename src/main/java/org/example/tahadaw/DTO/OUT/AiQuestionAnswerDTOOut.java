package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiQuestionAnswerDTOOut {

    private Long id;
    private Long aiGeneratedQuestionId;
    private String questionText;
    private String answerText;
}
