package dataAccess;

public class AlreadyTakenException extends DataAccessException{
    public AlreadyTakenException(String message) {
        super(message);
    }
}