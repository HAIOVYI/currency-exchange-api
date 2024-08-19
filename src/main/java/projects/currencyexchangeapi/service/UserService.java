package projects.currencyexchangeapi.service;

import projects.currencyexchangeapi.dto.registration.UserRegisterRequestDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterResponseDto;

public interface UserService {

    UserRegisterResponseDto register(UserRegisterRequestDto requestDto);

    void blockUser(Long userId);
}
