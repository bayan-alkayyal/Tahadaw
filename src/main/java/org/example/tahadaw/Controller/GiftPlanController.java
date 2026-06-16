package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.Service.GiftPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gift-plans")
@RequiredArgsConstructor
public class GiftPlanController {

    private final GiftPlanService giftPlanService;

    // CREATE
    @PostMapping("/create-plan/{userId}/{recipientId}")
    public ResponseEntity<?> createGiftPlan(@PathVariable Long userId,
                                            @PathVariable Long recipientId,
                                            @RequestBody @Valid GiftPlanDTOIn request) {
        giftPlanService.createGiftPlan(userId, recipientId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Gift plan created successfully."));
    }

    // READ - all
    @GetMapping("/get")
    public ResponseEntity<?> getAllGiftPlans() {
        return ResponseEntity.status(200).body(giftPlanService.getAllGiftPlan());
    }

    // READ - by id
    @GetMapping("/get-gift-plan-by-id/{id}")
    public ResponseEntity<?> getGiftPlanById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(giftPlanService.getGiftPlanById(id));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateGiftPlan(@PathVariable Long id, @RequestBody @Valid GiftPlanDTOIn request) {
        giftPlanService.updateGiftPlan(id, request);
        return ResponseEntity.status(200).body(new ApiResponse("Gift plan updated successfully."));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGiftPlan(@PathVariable Long id) {
        giftPlanService.deleteGiftPlan(id);
        return ResponseEntity.status(200).body(new ApiResponse("Gift plan deleted successfully."));
    }
}
