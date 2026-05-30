package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.command.exceptions.SyntaxErrorException;

import java.util.Arrays;

public class IRC implements Command {

    @Override
    public String[] getAliases() {
        return new String[]{"irc", "i"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length < 2) throw new SyntaxErrorException(getUsage());


        String message = String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length));
    }

    @Override
    public String getUsage() {
        return ".irc [message]";
    }
}
