package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AiQuestionAnswerDetailDTOOut {

    private Long id;
    private Long aiGeneratedQuestionId;
    private String questionText;
    private String answerText;
}
