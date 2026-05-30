package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.util.Formatting;

public class Binds implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"binds", "bs"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException{
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Module module : Client.moduleManager.getModules()) {
            if (!module.bind.is("NONE")) {
                sb.append(Formatting.AQUA).append(module.name).append(" ").append(Formatting.WHITE).append(module.bind.get()).append(Formatting.RESET).append("\n");
            }
        }

        if (sb.length() == 1) {
            throw new CommandExecutionException(Formatting.RED + "No module was bound.");
        }

        ClientUtils.displayChat(Formatting.WHITE + "List of bound modules:" + Formatting.RESET);
        ClientUtils.displayChat(sb.toString());
    }

    @Override
    public String getUsage() {
        return "";
    }
}
