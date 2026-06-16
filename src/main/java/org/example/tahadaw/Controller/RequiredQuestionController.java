package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.Model.RequiredQuestion;
import org.example.tahadaw.Service.RequiredQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/required-questions")
@RequiredArgsConstructor
public class RequiredQuestionController {

    private final RequiredQuestionService requiredQuestionService;

    @PostMapping("/add")
    public ResponseEntity<?> addRequiredQuestion(@RequestBody @Valid RequiredQuestion requiredQuestion) {
        requiredQuestionService.addRequiredQuestion(requiredQuestion);
        return ResponseEntity.status(200).body(new ApiResponse("Required question added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRequiredQuestions() {
        return ResponseEntity.status(200).body(requiredQuestionService.getRequiredQuestions());
    }

    @PutMapping("/update/{questionId}")
    public ResponseEntity<?> updateRequiredQuestion(@PathVariable Long questionId, @RequestBody @Valid RequiredQuestion requiredQuestion) {
        requiredQuestionService.updateRequiredQuestion(questionId, requiredQuestion);
        return ResponseEntity.status(200).body(new ApiResponse("Required question updated successfully"));
    }


    @DeleteMapping("/delete/{questionId}")
    public ResponseEntity<?> deleteRequiredQuestion(@PathVariable Long questionId) {
        requiredQuestionService.deleteRequiredQuestion(questionId);
        return ResponseEntity.status(200).body(new ApiResponse("Required question deleted successfully"));
    }
}
