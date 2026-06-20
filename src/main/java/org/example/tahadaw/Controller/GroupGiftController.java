package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftVoteDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftDTOOut;
import org.example.tahadaw.Model.GroupGiftInvite;
import org.example.tahadaw.Model.GroupGiftOption;
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

    @PostMapping("/add-option/{groupGiftId}/{userId}")
    public ResponseEntity<?> addOption(@PathVariable Long groupGiftId,
                                       @PathVariable Long userId,
                                       @RequestBody @Valid GroupGiftOption groupGiftOption) {

        groupGiftService.addGroupGiftOption(userId, groupGiftId, groupGiftOption);
        return ResponseEntity.status(200).body(new ApiResponse("Group gift option added successfully"));
    }

    @PostMapping("/ai-generate-option/{groupGiftId}")
    public ResponseEntity<?> generateAiOptions(@PathVariable Long groupGiftId,
                                               @RequestParam Long userId) {

        groupGiftService.generateAiOptions(userId, groupGiftId);
        return ResponseEntity.status(200).body(new ApiResponse("AI group gift options generated successfully"));
    }

    @GetMapping("/get-options/{groupGiftId}")
    public ResponseEntity<?> getOptions(@PathVariable Long groupGiftId) {
        return ResponseEntity.status(200).body(groupGiftService.getOptions(groupGiftId));
    }

    @PostMapping("/send-invite/{groupGiftId}")
    public ResponseEntity<?> sendInvites(@PathVariable Long groupGiftId,
                                         @RequestParam Long userId,
                                         @RequestBody List<GroupGiftInvite> invites) {

        return ResponseEntity.status(200).body(groupGiftService.sendInvites(userId, groupGiftId, invites));
    }

    @GetMapping("/get-vote/{token}")
    public ResponseEntity<?> getVotePageData(@PathVariable String token) {

        return ResponseEntity.status(200).body(groupGiftService.getVotePageData(token));
    }

    @PostMapping("/vote/{token}")
    public ResponseEntity<?> submitVote(@PathVariable String token,
                                        @RequestBody @Valid GroupGiftVoteDTOIn voteDTOIn) {

        groupGiftService.submitVote(token, voteDTOIn.getOptionId());
        return ResponseEntity.status(200).body(new ApiResponse("Vote submitted successfully"));
    }

    @PutMapping("/close-voting/{groupGiftId}/{userId}")
    public ResponseEntity<?> closeVoting(@PathVariable Long groupGiftId,
                                         @PathVariable Long userId) {

        groupGiftService.closeVoting(userId, groupGiftId);
        return ResponseEntity.status(200).body(new ApiResponse("Group gift voting closed successfully"));
    }

    @GetMapping("/results/{groupGiftId}/{userId}")
    public ResponseEntity<?> getResults(@PathVariable Long groupGiftId,
                                        @PathVariable Long userId) {

        return ResponseEntity.status(200).body(groupGiftService.getResults(userId, groupGiftId));
    }
}
