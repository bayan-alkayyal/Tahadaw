package org.example.tahadaw.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.Model.GiftQualityCheck;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftQualityCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gift-quality-checks")
@RequiredArgsConstructor
public class GiftQualityCheckController {


    private final GiftQualityCheckService giftQualityCheckService;

    @PostMapping("/add/{recipientId}")
    public ResponseEntity<?> runGiftQualityCheck(@AuthenticationPrincipal User user,
                                                 @PathVariable Long recipientId,
                                                 @RequestBody @Valid GiftQualityCheck giftQualityCheck) {

        giftQualityCheckService.runGiftQualityCheck(user.getId(), recipientId, giftQualityCheck);
        return ResponseEntity.status(200).body(new ApiResponse("Gift quality check completed successfully"));
    }


    @GetMapping("/recipients/{recipientId}")
    public ResponseEntity<?> getGiftQualityChecksByRecipient(@PathVariable Long recipientId,
                                                             @AuthenticationPrincipal User user) {

        return ResponseEntity.status(200).body(giftQualityCheckService.getGiftQualityChecksByRecipient(user.getId(), recipientId));
    }

    @GetMapping("/{checkId}")
    public ResponseEntity<?> getGiftQualityCheckById(@PathVariable Long checkId,
                                                     @AuthenticationPrincipal User user) {

        return ResponseEntity.status(200).body(giftQualityCheckService.getGiftQualityCheckById(user.getId(), checkId));
    }
}
