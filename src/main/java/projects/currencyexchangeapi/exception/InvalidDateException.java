package projects.currencyexchangeapi.exception;

public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String massage) {
        super(massage);
    }
}
