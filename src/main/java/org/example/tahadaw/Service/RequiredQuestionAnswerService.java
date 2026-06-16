package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerDTOIn;
import org.example.tahadaw.Model.RequiredQuestion;
import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.example.tahadaw.Repository.RequiredQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequiredQuestionAnswerService {

    //shahad-CRUD

    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final RequiredQuestionRepository requiredQuestionRepository;


    //shahad-CRUD

    public void createRequiredQuestionAnswer(Long requiredQuestionId, RequiredQuestionAnswerDTOIn request) {
        RequiredQuestion requiredQuestion = requiredQuestionRepository.findRequiredQuestionById(requiredQuestionId).orElse(null);
        if (requiredQuestion == null) {
            throw new IllegalArgumentException("Question not found");
        }

        RequiredQuestionAnswer requiredQuestionAnswer = new RequiredQuestionAnswer();
        requiredQuestionAnswer.setAnswerText(request.getAnswerText());
        requiredQuestionAnswer.setRequiredQuestion(requiredQuestion);

        requiredQuestionAnswerRepository.save(requiredQuestionAnswer);

    }

    public List<RequiredQuestionAnswer> getAllRequiredQuestionAnswer() {
        return requiredQuestionAnswerRepository.findAll();
    }

    public void updateRequiredQuestionAnswer(long id, RequiredQuestionAnswerDTOIn request) {
        RequiredQuestionAnswer oldRequiredQuestionAnswer = getRequiredQuestionAnswerById(id);
        oldRequiredQuestionAnswer.setAnswerText(request.getAnswerText());
        requiredQuestionAnswerRepository.save(oldRequiredQuestionAnswer);

    }

    public void deleteRequiredQuestionAnswer(Long id) {
        RequiredQuestionAnswer oldRequiredQuestionAnswer = getRequiredQuestionAnswerById(id);
        requiredQuestionAnswerRepository.delete(oldRequiredQuestionAnswer);
    }


    public RequiredQuestionAnswer getRequiredQuestionAnswerById(Long id) {
        RequiredQuestionAnswer requiredQuestionAnswer = requiredQuestionAnswerRepository.findRequiredQuestionAnswerById(id).orElse(null);
        if (requiredQuestionAnswer == null) {
            throw new IllegalArgumentException("Answer not found");
        }
        return requiredQuestionAnswer;
    }
}
