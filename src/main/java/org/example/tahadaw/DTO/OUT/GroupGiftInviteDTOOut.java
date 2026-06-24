package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupGiftInviteDTOOut {

    private Long id;
    private String inviteeName;
    private String inviteeEmail;
    private String token;
}
