package projects.currencyexchangeapi.exception;

public class CurrencyAlreadyExistsException extends RuntimeException {
    public CurrencyAlreadyExistsException(String massage) {
        super(massage);
    }
}
