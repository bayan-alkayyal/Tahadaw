package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequiredQuestionDetailDTOOut {

    private Long id;
    private String questionText;
    private String questionType;
    private Integer displayOrder;
    private boolean active;
}
