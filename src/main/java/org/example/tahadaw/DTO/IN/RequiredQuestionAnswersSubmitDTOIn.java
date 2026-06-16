package org.example.tahadaw.DTO.IN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredQuestionAnswersSubmitDTOIn {

    @NotEmpty
    @Valid
    private List<RequiredQuestionAnswerItemDTOIn> answers;
}
