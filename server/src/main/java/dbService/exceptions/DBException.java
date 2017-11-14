package dbService.exceptions;

/**
 * Created by Dmitrii Stoianov
 */


public class DBException extends Exception {
    public DBException(Throwable throwable) {
        super(throwable);
    }

    public DBException(String message) {
        super(message);
    }
}
