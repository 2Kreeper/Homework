package eu.baron_online.homework.exception;

public class MySQLException extends Exception {

    public MySQLException() {
        super();
    }

    public MySQLException(String description) {
        super(description);
    }
}
