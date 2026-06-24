package org.example.tahadaw.Controller;


import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.OUT.GiftIdeaRecommendationDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-recommendations")
@RequiredArgsConstructor
public class GifRecommendationIdeaController {

    private final GiftRecommendationService giftRecommendationService;

    @PutMapping("/{recommendationId}/select")
    public ResponseEntity<ApiResponse> selectRecommendation(@AuthenticationPrincipal User user,
                                                              @PathVariable Long recommendationId) {
        giftRecommendationService.selectRecommendation(user.getId(), recommendationId);
        return ResponseEntity.ok(new ApiResponse("Recommendation selected successfully."));
    }

    @GetMapping("/gift-plans/{giftPlanId}")
    public ResponseEntity<List<GiftIdeaRecommendationDTOOut>> listRecommendations(
            @AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftRecommendationService.listRecommendations(user.getId(), giftPlanId));
    }

    @PostMapping("/gift-plans/{giftPlanId}/generate")
    public ResponseEntity<List<GiftIdeaRecommendationDTOOut>> generateRecommendations(
            @AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftRecommendationService.generateGiftRecommendation(user.getId(), giftPlanId));
    }

    @PutMapping("/{recommendationId}/unselect")
    public ResponseEntity<ApiResponse> unselectRecommendation(@AuthenticationPrincipal User user,
                                                               @PathVariable Long recommendationId) {
        giftRecommendationService.unselectRecommendation(user.getId(), recommendationId);
        return ResponseEntity.ok(new ApiResponse("Recommendation unselected successfully."));
    }

    @PostMapping("/gift-plans/{giftPlanId}/regenerate")
    public ResponseEntity<List<GiftIdeaRecommendationDTOOut>> regenerateRecommendation(
            @AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftRecommendationService.regenerateGiftRecommendation(user.getId(), giftPlanId));
    }

    @GetMapping("/gift-plans/{giftPlanId}/selected")
    public ResponseEntity<GiftIdeaRecommendationDTOOut> getSelectedIdea(
            @AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.ok(giftRecommendationService.getSelectedRecommendation(user.getId(), giftPlanId));
    }
}
