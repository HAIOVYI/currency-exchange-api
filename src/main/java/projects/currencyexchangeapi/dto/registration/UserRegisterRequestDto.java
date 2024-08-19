package projects.currencyexchangeapi.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 50)
        String password,

        @NotBlank
        @Size(min = 8, max = 50)
        String confirmPassword,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName
) {
}
