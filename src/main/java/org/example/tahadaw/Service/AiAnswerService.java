package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.AiQuestionAnswerDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswerItemDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDetailDTOOut;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.example.tahadaw.Model.AiQuestionAnswer;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.GiftPlanStatus;
import org.example.tahadaw.Repository.AiGeneratedQuestionRepository;
import org.example.tahadaw.Repository.AiQuestionAnswerRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiAnswerService {


    private final AiGeneratedQuestionRepository aiGeneratedQuestionRepository;
    private final AiQuestionAnswerRepository aiQuestionAnswerRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final AiService aiService;


    //shahad-CRUD

    public void createAiAnswer(Long aiQuestionId, AiQuestionAnswerDTOIn request) {
        AiGeneratedQuestion aiGeneratedQuestion = aiGeneratedQuestionRepository.findAiGeneratedQuestionById(aiQuestionId).orElse(null);
        if (aiGeneratedQuestion == null) {
            throw new ApiException("Ai question not found");
        }

        AiQuestionAnswer aiQuestionAnswer = new AiQuestionAnswer();
        aiQuestionAnswer.setAnswerText(request.getAnswerText());
        aiQuestionAnswer.setAiGeneratedQuestion(aiGeneratedQuestion);
        aiQuestionAnswer.setCreatedAt(LocalDateTime.now());

        aiQuestionAnswerRepository.save(aiQuestionAnswer);
    }

    public List<AiQuestionAnswerDetailDTOOut> getAllAiAnswer() {
        return aiQuestionAnswerRepository.findAll().stream()
                .map(ResponseMapper::toAiQuestionAnswerDetailDto)
                .toList();
    }

    public void updateAiQuestionAnswer(long id, AiQuestionAnswerDTOIn request) {
        AiQuestionAnswer oldAiQuestionAnswer = getAiQuestionAnswerEntityById(id);
        oldAiQuestionAnswer.setAnswerText(request.getAnswerText());
        aiQuestionAnswerRepository.save(oldAiQuestionAnswer);

    }

    public void deleteAiQuestionAnswer(Long id) {
        AiQuestionAnswer aiQuestionAnswer = getAiQuestionAnswerEntityById(id);
        aiQuestionAnswerRepository.delete(aiQuestionAnswer);
    }


    public AiQuestionAnswerDetailDTOOut getAiQuestionAnswerById(Long id) {
        return ResponseMapper.toAiQuestionAnswerDetailDto(getAiQuestionAnswerEntityById(id));
    }

    private AiQuestionAnswer getAiQuestionAnswerEntityById(Long id) {
        AiQuestionAnswer aiQuestionAnswer = aiQuestionAnswerRepository.findAiQuestionAnswerById(id).orElse(null);
        if (aiQuestionAnswer == null) {
            throw new ApiException("Ai Answer not found");
        }
        return aiQuestionAnswer;
    }

    @Transactional
    public List<AiQuestionAnswerDTOOut> submitAnswers(Long userId, Long giftPlanId,
                                                      AiQuestionAnswersSubmitDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (!GiftPlanStatus.AI_QUESTIONS_GENERATED.equals(giftPlan.getStatus())
                && !GiftPlanStatus.AI_QUESTIONS_ANSWERED.equals(giftPlan.getStatus())) {
            throw new ApiException("Generate AI follow-up questions before submitting answers.");
        }
        if (GiftPlanStatus.isAtOrAfterRecommendations(giftPlan.getStatus())) {
            throw new ApiException("Gift recommendations already generated; AI answers cannot be changed.");
        }

        List<AiGeneratedQuestion> planQuestions =
                aiGeneratedQuestionRepository.findByGiftPlan_IdOrderByDisplayOrderAsc(giftPlanId);
        if (planQuestions.isEmpty()) {
            throw new ApiException("No AI follow-up questions found for this gift plan.");
        }

        Set<Long> submittedQuestionIds = new HashSet<>();
        for (AiQuestionAnswerItemDTOIn item : request.getAnswers()) {
            if (!submittedQuestionIds.add(item.getAiGeneratedQuestionId())) {
                throw new ApiException("Duplicate answer for AI question id " + item.getAiGeneratedQuestionId() + ".");
            }
        }

        for (AiGeneratedQuestion question : planQuestions) {
            if (!submittedQuestionIds.contains(question.getId())) {
                throw new ApiException("Answer required for AI question: " + question.getQuestionText());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (AiQuestionAnswerItemDTOIn item : request.getAnswers()) {
            AiGeneratedQuestion question = aiGeneratedQuestionRepository
                    .findAiGeneratedQuestionById(item.getAiGeneratedQuestionId())
                    .orElseThrow(() -> new ApiException("AI question not found."));
            if (!question.getGiftPlan().getId().equals(giftPlanId)) {
                throw new ApiException("AI question does not belong to this gift plan.");
            }

            AiQuestionAnswer answer = aiQuestionAnswerRepository
                    .findByAiGeneratedQuestion_Id(question.getId())
                    .orElse(null);
            if (answer == null) {
                answer = new AiQuestionAnswer();
            }

            answer.setAiGeneratedQuestion(question);
            answer.setAnswerText(item.getAnswerText());
            if (answer.getCreatedAt() == null) {
                answer.setCreatedAt(now);
            }
            aiQuestionAnswerRepository.save(answer);
        }

        giftPlan.setStatus(GiftPlanStatus.AI_QUESTIONS_ANSWERED);
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return listAnswers(userId, giftPlanId);
    }

    public List<AiQuestionAnswerDTOOut> listAnswers(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        List<AiQuestionAnswer> answers = aiQuestionAnswerRepository
                .findByAiGeneratedQuestion_GiftPlan_IdOrderByCreatedAtAsc(giftPlanId);

        List<AiQuestionAnswerDTOOut> result = new ArrayList<>();
        for (AiQuestionAnswer answer : answers) {
            result.add(toAnswerDto(answer));
        }
        return result;
    }

    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        if (!giftPlan.getRecipient().getUser().getId().equals(userId)) {
            throw new ApiException("Recipient must belong to the gift plan owner.");
        }
        return giftPlan;
    }

    private AiQuestionAnswerDTOOut toAnswerDto(AiQuestionAnswer answer) {
        AiQuestionAnswerDTOOut dto = new AiQuestionAnswerDTOOut();
        dto.setId(answer.getId());
        dto.setAiGeneratedQuestionId(answer.getAiGeneratedQuestion().getId());
        dto.setQuestionText(answer.getAiGeneratedQuestion().getQuestionText());
        dto.setAnswerText(answer.getAnswerText());
        return dto;
    }
}
