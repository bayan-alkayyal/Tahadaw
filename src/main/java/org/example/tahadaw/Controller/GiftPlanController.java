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

    // Saud style
    @PostMapping
    public ResponseEntity<GiftPlan> create(@RequestParam Long userId,
                                           @RequestParam Long recipientId,
                                           @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.createGiftPlan(userId, recipientId, request));
    }

    // Shahad style
    @PostMapping("/create/{userId}/{recipientId}")
    public ResponseEntity<?> createByPath(@PathVariable Long userId,
                                          @PathVariable Long recipientId,
                                          @RequestBody @Valid GiftPlanDTOIn request) {
        giftPlanService.createGiftPlan(userId, recipientId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Gift plan created successfully."));
    }

    // Saud style
    @GetMapping
    public ResponseEntity<List<GiftPlan>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(giftPlanService.listByUser(userId));
    }

    // Shahad style
    @GetMapping("/get-my-plans/{userId}")
    public ResponseEntity<List<GiftPlan>> listMineByPath(@PathVariable Long userId) {
        return ResponseEntity.ok(giftPlanService.listByUser(userId));
    }

    @GetMapping("/{giftPlanId}")
    public ResponseEntity<GiftPlan> getOne(@RequestParam Long userId,
                                           @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftPlanService.getGiftPlanById(userId, giftPlanId));
    }

    @PutMapping("/{giftPlanId}")
    public ResponseEntity<GiftPlan> update(@RequestParam Long userId,
                                           @PathVariable Long giftPlanId,
                                           @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.updateGiftPlan(userId, giftPlanId, request));
    }

    @DeleteMapping("/{giftPlanId}")
    public ResponseEntity<Void> delete(@RequestParam Long userId,
                                       @PathVariable Long giftPlanId) {
        giftPlanService.deleteGiftPlan(userId, giftPlanId);
        return ResponseEntity.noContent().build();
    }

    // ===== Required questions / answers =====

    @GetMapping("/{giftPlanId}/required-questions")
    public ResponseEntity<List<RequiredQuestionDTOOut>> listRequiredQuestions(@RequestParam Long userId,
                                                                              @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(requiredQuestionService.listActiveForGiftPlan(userId, giftPlanId));
    }

    @PostMapping("/{giftPlanId}/required-answers")
    public ResponseEntity<List<RequiredQuestionAnswerDTOOut>> submitRequiredAnswers(
            @RequestParam Long userId,
            @PathVariable Long giftPlanId,
            @RequestBody @Valid RequiredQuestionAnswersSubmitDTOIn request) {
        return ResponseEntity.ok(requiredQuestionAnswerService.submitAnswers(userId, giftPlanId, request));
    }

    @GetMapping("/{giftPlanId}/required-answers")
    public ResponseEntity<List<RequiredQuestionAnswerDTOOut>> listRequiredAnswers(@RequestParam Long userId,
                                                                                  @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(requiredQuestionAnswerService.listByGiftPlan(userId, giftPlanId));
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
