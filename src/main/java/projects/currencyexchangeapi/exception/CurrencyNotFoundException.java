package projects.currencyexchangeapi.exception;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String massage) {
        super(massage);
    }
}
