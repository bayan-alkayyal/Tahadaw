package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.IN.SurprisePlanGenerateDTOIn;
import org.example.tahadaw.DTO.IN.SurprisePlanUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.AiGeneratedQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductDTOOut;
import org.example.tahadaw.DTO.OUT.SurprisePlanDTOOut;
import org.example.tahadaw.DTO.OUT.GiftPlanDTOOut;
import org.example.tahadaw.DTO.OUT.GiftPlanSummeryDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-plans")
@RequiredArgsConstructor
public class GiftPlanController {

    private final GiftPlanService giftPlanService;
    private final RequiredQuestionService requiredQuestionService;
    private final RequiredQuestionAnswerService requiredQuestionAnswerService;
    private final AiQuestionService aiQuestionService;
    private final ProductSearchService productSearchService;
    private final SurprisePlanService surprisePlanService;
    private final GiftRecommendationService giftRecommendationService;

    // ===== Gift plan CRUD =====
    // NOTE: both endpoint styles kept after the Shahad merge; API design to be unified later.


    // Shahad
    @PostMapping("/create/{recipientId}")
    public ResponseEntity<GiftPlanDTOOut> create(@AuthenticationPrincipal User user,
                                                 @PathVariable Long recipientId,
                                                 @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.createGiftPlan(user.getId(), recipientId, request));
    }


    // Shahad
    @GetMapping("/get-my-plans")
    public ResponseEntity<List<GiftPlanDTOOut>> getMyPlans(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftPlanService.listByUser(user.getId()));
    }

    @GetMapping("/get-plan-by-id/{giftPlanId}")
    public ResponseEntity<GiftPlanDTOOut> getPlanById(@AuthenticationPrincipal User user,
                                           @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftPlanService.getGiftPlanById(user.getId(), giftPlanId));
    }

    @PutMapping("/update/{giftPlanId}")
    public ResponseEntity<GiftPlanDTOOut> update(@AuthenticationPrincipal User user,
                                           @PathVariable Long giftPlanId,
                                           @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.updateGiftPlan(user.getId(), giftPlanId, request));
    }

    @DeleteMapping("/delete/{giftPlanId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user,
                                       @PathVariable Long giftPlanId) {
        giftPlanService.deleteGiftPlan(user.getId(), giftPlanId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-active-plans")
    public ResponseEntity<List<GiftPlanDTOOut>> getActivePlans(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftPlanService.listAllActiveGiftPlans(user.getId()));
    }
    @GetMapping("/get-previous-plans")
    public ResponseEntity<List<GiftPlanDTOOut>> getPreviousPlans(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftPlanService.listAllPreviousGiftPlans(user.getId()));
    }

    @GetMapping("/get-gift-plan-Summery/{giftPlanId}")
    public ResponseEntity<GiftPlanSummeryDTOOut> getGiftPlanSummery(@AuthenticationPrincipal User user,@PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftPlanService.getGiftPlanSummary(user.getId(), giftPlanId));
    }


    // ===== Surprise plan (Saud) =====

    @PostMapping("/{giftPlanId}/surprise-plan/generate")
    public ResponseEntity<SurprisePlanDTOOut> generateSurprisePlan(
            @AuthenticationPrincipal User user,
            @PathVariable Long giftPlanId,
            @RequestBody(required = false) SurprisePlanGenerateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.generate(user.getId(), giftPlanId, request));
    }

    @PostMapping("/{giftPlanId}/surprise-plan/regenerate")
    public ResponseEntity<SurprisePlanDTOOut> regenerateSurprisePlan(
            @AuthenticationPrincipal User user,
            @PathVariable Long giftPlanId,
            @RequestBody(required = false) SurprisePlanGenerateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.regenerate(user.getId(), giftPlanId, request));
    }

    @PutMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<SurprisePlanDTOOut> updateSurprisePlan(
            @AuthenticationPrincipal User user,
            @PathVariable Long giftPlanId,
            @Valid @RequestBody SurprisePlanUpdateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.update(user.getId(), giftPlanId, request));
    }

    @DeleteMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<ApiResponse> deleteSurprisePlan(@AuthenticationPrincipal User user,
                                                          @PathVariable Long giftPlanId) {
        surprisePlanService.delete(user.getId(), giftPlanId);
        return ResponseEntity.ok(new ApiResponse("Surprise plan deleted."));
    }

    @GetMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<SurprisePlanDTOOut> getSurprisePlan(@AuthenticationPrincipal User user,
                                                              @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(surprisePlanService.getByGiftPlan(user.getId(), giftPlanId));
    }
}
