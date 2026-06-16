package org.example.tahadaw.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.Service.GiftHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final GiftHistoryService giftHistoryService;

    @GetMapping("/{recipientId}/gift-history")
    public ResponseEntity<List<GiftHistoryDTOOut>> listGiftHistory(@RequestParam Long userId,
                                                                    @PathVariable Long recipientId) {
        return ResponseEntity.ok(giftHistoryService.listByRecipient(userId, recipientId));
    }
}
