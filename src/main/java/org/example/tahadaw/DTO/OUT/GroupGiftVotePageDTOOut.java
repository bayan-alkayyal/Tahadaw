package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupGiftVotePageDTOOut {

    private String title;
    private String description;
    private String recipientName;
    private LocalDateTime votingDeadline;
    private Boolean votingOpen;
    private List<GroupGiftOptionDTOOut> options;
    private String inviteeName;
}
