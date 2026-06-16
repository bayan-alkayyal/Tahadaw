package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.AiQuestionAnswerDTOIn;
import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.example.tahadaw.Model.AiQuestionAnswer;
import org.example.tahadaw.Repository.AiGeneratedQuestionRepository;
import org.example.tahadaw.Repository.AiQuestionAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAnswerService {


    private final AiGeneratedQuestionRepository aiGeneratedQuestionRepository;
    private final AiQuestionAnswerRepository aiQuestionAnswerRepository;


    //shahad-CRUD

    public void createAiAnswer(Long aiQuestionId, AiQuestionAnswerDTOIn request) {
        AiGeneratedQuestion aiGeneratedQuestion = aiGeneratedQuestionRepository.findAiGeneratedQuestionById(aiQuestionId).orElse(null);
        if (aiGeneratedQuestion == null) {
            throw new ApiException("Ai question not found");
        }

        AiQuestionAnswer aiQuestionAnswer = new AiQuestionAnswer();
        aiQuestionAnswer.setAnswerText(request.getAnswerText());
        aiQuestionAnswer.setAiGeneratedQuestion(aiGeneratedQuestion);
        aiQuestionAnswer.setCreatedAt(LocalDate.now());

        aiQuestionAnswerRepository.save(aiQuestionAnswer);
    }

    public List<AiQuestionAnswer> getAllAiAnswer() {
        return aiQuestionAnswerRepository.findAll();
    }

    public void updateAiQuestionAnswer(long id, AiQuestionAnswerDTOIn request) {
        AiQuestionAnswer oldAiQuestionAnswer = getAiQuestionAnswerById(id);
        oldAiQuestionAnswer.setAnswerText(request.getAnswerText());
        aiQuestionAnswerRepository.save(oldAiQuestionAnswer);

    }

    public void deleteAiQuestionAnswer(Long id) {
        AiQuestionAnswer aiQuestionAnswer = getAiQuestionAnswerById(id);
        aiQuestionAnswerRepository.delete(aiQuestionAnswer);
    }


    public AiQuestionAnswer getAiQuestionAnswerById(Long id) {
        AiQuestionAnswer aiQuestionAnswer = aiQuestionAnswerRepository.findAiQuestionAnswerById(id).orElse(null);
        if (aiQuestionAnswer == null) {
            throw new ApiException("Ai Answer not found");
        }
        return aiQuestionAnswer;
    }
}
