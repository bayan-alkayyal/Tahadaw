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
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Service.*;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/create/{userId}/{recipientId}")
    public ResponseEntity<?> create(@PathVariable Long userId,
                                          @PathVariable Long recipientId,
                                          @RequestBody @Valid GiftPlanDTOIn request) {
        giftPlanService.createGiftPlan(userId, recipientId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Gift plan created successfully."));
    }


    // Shahad
    @GetMapping("/get-my-plans/{userId}")
    public ResponseEntity<List<GiftPlan>> getMyPlans(@PathVariable Long userId) {
        return ResponseEntity.ok(giftPlanService.listByUser(userId));
    }

    @GetMapping("/get-plan-by-id/{userId}/{giftPlanId}")
    public ResponseEntity<GiftPlan> getPlanById(@PathVariable Long userId,
                                           @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftPlanService.getGiftPlanById(userId, giftPlanId));
    }

    @PutMapping("/update/{userId}/{giftPlanId}")
    public ResponseEntity<GiftPlan> update(@PathVariable Long userId,
                                           @PathVariable Long giftPlanId,
                                           @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.updateGiftPlan(userId, giftPlanId, request));
    }

    @DeleteMapping("/delete/{userId}/{giftPlanId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long giftPlanId) {
        giftPlanService.deleteGiftPlan(userId, giftPlanId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-active-plans/{userid}")
    public ResponseEntity<List<GiftPlan>> getActivePlans(@PathVariable Long userid) {
        return ResponseEntity.status(200).body(giftPlanService.listAllActiveGiftPlans(userid));
    }
    @GetMapping("/get-previous-plans/{userid}")
    public ResponseEntity<List<GiftPlan>> getPreviousPlans(@PathVariable Long userid) {
        return ResponseEntity.status(200).body(giftPlanService.listAllPreviousGiftPlans(userid));
    }

    @GetMapping("/get-gift-plan-Summery/{userId}/{giftPlanId}")
    public ResponseEntity<?> getGiftPlanSummery(@PathVariable Long userId,@PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(giftPlanService.getGiftPlanSummary(userId, giftPlanId));
    }


    // ===== Surprise plan (Saud) =====

    @PostMapping("/{giftPlanId}/surprise-plan/generate")
    public ResponseEntity<SurprisePlanDTOOut> generateSurprisePlan(
            @RequestParam Long userId,
            @PathVariable Long giftPlanId,
            @RequestBody(required = false) SurprisePlanGenerateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.generate(userId, giftPlanId, request));
    }

    @PostMapping("/{giftPlanId}/surprise-plan/regenerate")
    public ResponseEntity<SurprisePlanDTOOut> regenerateSurprisePlan(
            @RequestParam Long userId,
            @PathVariable Long giftPlanId,
            @RequestBody(required = false) SurprisePlanGenerateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.regenerate(userId, giftPlanId, request));
    }

    @PutMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<SurprisePlanDTOOut> updateSurprisePlan(
            @RequestParam Long userId,
            @PathVariable Long giftPlanId,
            @Valid @RequestBody SurprisePlanUpdateDTOIn request) {
        return ResponseEntity.ok(surprisePlanService.update(userId, giftPlanId, request));
    }

    @DeleteMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<ApiResponse> deleteSurprisePlan(@RequestParam Long userId,
                                                          @PathVariable Long giftPlanId) {
        surprisePlanService.delete(userId, giftPlanId);
        return ResponseEntity.ok(new ApiResponse("Surprise plan deleted."));
    }

    @GetMapping("/{giftPlanId}/surprise-plan")
    public ResponseEntity<SurprisePlanDTOOut> getSurprisePlan(@RequestParam Long userId,
                                                              @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(surprisePlanService.getByGiftPlan(userId, giftPlanId));
    }
}
