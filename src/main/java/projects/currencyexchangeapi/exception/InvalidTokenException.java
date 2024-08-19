package projects.currencyexchangeapi.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String massage) {
        super(massage);
    }
}
