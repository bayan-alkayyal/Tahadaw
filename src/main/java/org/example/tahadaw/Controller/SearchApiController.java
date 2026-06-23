package org.example.tahadaw.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.SearchApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchApiController {
    private final SearchApiService searchApiService;

    @GetMapping("/gift-plans/{giftPlanId}/products")
    public ResponseEntity<?> search(@AuthenticationPrincipal User user, @PathVariable Long giftPlanId) {
        return ResponseEntity.status(200).body(searchApiService.search(user.getId(), giftPlanId));
    }
}
