package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftDTOOut;
import org.example.tahadaw.DTO.OUT.GroupGiftInviteDTOOut;
import org.example.tahadaw.DTO.OUT.GroupGiftOptionDTOOut;
import org.example.tahadaw.DTO.OUT.GroupGiftResultsDTOOut;
import org.example.tahadaw.Model.GroupGiftInvite;
import org.example.tahadaw.Model.GroupGiftOption;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GroupGiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/group-gifts")
@RequiredArgsConstructor
public class GroupGiftController {

    private final GroupGiftService groupGiftService;

    @PostMapping
    public ResponseEntity<GroupGiftDTOOut> create(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody GroupGiftCreateDTOIn request) {
        return ResponseEntity.ok(groupGiftService.create(user.getId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupGiftDTOOut>> listMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupGiftService.listMine(user.getId()));
    }

    @GetMapping("/{groupGiftId}")
    public ResponseEntity<GroupGiftDTOOut> getOne(@AuthenticationPrincipal User user,
                                                @PathVariable Long groupGiftId) {
        return ResponseEntity.ok(groupGiftService.getOne(user.getId(), groupGiftId));
    }

    @PutMapping("/{groupGiftId}")
    public ResponseEntity<GroupGiftDTOOut> update(@AuthenticationPrincipal User user,
                                                  @PathVariable Long groupGiftId,
                                                  @Valid @RequestBody GroupGiftUpdateDTOIn request) {
        return ResponseEntity.ok(groupGiftService.update(user.getId(), groupGiftId, request));
    }

    @DeleteMapping("/{groupGiftId}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal User user,
                                              @PathVariable Long groupGiftId) {
        groupGiftService.delete(user.getId(), groupGiftId);
        return ResponseEntity.ok(new ApiResponse("Group gift deleted."));
    }

    @PostMapping("/add-option/{groupGiftId}")
    public ResponseEntity<?> addOption(@PathVariable Long groupGiftId,
                                       @AuthenticationPrincipal User user,
                                       @RequestBody @Valid GroupGiftOption groupGiftOption) {

        groupGiftService.addGroupGiftOption(user.getId(), groupGiftId, groupGiftOption);
        return ResponseEntity.status(200).body(new ApiResponse("Group gift option added successfully"));
    }

    @PostMapping("/ai-generate-option/{groupGiftId}")
    public ResponseEntity<?> generateAiOptions(@PathVariable Long groupGiftId,
                                               @AuthenticationPrincipal User user) {

        groupGiftService.generateAiOptions(user.getId(), groupGiftId);
        return ResponseEntity.status(200).body(new ApiResponse("AI group gift options generated successfully"));
    }

    @GetMapping("/get-options/{groupGiftId}")
    public ResponseEntity<List<GroupGiftOptionDTOOut>> getOptions(@PathVariable Long groupGiftId,
                                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupGiftService.getOptions(user.getId(), groupGiftId));
    }

    @PostMapping("/send-invite/{groupGiftId}")
    public ResponseEntity<List<GroupGiftInviteDTOOut>> sendInvites(@PathVariable Long groupGiftId,
                                         @AuthenticationPrincipal User user,
                                         @RequestBody List<GroupGiftInvite> invites) {

        return ResponseEntity.ok(groupGiftService.sendInvites(user.getId(), groupGiftId, invites));
    }

    @PutMapping("/close-voting/{groupGiftId}")
    public ResponseEntity<?> closeVoting(@PathVariable Long groupGiftId,
                                         @AuthenticationPrincipal User user) {

        groupGiftService.closeVoting(user.getId(), groupGiftId);
        return ResponseEntity.status(200).body(new ApiResponse("Group gift voting closed successfully"));
    }

    @GetMapping("/results/{groupGiftId}")
    public ResponseEntity<GroupGiftResultsDTOOut> getResults(@PathVariable Long groupGiftId,
                                        @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(groupGiftService.getResults(user.getId(), groupGiftId));
    }
}
