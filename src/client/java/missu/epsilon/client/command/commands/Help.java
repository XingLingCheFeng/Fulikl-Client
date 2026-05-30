package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.utils.client.ClientUtils;

import java.util.Arrays;

public final class Help implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"help", "h"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        ClientUtils.addMessage("§fAvailable Commands:");
        for (Command command : Client.commandManager.getElements()) {
            String[] aliases = command.getAliases();
            String main = aliases[0];
            String aliasPart = aliases.length > 1
                    ? "(" + String.join(", ", Arrays.copyOfRange(aliases, 1, aliases.length)) + ")"
                    : "";
            ClientUtils.displayChat(String.format("§b%s§7%s §f: %s", main, aliasPart, command.getUsage()));
        }
    }

    @Override
    public String getUsage() {
        return "help/h";
    }
}
