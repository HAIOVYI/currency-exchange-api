package projects.currencyexchangeapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import projects.currencyexchangeapi.dto.currency.CreateCurrencyRequestDto;
import projects.currencyexchangeapi.dto.currency.CurrencyResponseDto;
import projects.currencyexchangeapi.service.CurrencyService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Currency management", description = "Endpoints for managing currencies")
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(summary = "Get currency by id",
            description = "Retrieve the details of a currency by its id.")
    @GetMapping("/{id}")
    public CurrencyResponseDto findById(@PathVariable Long id) {
        return currencyService.getById(id);
    }

    @Operation(summary = "Get all currencies",
            description = "Retrieve a list of all currencies.")
    @GetMapping
    public List<CurrencyResponseDto> findAll() {
        return currencyService.getAll();
    }

    @Operation(summary = "Create a new currency",
            description = "Create a new currency by providing the details in the request body."
                    + " This endpoint requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyResponseDto create(@Valid @RequestBody CreateCurrencyRequestDto requestDto) {
        return currencyService.create(requestDto);
    }

    @Operation(summary = "Delete currency by id",
            description = "Delete a currency by its id. This endpoint requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable("id") Long id) {
        currencyService.delete(id);

        return ResponseEntity.ok("Deleted Currency with id " + id);
    }
}
