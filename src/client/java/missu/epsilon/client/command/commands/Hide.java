package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.completion.ArgsCompleter;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.command.exceptions.InvalidArgumentException;
import missu.epsilon.client.command.exceptions.SyntaxErrorException;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class Hide implements Command, ArgsCompleter {

    @Override
    public String[] getAliases() {
        return new String[]{"hide"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length != 2) throw new SyntaxErrorException(getUsage());

        Module module = Client.moduleManager.getModule(arguments[1]);
        if (module == null) throw new InvalidArgumentException(arguments[1], "module not found");

        module.hide.set(!module.hide.get());
        ClientUtils.displayChat(String.format("%s%s %swas %s%s",
                Formatting.AQUA,
                module.getName(),
                Formatting.WHITE,
                module.isHide() ? Formatting.RED : Formatting.AQUA,
                module.isHide() ? "Hidden" : "Shown"));
    }

    @Override
    public String getUsage() {
        return ".hide [module]";
    }

    @Override
    public List<String> suggestArgs(String[] args) {
        if (args.length != 2) return List.of();
        String partial = args[1].toLowerCase();
        return Client.moduleManager.getModules().stream()
                .map(Module::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
