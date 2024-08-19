package projects.currencyexchangeapi.dto.registration;

public record UserRegisterResponseDto(

        Long id,

        String email,

        String firstName,

        String lastName
) {
}
