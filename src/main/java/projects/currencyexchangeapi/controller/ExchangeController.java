package projects.currencyexchangeapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import projects.currencyexchangeapi.dto.exchange.ExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.UserEntity;
import projects.currencyexchangeapi.service.ExchangeService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Currency exchange management", description = "Endpoints for managing currency exchange requests")
@RequestMapping("/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @Operation(summary = "Create a new exchange request",
            description = "Create a new exchange request by providing the details in the request body."
                    + " The request is processed based on the user's details.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ExchangeResponseDto create(@RequestBody ExchangeRequestDto requestDto,
                               @AuthenticationPrincipal UserEntity user) {
        return exchangeService.create(requestDto, user);
    }
}
