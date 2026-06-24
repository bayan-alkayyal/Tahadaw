package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerItemDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDetailDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.GiftPlanStatus;
import org.example.tahadaw.Model.RequiredQuestion;
import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.example.tahadaw.Repository.RequiredQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequiredQuestionAnswerService {

    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final RequiredQuestionRepository requiredQuestionRepository;
    private final GiftPlanRepository giftPlanRepository;



    //shahad-CRUD

    public void createRequiredQuestionAnswer(Long requiredQuestionId, RequiredQuestionAnswerDTOIn request) {
        RequiredQuestion requiredQuestion = requiredQuestionRepository.findRequiredQuestionById(requiredQuestionId)
                .orElseThrow(() -> new ApiException("required question not found."));

        RequiredQuestionAnswer requiredQuestionAnswer = new RequiredQuestionAnswer();
        requiredQuestionAnswer.setAnswerText(request.getAnswerText());
        requiredQuestionAnswer.setRequiredQuestion(requiredQuestion);

        requiredQuestionAnswerRepository.save(requiredQuestionAnswer);

    }

    public List<RequiredQuestionAnswerDetailDTOOut> getAllRequiredQuestionAnswer() {
        return requiredQuestionAnswerRepository.findAll().stream()
                .map(ResponseMapper::toRequiredQuestionAnswerDetailDto)
                .toList();
    }

    public void updateRequiredQuestionAnswer(long id, RequiredQuestionAnswerDTOIn request) {
        RequiredQuestionAnswer oldRequiredQuestionAnswer = getRequiredQuestionAnswerEntityById(id);
        oldRequiredQuestionAnswer.setAnswerText(request.getAnswerText());
        requiredQuestionAnswerRepository.save(oldRequiredQuestionAnswer);

    }

    public void deleteRequiredQuestionAnswer(Long id) {
        RequiredQuestionAnswer oldRequiredQuestionAnswer = getRequiredQuestionAnswerEntityById(id);
        requiredQuestionAnswerRepository.delete(oldRequiredQuestionAnswer);
    }


    public RequiredQuestionAnswerDetailDTOOut getRequiredQuestionAnswerById(Long id) {
        return ResponseMapper.toRequiredQuestionAnswerDetailDto(getRequiredQuestionAnswerEntityById(id));
    }

    private RequiredQuestionAnswer getRequiredQuestionAnswerEntityById(Long id) {
        return requiredQuestionAnswerRepository.findRequiredQuestionAnswerById(id)
                .orElseThrow(() -> new ApiException("the answer for required question not found."));
    }


    @Transactional
    public List<RequiredQuestionAnswerDTOOut> submitAnswers(Long userId, Long giftPlanId,
                                                            RequiredQuestionAnswersSubmitDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (!GiftPlanStatus.CREATED.equals(giftPlan.getStatus())
                && !GiftPlanStatus.REQUIRED_QUESTIONS_ANSWERED.equals(giftPlan.getStatus())) {
            throw new ApiException("Required answers can only be submitted before AI questions are generated.");
        }
        //getting the requierd Q that are active
        List<RequiredQuestion> activeQuestions = requiredQuestionRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        if (activeQuestions.isEmpty()) {
            throw new ApiException("No active required questions are configured.");
        }

        //make sure that no duplicate question is submitted
        Set<Long> submittedQuestionIds = new HashSet<>();
        for (RequiredQuestionAnswerItemDTOIn item : request.getAnswers()) {
            if (!submittedQuestionIds.add(item.getRequiredQuestionId())) {
                throw new ApiException("Duplicate answer for required question id " + item.getRequiredQuestionId() + ".");
            }
        }
        //make sure that all questions are answerd
        for (RequiredQuestion question : activeQuestions) {
            if (!submittedQuestionIds.contains(question.getId())) {
                throw new ApiException("Answer required for question: " + question.getQuestionText());
            }
        }

        //make sure that the answer for active questions only
        for (RequiredQuestionAnswerItemDTOIn item : request.getAnswers()) {
            RequiredQuestion question = requiredQuestionRepository.findRequiredQuestionById(item.getRequiredQuestionId())
                    .orElseThrow(() -> new ApiException("Required question not found."));
            if (!Boolean.TRUE.equals(question.getIsActive())) {
                throw new ApiException("Required question is not active: " + question.getId());
            }

            RequiredQuestionAnswer answer = requiredQuestionAnswerRepository
                    .findByGiftPlan_IdAndRequiredQuestion_Id(giftPlanId, question.getId())
                    .orElse(null);

            if (answer == null) {
                answer = new RequiredQuestionAnswer();
            }

            answer.setGiftPlan(giftPlan);
            answer.setRequiredQuestion(question);
            answer.setAnswerText(item.getAnswerText());
            if (answer.getCreatedAt() == null) {
                answer.setCreatedAt(LocalDateTime.now());
            }
            requiredQuestionAnswerRepository.save(answer);
        }

        giftPlan.setStatus(GiftPlanStatus.REQUIRED_QUESTIONS_ANSWERED);
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);

        return listByGiftPlan(userId, giftPlanId);
    }

    public List<RequiredQuestionAnswerDTOOut> listByGiftPlan(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        List<RequiredQuestionAnswer> answers = requiredQuestionAnswerRepository
                .findByGiftPlan_IdOrderByCreatedAtAsc(giftPlanId);

        List<RequiredQuestionAnswerDTOOut> result = new ArrayList<>();
        for (RequiredQuestionAnswer answer : answers) {
            result.add(toDto(answer));
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

    private RequiredQuestionAnswerDTOOut toDto(RequiredQuestionAnswer answer) {
        return new RequiredQuestionAnswerDTOOut(
                answer.getId(),
                answer.getRequiredQuestion().getId(),
                answer.getRequiredQuestion().getQuestionText(),
                answer.getAnswerText()
        );
    }
}
