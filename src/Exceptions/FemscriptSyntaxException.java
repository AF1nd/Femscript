package Exceptions;

public class FemscriptSyntaxException extends Exception {
    public FemscriptSyntaxException(String message, Integer line) {
        super(message + " | LINE: " + line);
    }
}
