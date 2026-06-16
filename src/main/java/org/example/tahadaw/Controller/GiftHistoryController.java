package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftHistoryCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftHistoryUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.Service.GiftHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-history")
@RequiredArgsConstructor
public class GiftHistoryController {

    private final GiftHistoryService giftHistoryService;

    @PostMapping
    public ResponseEntity<GiftHistoryDTOOut> create(@RequestParam Long userId,
                                                    @Valid @RequestBody GiftHistoryCreateDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.create(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftHistoryDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(giftHistoryService.listMine(userId));
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<GiftHistoryDTOOut> getOne(@RequestParam Long userId,
                                                    @PathVariable Long historyId) {
        return ResponseEntity.ok(giftHistoryService.getOne(userId, historyId));
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<GiftHistoryDTOOut> update(@RequestParam Long userId,
                                                  @PathVariable Long historyId,
                                                  @Valid @RequestBody GiftHistoryUpdateDTOIn request) {
        return ResponseEntity.ok(giftHistoryService.update(userId, historyId, request));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<ApiResponse> delete(@RequestParam Long userId,
                                              @PathVariable Long historyId) {
        giftHistoryService.delete(userId, historyId);
        return ResponseEntity.ok(new ApiResponse("Gift history deleted."));
    }
}
