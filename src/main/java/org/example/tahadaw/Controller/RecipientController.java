package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.RecipientInsightsDTOOut;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftHistoryService;
import org.example.tahadaw.Service.RecipientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final GiftHistoryService giftHistoryService;

    @PostMapping("/add")
    public ResponseEntity<?> addRecipient(@AuthenticationPrincipal User user, @RequestBody @Valid Recipient recipient) {
        recipientService.addRecipient(user.getId(), recipient);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRecipients(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(200).body(recipientService.getRecipientsByUserId(user.getId()));
    }

    @PutMapping("/update/{recipientId}")
    public ResponseEntity<?> updateRecipient(@AuthenticationPrincipal User user , @PathVariable Long recipientId, @RequestBody @Valid Recipient recipient) {
        recipientService.updateRecipient(user.getId(),recipientId, recipient);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient updated successfully"));
    }

    @DeleteMapping("/delete/{recipientId}")
    public ResponseEntity<?> deleteRecipient(@AuthenticationPrincipal User user , @PathVariable Long recipientId) {
        recipientService.deleteRecipient(user.getId(),recipientId);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient deleted successfully"));
    }

    @GetMapping("/{recipientId}/gift-history")
    public ResponseEntity<List<GiftHistoryDTOOut>> listGiftHistory(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long recipientId) {
        return ResponseEntity.ok(giftHistoryService.listByRecipient(user.getId(), recipientId));
    }

    @GetMapping("/{recipientId}/insights")
    public ResponseEntity<RecipientInsightsDTOOut> insights(@AuthenticationPrincipal User user,
                                                            @PathVariable Long recipientId) {
        return ResponseEntity.ok(giftHistoryService.recipientInsights(user.getId(), recipientId));
    }

    @GetMapping("/get-by-user-id")
    public ResponseEntity<?> getRecipientsByUserId(@AuthenticationPrincipal User user){
        return ResponseEntity.status(200).body(recipientService.getRecipientsByUserId(user.getId()));
    }

    @GetMapping("/get/{recipientId}")
    public ResponseEntity<?> getRecipientByIdAndUserId(@AuthenticationPrincipal User user,
                                                       @PathVariable Long recipientId) {

        return ResponseEntity.status(200).body(recipientService.getRecipientByIdAndUserId(user.getId(), recipientId));
    }
}
