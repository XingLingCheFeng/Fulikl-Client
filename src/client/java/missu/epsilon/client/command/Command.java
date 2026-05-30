package missu.epsilon.client.command;


import missu.epsilon.client.command.exceptions.CommandExecutionException;

public interface Command {

    String[] getAliases();

    void execute(String[] arguments) throws CommandExecutionException;

    String getUsage();

}
