package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDetailDTOOut;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.RequiredQuestionAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/required-question-answers")
@RequiredArgsConstructor
public class RequiredQuestionAnswerController {

    private final RequiredQuestionAnswerService requiredQuestionAnswerService;

    // CREATE
    @PostMapping("/required-question/{requiredQuestionId}")
    public ResponseEntity<?> createRequiredQuestionAnswer(@PathVariable Long requiredQuestionId,
                                                          @RequestBody @Valid RequiredQuestionAnswerDTOIn request) {
        requiredQuestionAnswerService.createRequiredQuestionAnswer(requiredQuestionId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Required question answer created successfully."));
    }

    // READ - all
    @GetMapping("/get")
    public ResponseEntity<List<RequiredQuestionAnswerDetailDTOOut>> getAllRequiredQuestionAnswers() {
        return ResponseEntity.ok(requiredQuestionAnswerService.getAllRequiredQuestionAnswer());
    }

    // READ - by id
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<RequiredQuestionAnswerDetailDTOOut> getRequiredQuestionAnswerById(@PathVariable Long id) {
        return ResponseEntity.ok(requiredQuestionAnswerService.getRequiredQuestionAnswerById(id));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRequiredQuestionAnswer(@PathVariable Long id,
                                                          @RequestBody @Valid RequiredQuestionAnswerDTOIn request) {
        requiredQuestionAnswerService.updateRequiredQuestionAnswer(id, request);
        return ResponseEntity.status(200).body(new ApiResponse("Required question answer updated successfully."));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRequiredQuestionAnswer(@PathVariable Long id) {
        requiredQuestionAnswerService.deleteRequiredQuestionAnswer(id);
        return ResponseEntity.status(200).body(new ApiResponse("Required question answer deleted successfully."));
    }

    @PostMapping("/gift-plans/{giftPlanId}/submit")
    public ResponseEntity<?> submitRequiredAnswers(
            @AuthenticationPrincipal User user,
            @PathVariable Long giftPlanId,
            @RequestBody @Valid RequiredQuestionAnswersSubmitDTOIn request) {
        requiredQuestionAnswerService.submitAnswers(user.getId(), giftPlanId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Answers for required question submitted successfully"));
    }

    @GetMapping("/gift-plans/{giftPlanId}")
    public ResponseEntity<List<RequiredQuestionAnswerDTOOut>> listRequiredAnswers(@AuthenticationPrincipal User user,
                                                                                  @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(requiredQuestionAnswerService.listByGiftPlan(user.getId(), giftPlanId));
    }
}
