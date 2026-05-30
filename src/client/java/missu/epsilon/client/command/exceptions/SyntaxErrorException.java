package missu.epsilon.client.command.exceptions;

public class SyntaxErrorException extends CommandExecutionException{
    public SyntaxErrorException(String message) {
        super(message);
    }
}
