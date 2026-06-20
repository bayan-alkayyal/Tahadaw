package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.Service.RequiredQuestionAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/required-questions-answer")
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
    public ResponseEntity<?> getAllRequiredQuestionAnswers() {
        return ResponseEntity.status(200).body(requiredQuestionAnswerService.getAllRequiredQuestionAnswer());
    }

    // READ - by id
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<?> getRequiredQuestionAnswerById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(requiredQuestionAnswerService.getRequiredQuestionAnswerById(id));
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

    @PostMapping("/submit-required-answers/{userId}/{giftPlanId}")
    public ResponseEntity<?> submitRequiredAnswers(
            @PathVariable Long userId,
            @PathVariable Long giftPlanId,
            @RequestBody @Valid RequiredQuestionAnswersSubmitDTOIn request) {
        requiredQuestionAnswerService.submitAnswers(userId, giftPlanId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Answers for required question submitted successfully"));
    }

    @GetMapping("/list-required-answers/{userId}/{giftPlanId}")
    public ResponseEntity<List<RequiredQuestionAnswerDTOOut>> listRequiredAnswers(@PathVariable Long userId,
                                                                                  @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(requiredQuestionAnswerService.listByGiftPlan(userId, giftPlanId));
    }
}
