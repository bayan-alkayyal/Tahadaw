package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tahadaw.Model.enums.Role;

@Data
@AllArgsConstructor

public class UserDTOOut {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

    private Boolean isPremium;
}
