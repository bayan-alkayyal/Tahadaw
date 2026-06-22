package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftHistoryLogDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.GiftHistorySummaryDTOOut;
import org.example.tahadaw.DTO.OUT.SpendingStatsDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-history")
@RequiredArgsConstructor
public class GiftHistoryController {

    private final GiftHistoryService giftHistoryService;

    @PostMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> logFromProduct(@AuthenticationPrincipal User user,
                                                            @PathVariable Long selectedProductId,
                                                            @Valid @RequestBody GiftHistoryLogDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.logFromProduct(user.getId(), selectedProductId, request));
    }

    @PutMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> editLog(@AuthenticationPrincipal User user,
                                                     @PathVariable Long selectedProductId,
                                                     @Valid @RequestBody GiftHistoryLogDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.editLog(user.getId(), selectedProductId, request));
    }

    @DeleteMapping("/from-product/{selectedProductId}")
    public ResponseEntity<ApiResponse> deleteLog(@AuthenticationPrincipal User user,
                                                 @PathVariable Long selectedProductId) {
        giftHistoryService.deleteLog(user.getId(), selectedProductId);
        return ResponseEntity.ok(new ApiResponse("Gift history log deleted."));
    }

    @GetMapping("/from-product/{selectedProductId}")
    public ResponseEntity<GiftHistoryDTOOut> getByProduct(@AuthenticationPrincipal User user,
                                                          @PathVariable Long selectedProductId) {
        return ResponseEntity.ok(giftHistoryService.getByProduct(user.getId(), selectedProductId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftHistoryDTOOut>> listMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftHistoryService.listMine(user.getId()));
    }

    @GetMapping("/summary")
    public ResponseEntity<GiftHistorySummaryDTOOut> summary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftHistoryService.summary(user.getId()));
    }

    @GetMapping("/spending-stats")
    public ResponseEntity<SpendingStatsDTOOut> spendingStats(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(giftHistoryService.spendingStats(user.getId(), from, to));
    }
}
