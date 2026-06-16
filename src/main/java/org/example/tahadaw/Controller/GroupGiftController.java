package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftDTOOut;
import org.example.tahadaw.Service.GroupGiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/group-gifts")
@RequiredArgsConstructor
public class GroupGiftController {

    private final GroupGiftService groupGiftService;

    @PostMapping
    public ResponseEntity<GroupGiftDTOOut> create(@RequestParam Long userId,
                                                  @Valid @RequestBody GroupGiftCreateDTOIn request) {
        return ResponseEntity.ok(groupGiftService.create(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupGiftDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(groupGiftService.listMine(userId));
    }

    @GetMapping("/{groupGiftId}")
    public ResponseEntity<GroupGiftDTOOut> getOne(@RequestParam Long userId,
                                                @PathVariable Long groupGiftId) {
        return ResponseEntity.ok(groupGiftService.getOne(userId, groupGiftId));
    }

    @PutMapping("/{groupGiftId}")
    public ResponseEntity<GroupGiftDTOOut> update(@RequestParam Long userId,
                                                  @PathVariable Long groupGiftId,
                                                  @Valid @RequestBody GroupGiftUpdateDTOIn request) {
        return ResponseEntity.ok(groupGiftService.update(userId, groupGiftId, request));
    }

    @DeleteMapping("/{groupGiftId}")
    public ResponseEntity<ApiResponse> delete(@RequestParam Long userId,
                                              @PathVariable Long groupGiftId) {
        groupGiftService.delete(userId, groupGiftId);
        return ResponseEntity.ok(new ApiResponse("Group gift deleted."));
    }
}
