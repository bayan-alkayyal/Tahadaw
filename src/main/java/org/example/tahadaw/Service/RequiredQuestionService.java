package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.RequiredQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionDetailDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.RequiredQuestion;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.example.tahadaw.Repository.RequiredQuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequiredQuestionService {

    private final RequiredQuestionRepository requiredQuestionRepository;
    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final GiftPlanRepository giftPlanRepository;

    //Bayan CRUD
    public void addRequiredQuestion(RequiredQuestion requiredQuestion) {
        requiredQuestion.setIsActive(true);
        requiredQuestion.setCreatedAt(LocalDateTime.now());
        requiredQuestion.setUpdatedAt(LocalDateTime.now());
        requiredQuestionRepository.save(requiredQuestion);
    }

    public List<RequiredQuestionDetailDTOOut> getRequiredQuestions() {
        return requiredQuestionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(ResponseMapper::toRequiredQuestionDetailDto)
                .toList();
    }

    public void updateRequiredQuestion(Long questionId, RequiredQuestion requiredQuestion) {
        RequiredQuestion oldQuestion = requiredQuestionRepository.findRequiredQuestionById(questionId)
                .orElseThrow(() -> new ApiException("Required question not found"));

        oldQuestion.setQuestionText(requiredQuestion.getQuestionText());
        oldQuestion.setQuestionType(requiredQuestion.getQuestionType());
        oldQuestion.setDisplayOrder(requiredQuestion.getDisplayOrder());
        oldQuestion.setUpdatedAt(LocalDateTime.now());
        requiredQuestionRepository.save(oldQuestion);
    }


    public void deleteRequiredQuestion(Long questionId) {
        RequiredQuestion requiredQuestion = requiredQuestionRepository.findRequiredQuestionById(questionId)
                .orElseThrow(() -> new ApiException("Required question not found"));

        if (requiredQuestionAnswerRepository.existsByRequiredQuestion_Id(questionId)) {
            throw new ApiException("Cannot delete a required question that has answers. Disable it instead.");
        }

        requiredQuestionRepository.delete(requiredQuestion);
    }


    //Bayan
    public void disableRequiredQuestion(Long questionId){
        RequiredQuestion requiredQuestion = requiredQuestionRepository.findRequiredQuestionById(questionId)
                .orElseThrow(() -> new ApiException("Required question not found"));

        requiredQuestion.setIsActive(false);
        requiredQuestion.setUpdatedAt(LocalDateTime.now());

        requiredQuestionRepository.save(requiredQuestion);
    }

    public List<RequiredQuestionDTOOut> listActiveForGiftPlan(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        return requiredQuestionRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .toList();
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

    private RequiredQuestionDTOOut toDto(RequiredQuestion question) {
        return new RequiredQuestionDTOOut(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getDisplayOrder()
        );
    }


}
