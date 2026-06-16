package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGeneratedQuestionDTOIn {

    @NotEmpty(message = "Question text cannot be empty")
    private String questionText;

    @NotEmpty(message = "Reason for question cannot be empty")
    private String reasonForQuestion;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;
}
