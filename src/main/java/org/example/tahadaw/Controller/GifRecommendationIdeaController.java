package org.example.tahadaw.Controller;


import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.Service.GiftRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gif-recommendation")
@RequiredArgsConstructor
public class GifRecommendationIdeaController {

    private final GiftRecommendationService giftRecommendationService;

    // ===== Gift idea recommendations (Shahad) =====

    @PutMapping("/select-Recomendation/{userId}/{recommendationId}")
    public ResponseEntity<?> selectRecommendation(@PathVariable Long userId, @PathVariable Long recommendationId) {
        giftRecommendationService.selectRecommendation(userId, recommendationId);
        return ResponseEntity.status(200).body(new ApiResponse("Recommendation selected successfully."));
    }

    @GetMapping("/get-Recomendation/{userId}/{giftId}")
    public ResponseEntity<?> getRecommendation(@PathVariable Long userId, @PathVariable Long giftId) {
        return ResponseEntity.status(200).body(giftRecommendationService.generateGiftRecommendation(userId, giftId));
    }

    @GetMapping("/regenerate-Recomendation/{userId}/{giftId}")
    public ResponseEntity<?> regenerateRecommendation(@PathVariable Long userId, @PathVariable Long giftId) {
        return ResponseEntity.status(200).body(giftRecommendationService.regenerateGiftRecommendation(userId, giftId));
    }

    @GetMapping("/get-selected-idea/{giftplanId}")
    public ResponseEntity<?> getSelectedIdea(@PathVariable Long giftplanId){
        return ResponseEntity.status(200).body(giftRecommendationService.getSelectedRecommendation(giftplanId));
    }


}
