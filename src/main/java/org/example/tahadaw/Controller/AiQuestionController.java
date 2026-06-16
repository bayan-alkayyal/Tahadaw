package org.example.tahadaw.Controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.AiGeneratedQuestionDTOIn;
import org.example.tahadaw.Service.AiQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-questions")
@RequiredArgsConstructor
public class AiQuestionController {

    private final AiQuestionService aiQuestionService;

    // CREATE
    @PostMapping("/gift-plan/{giftPlanId}")
    public ResponseEntity<?> createAiQuestion(@PathVariable Long giftPlanId,
                                                 @RequestBody @Valid AiGeneratedQuestionDTOIn request) {
        aiQuestionService.createAiQuestion(giftPlanId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Ai question created successfully."));
    }

    // READ - all
    @GetMapping("/get")
    public ResponseEntity<?> getAllAiQuestions() {
        return ResponseEntity.status(200).body(aiQuestionService.getAllAiQuestion());
    }

    // READ - by id
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<?> getAiQuestionById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(aiQuestionService.getAiQuestionById(id));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAiQuestion(@PathVariable Long id,
                                                 @RequestBody @Valid AiGeneratedQuestionDTOIn request) {
        aiQuestionService.updateAiQuestion(id, request);
        return ResponseEntity.status(200).body(new ApiResponse("Ai question updated successfully."));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAiQuestion(@PathVariable Long id) {
        aiQuestionService.deleteAiQuestion(id);
        return ResponseEntity.status(200).body(new ApiResponse("Ai question deleted successfully."));
    }
}
