package projects.currencyexchangeapi.service.impl;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import projects.currencyexchangeapi.dto.registration.UserRegisterRequestDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterResponseDto;
import projects.currencyexchangeapi.entity.RoleEntity;
import projects.currencyexchangeapi.entity.UserEntity;
import projects.currencyexchangeapi.exception.RegistrationException;
import projects.currencyexchangeapi.mapper.UserMapper;
import projects.currencyexchangeapi.repository.RoleRepository;
import projects.currencyexchangeapi.repository.UserRepository;
import projects.currencyexchangeapi.service.UserService;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private long userRoleId;

    @PostConstruct
    void init() {
        userRoleId = roleRepository.findByType(RoleEntity.RoleType.USER).getId();
    }

    @Override
    public UserRegisterResponseDto register(UserRegisterRequestDto requestDto) {
        log.info("Attempting to register a new user");

        if (userRepository.existsByEmail(requestDto.email())) {
            log.warn("Registration attempt failed: Email already exists");

            throw new RegistrationException("User with this email already exists");
        }

        UserEntity user = userMapper.toUserEntity(requestDto);
        user.setRoles(Set.of(roleRepository.getReferenceById(userRoleId)));
        user.setPassword(passwordEncoder.encode(requestDto.password()));

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully, user id {}", savedUser.getId());

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public void blockUser(Long userId) {
        log.info("Attempting to block user with ID: {}", userId);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User with id " + userId + " not found"));

        user.setBlocked(true);
        userRepository.save(user);

        log.info("User with ID: {} successfully blocked", userId);
    }
}
