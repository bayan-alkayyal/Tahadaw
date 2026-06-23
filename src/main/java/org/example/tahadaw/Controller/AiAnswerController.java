package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.AiQuestionAnswerDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.AiAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-answers")
@RequiredArgsConstructor
public class AiAnswerController {

    private final AiAnswerService aiAnswerService;

    // CREATE
    @PostMapping("/ai-question/{aiQuestionId}")
    public ResponseEntity<?> createAiAnswer(@PathVariable Long aiQuestionId,
                                               @RequestBody @Valid AiQuestionAnswerDTOIn request) {
        aiAnswerService.createAiAnswer(aiQuestionId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Ai answer created successfully."));
    }

    // READ - all
    @GetMapping("/get")
    public ResponseEntity<?> getAllAiAnswers() {
        return ResponseEntity.status(200).body(aiAnswerService.getAllAiAnswer());
    }

    // READ - by id
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<?> getAiAnswerById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(aiAnswerService.getAiQuestionAnswerById(id));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAiAnswer(@PathVariable Long id,
                                               @RequestBody @Valid AiQuestionAnswerDTOIn request) {
        aiAnswerService.updateAiQuestionAnswer(id, request);
        return ResponseEntity.status(200).body(new ApiResponse("Ai answer updated successfully."));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAiAnswer(@PathVariable Long id) {
        aiAnswerService.deleteAiQuestionAnswer(id);
        return ResponseEntity.status(200).body(new ApiResponse("Ai answer deleted successfully."));
    }

    // Shahad
    @PostMapping("/gift-plans/{giftPlanId}")
    public ResponseEntity<List<AiQuestionAnswerDTOOut>> submitAiAnswersByPath(
            @AuthenticationPrincipal User user,
            @PathVariable Long giftPlanId,
            @RequestBody @Valid AiQuestionAnswersSubmitDTOIn request) {
        return ResponseEntity.status(200).body(aiAnswerService.submitAnswers(user.getId(), giftPlanId, request));
    }

    // Shahad style
    @GetMapping("/gift-plans/{giftPlanId}")
    public ResponseEntity<List<AiQuestionAnswerDTOOut>> listAiAnswers(@AuthenticationPrincipal User user,
                                                                            @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(aiAnswerService.listAnswers(user.getId(), giftPlanId));
    }
}
