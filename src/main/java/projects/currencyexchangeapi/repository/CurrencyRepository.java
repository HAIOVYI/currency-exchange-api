package projects.currencyexchangeapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projects.currencyexchangeapi.entity.CurrencyEntity;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long> {

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
