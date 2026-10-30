package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DatabaseException extends Exception{
    public DatabaseException (String message) {
        super(message);
    }
    public DatabaseException (String message, Throwable ex) {
        super(message, ex);
    }
}