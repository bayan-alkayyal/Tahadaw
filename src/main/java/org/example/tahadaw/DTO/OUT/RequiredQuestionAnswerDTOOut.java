package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredQuestionAnswerDTOOut {

    private Long id;
    private Long giftPlanId;
    private Long requiredQuestionId;
    private String questionText;
    private String answerText;
    private LocalDateTime createdAt;
}
