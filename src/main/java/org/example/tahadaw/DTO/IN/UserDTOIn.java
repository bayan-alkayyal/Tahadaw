package org.example.tahadaw.DTO.IN;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOIn {

    @NotEmpty(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotEmpty(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be at least 6 characters")
    private String password;

    @NotEmpty(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(
            regexp = "^05[0-9]{8}$",
            message = "Phone number must start with 05 and contain 10 digits"
    )
    private String phoneNumber;
}
