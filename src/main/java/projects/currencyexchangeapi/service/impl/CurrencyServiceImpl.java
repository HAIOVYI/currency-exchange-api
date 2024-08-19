package projects.currencyexchangeapi.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import projects.currencyexchangeapi.dto.currency.CreateCurrencyRequestDto;
import projects.currencyexchangeapi.dto.currency.CurrencyResponseDto;
import projects.currencyexchangeapi.entity.CurrencyEntity;
import projects.currencyexchangeapi.exception.CurrencyAlreadyExistsException;
import projects.currencyexchangeapi.exception.CurrencyNotFoundException;
import projects.currencyexchangeapi.mapper.CurrencyMapper;
import projects.currencyexchangeapi.repository.CurrencyRepository;
import projects.currencyexchangeapi.service.CurrencyService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @Override
    public CurrencyResponseDto getById(Long id) {
        log.info("Fetching currency, currency id: {}", id);

        return currencyMapper.toDto(currencyRepository.findById(id).orElseThrow(() ->
                new CurrencyNotFoundException("Currency with id " + id + " not found")));
    }

    @Override
    public List<CurrencyResponseDto> getAll() {
        log.info("Fetching all currencies");

        return currencyRepository.findAll()
                .stream()
                .map(currencyMapper::toDto)
                .toList();
    }

    @Override
    public CurrencyResponseDto create(CreateCurrencyRequestDto requestDto) {
        log.info("Attempting to create currency, code: {}, name: {}", requestDto.code(), requestDto.name());

        validateCurrencyExists(requestDto);

        CurrencyEntity currencyEntity = currencyMapper.toEntity(requestDto);

        return currencyMapper.toDto(currencyRepository.save(currencyEntity));
    }

    @Override
    public void delete(Long id) {
        log.info("Attempting to delete currency, currency id: {}", id);
        currencyRepository.deleteById(id);
        log.info("Currency deleted successfully, currency id: {}", id);
    }

    private void validateCurrencyExists(CreateCurrencyRequestDto requestDto) {
        log.info("Validating if currency exists, code: {}, name: {}", requestDto.code(), requestDto.name());

        if (currencyRepository.existsByCode(requestDto.code())) {
            log.warn("Currency creation failed, currency code {} already exists", requestDto.code());

            throw new CurrencyAlreadyExistsException("Currency with code "
                    + requestDto.code()
                    + " already exists");
        }

        if (currencyRepository.existsByName(requestDto.name())) {
            log.warn("Currency creation failed, currency name {} already exists", requestDto.name());

            throw new CurrencyAlreadyExistsException("Currency with name "
                    + requestDto.name()
                    + " already exists");
        }
    }
}
