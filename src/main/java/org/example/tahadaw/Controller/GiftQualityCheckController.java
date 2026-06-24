package org.example.tahadaw.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.OUT.GiftQualityCheckDTOOut;
import org.example.tahadaw.Model.GiftQualityCheck;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftQualityCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-quality-checks")
@RequiredArgsConstructor
public class GiftQualityCheckController {


    private final GiftQualityCheckService giftQualityCheckService;

    @PostMapping("/add/{recipientId}")
    public ResponseEntity<ApiResponse> runGiftQualityCheck(@AuthenticationPrincipal User user,
                                                 @PathVariable Long recipientId,
                                                 @RequestBody @Valid GiftQualityCheck giftQualityCheck) {

        giftQualityCheckService.runGiftQualityCheck(user.getId(), recipientId, giftQualityCheck);
        return ResponseEntity.ok(new ApiResponse("Gift quality check completed successfully"));
    }


    @GetMapping("/recipients/{recipientId}")
    public ResponseEntity<List<GiftQualityCheckDTOOut>> getGiftQualityChecksByRecipient(@PathVariable Long recipientId,
                                                             @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(giftQualityCheckService.getGiftQualityChecksByRecipient(user.getId(), recipientId));
    }

    @GetMapping("/{checkId}")
    public ResponseEntity<GiftQualityCheckDTOOut> getGiftQualityCheckById(@PathVariable Long checkId,
                                                     @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(giftQualityCheckService.getGiftQualityCheckById(user.getId(), checkId));
    }
}
