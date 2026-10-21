package dataAccess;

public class InvalidLoginException extends Exception{
    public InvalidLoginException(String message) {
        super(message);
    }
    public InvalidLoginException() {}
}