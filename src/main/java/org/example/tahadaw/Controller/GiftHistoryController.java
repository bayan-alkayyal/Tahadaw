package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftHistoryLogDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.GiftHistorySummaryDTOOut;
import org.example.tahadaw.Service.GiftHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-history")
@RequiredArgsConstructor
public class GiftHistoryController {

    private final GiftHistoryService giftHistoryService;

    @PostMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> logFromProduct(@RequestParam Long userId,
                                                            @PathVariable Long selectedProductId,
                                                            @Valid @RequestBody GiftHistoryLogDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.logFromProduct(userId, selectedProductId, request));
    }

    @PutMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> editLog(@RequestParam Long userId,
                                                     @PathVariable Long selectedProductId,
                                                     @Valid @RequestBody GiftHistoryLogDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.editLog(userId, selectedProductId, request));
    }

    @DeleteMapping("/from-product/{selectedProductId}")
    public ResponseEntity<ApiResponse> deleteLog(@RequestParam Long userId,
                                                 @PathVariable Long selectedProductId) {
        giftHistoryService.deleteLog(userId, selectedProductId);
        return ResponseEntity.ok(new ApiResponse("Gift history log deleted."));
    }

    @GetMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> getByProduct(@RequestParam Long userId,
                                                          @PathVariable Long selectedProductId) {
        return ResponseEntity.ok(giftHistoryService.getByProduct(userId, selectedProductId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftHistoryDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(giftHistoryService.listMine(userId));
    }

    @GetMapping("/summary")
    public ResponseEntity<GiftHistorySummaryDTOOut> summary(@RequestParam Long userId) {
        return ResponseEntity.ok(giftHistoryService.summary(userId));
    }
}
