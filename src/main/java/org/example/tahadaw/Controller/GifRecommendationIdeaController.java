package org.example.tahadaw.Controller;


import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gift-recommendations")
@RequiredArgsConstructor
public class GifRecommendationIdeaController {

    private final GiftRecommendationService giftRecommendationService;

    // ===== Gift idea recommendations (Shahad) =====

    @PutMapping("/{recommendationId}/select")
    public ResponseEntity<?> selectRecommendation(@AuthenticationPrincipal User user, @PathVariable Long recommendationId) {
        giftRecommendationService.selectRecommendation(user.getId(), recommendationId);
        return ResponseEntity.status(200).body(new ApiResponse("Recommendation selected successfully."));
    }

    @GetMapping("/gift-plans/{giftPlanId}")
    public ResponseEntity<?> getRecommendation(@AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(giftRecommendationService.generateGiftRecommendation(user.getId(), giftPlanId));
    }

    @PutMapping("/{recommendationId}/unselect")
    public ResponseEntity<?> unselectRecommendation(@AuthenticationPrincipal User user, @PathVariable Long recommendationId) {
        giftRecommendationService.unselectRecommendation(user.getId(), recommendationId);
        return ResponseEntity.status(200).body(new ApiResponse("Recommendation unselected successfully."));
    }

    @GetMapping("/gift-plans/{giftPlanId}/regenerate")
    public ResponseEntity<?> regenerateRecommendation(@AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(giftRecommendationService.regenerateGiftRecommendation(user.getId(), giftPlanId));
    }

    @GetMapping("/gift-plans/{giftPlanId}/selected")
    public ResponseEntity<?> getSelectedIdea(@AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(giftRecommendationService.getSelectedRecommendation(user.getId(), giftPlanId));
    }


}
