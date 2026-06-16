package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.IN.AiGeneratedQuestionDTOIn;
import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Repository.AiGeneratedQuestionRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiQuestionService {


    private final AiGeneratedQuestionRepository aiGeneratedQuestionRepository;
    private final GiftPlanRepository giftPlanRepository;

    //shahad-CRUD

    public void createAiQuestion(Long gifPlanId, AiGeneratedQuestionDTOIn request) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(gifPlanId).orElse(null);
        if (giftPlan == null) {
            throw new IllegalArgumentException("Gift plan not found");
        }

        AiGeneratedQuestion aiGeneratedQuestion = new AiGeneratedQuestion();
        aiGeneratedQuestion.setQuestionText(request.getQuestionText());
        aiGeneratedQuestion.setReasonForQuestion(request.getReasonForQuestion());
        aiGeneratedQuestion.setDisplayOrder(request.getDisplayOrder());
        aiGeneratedQuestion.setGiftPlan(giftPlan);

        aiGeneratedQuestionRepository.save(aiGeneratedQuestion);
    }

    public List<AiGeneratedQuestion> getAllAiQuestion() {
        return aiGeneratedQuestionRepository.findAll();
    }

    public void updateAiQuestion(long id, AiGeneratedQuestionDTOIn request) {
        AiGeneratedQuestion oldAiGeneratedQuestion = getAiQuestionById(id);
        oldAiGeneratedQuestion.setQuestionText(request.getQuestionText());
        oldAiGeneratedQuestion.setReasonForQuestion(request.getReasonForQuestion());
        oldAiGeneratedQuestion.setDisplayOrder(request.getDisplayOrder());
        aiGeneratedQuestionRepository.save(oldAiGeneratedQuestion);

    }

    public void deleteAiQuestion(Long id) {
        AiGeneratedQuestion aiGeneratedQuestion = getAiQuestionById(id);
        aiGeneratedQuestionRepository.delete(aiGeneratedQuestion);
    }


    public AiGeneratedQuestion getAiQuestionById(Long id) {
        AiGeneratedQuestion aiGeneratedQuestion = aiGeneratedQuestionRepository.findAiGeneratedQuestionById(id).orElse(null);
        if (aiGeneratedQuestion == null) {
            throw new IllegalArgumentException("Ai question not found");
        }
        return aiGeneratedQuestion;
    }
}
