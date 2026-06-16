package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiQuestionAnswerItemDTOIn {

    @NotNull
    private Long aiGeneratedQuestionId;

    @NotBlank
    private String answerText;
}
