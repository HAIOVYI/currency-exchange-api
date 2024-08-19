package projects.currencyexchangeapi.entity;

import java.math.BigDecimal;

public interface CurrencyRateProjection {

    BigDecimal getMinRate();

    BigDecimal getMaxRate();
}
