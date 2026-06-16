package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.IN.GiftMessageGenerateDTOIn;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.DTO.IN.ProductSelectDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.AiGeneratedQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.GiftMessageDTOOut;
import org.example.tahadaw.DTO.OUT.ProductSearchResultDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductDTOOut;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Service.GiftHistoryService;
import org.example.tahadaw.Service.GiftMessageService;
import org.example.tahadaw.Service.AiQuestionService;
import org.example.tahadaw.Service.GiftPlanService;
import org.example.tahadaw.Service.ProductSearchService;
import org.example.tahadaw.Service.RequiredQuestionAnswerService;
import org.example.tahadaw.Service.RequiredQuestionService;
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
    private final GiftMessageService giftMessageService;
    private final GiftHistoryService giftHistoryService;

    @PostMapping
    public ResponseEntity<GiftPlan> create(@RequestParam Long userId,
                                           @RequestParam Long recipientId,
                                           @RequestBody @Valid GiftPlanDTOIn request) {
        return ResponseEntity.ok(giftPlanService.createGiftPlan(userId, recipientId, request));
    }

    @GetMapping
    public ResponseEntity<List<GiftPlan>> listMine(@RequestParam Long userId) {
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

    @PostMapping("/{giftPlanId}/ai-questions/generate")
    public ResponseEntity<List<AiGeneratedQuestionDTOOut>> generateAiQuestions(@RequestParam Long userId,
                                                                               @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(aiQuestionService.generateQuestions(userId, giftPlanId));
    }

    @GetMapping("/{giftPlanId}/ai-questions")
    public ResponseEntity<List<AiGeneratedQuestionDTOOut>> listAiQuestions(@RequestParam Long userId,
                                                                           @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(aiQuestionService.listQuestions(userId, giftPlanId));
    }

    @PostMapping("/{giftPlanId}/ai-answers")
    public ResponseEntity<List<AiQuestionAnswerDTOOut>> submitAiAnswers(
            @RequestParam Long userId,
            @PathVariable Long giftPlanId,
            @RequestBody @Valid AiQuestionAnswersSubmitDTOIn request) {
        return ResponseEntity.ok(aiQuestionService.submitAnswers(userId, giftPlanId, request));
    }

    @GetMapping("/{giftPlanId}/ai-answers")
    public ResponseEntity<List<AiQuestionAnswerDTOOut>> listAiAnswers(@RequestParam Long userId,
                                                                      @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(aiQuestionService.listAnswers(userId, giftPlanId));
    }

    @PostMapping("/{giftPlanId}/products/search")
    public ResponseEntity<List<ProductSearchResultDTOOut>> searchProducts(@PathVariable Long giftPlanId) {
        return ResponseEntity.ok(productSearchService.searchProducts(giftPlanId));
    }

    @PostMapping("/{giftPlanId}/products/select")
    public ResponseEntity<SelectedProductDTOOut> selectProduct(@PathVariable Long giftPlanId,
                                                               @Valid @RequestBody ProductSelectDTOIn request) {
        return ResponseEntity.ok(productSearchService.selectProduct(giftPlanId, request));
    }

    @GetMapping("/{giftPlanId}/selected-product")
    public ResponseEntity<SelectedProductDTOOut> getSelectedProduct(@PathVariable Long giftPlanId) {
        return ResponseEntity.ok(productSearchService.getSelectedProduct(giftPlanId));
    }

    @PostMapping("/{giftPlanId}/messages/generate")
    public ResponseEntity<GiftMessageDTOOut> generateMessage(@RequestParam Long userId,
                                                             @PathVariable Long giftPlanId,
                                                             @Valid @RequestBody GiftMessageGenerateDTOIn request) {
        return ResponseEntity.ok(giftMessageService.generate(userId, giftPlanId, request));
    }

    @GetMapping("/{giftPlanId}/messages")
    public ResponseEntity<List<GiftMessageDTOOut>> listMessages(@RequestParam Long userId,
                                                                  @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftMessageService.listByGiftPlan(userId, giftPlanId));
    }

    @PostMapping("/{giftPlanId}/history")
    public ResponseEntity<GiftHistoryDTOOut> saveHistoryFromPlan(@RequestParam Long userId,
                                                                 @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftHistoryService.saveFromPlan(userId, giftPlanId));
    }
}
