package projects.currencyexchangeapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projects.currencyexchangeapi.entity.ExchangeEntity;

public interface ExchangeRepository extends JpaRepository<ExchangeEntity, Long> {
}
