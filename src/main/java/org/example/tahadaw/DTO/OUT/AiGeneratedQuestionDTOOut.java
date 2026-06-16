package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGeneratedQuestionDTOOut {

    private Long id;
    private Long giftPlanId;
    private String questionText;
    private String reasonForQuestion;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
