package projects.currencyexchangeapi.exception;

public class ExchangeNotFoundException extends RuntimeException {
    public ExchangeNotFoundException(String massage) {
        super(massage);
    }
}
