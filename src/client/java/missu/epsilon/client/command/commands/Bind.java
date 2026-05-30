package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.completion.ArgsCompleter;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.command.exceptions.SyntaxErrorException;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.KeyCodeConverter;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class Bind implements Command, ArgsCompleter {

    @Override
    public String[] getAliases() {
        return new String[]{"bind", "keybind"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if(arguments.length != 3) throw new SyntaxErrorException(getUsage());

        Module module = Client.moduleManager.getModule(arguments[1]);

        int key = KeyCodeConverter.stringToKeycode(arguments[2].toUpperCase());

        if(module == null) {
            throw new CommandExecutionException(Formatting.RED + arguments[1] + Formatting.RESET + " is an invalid module.");
        }

        module.bind.set(KeyCodeConverter.keycodeToString(key).toUpperCase());
        ClientUtils.displayChat(String.format("%sBound %s%s %sto %s%s",
                Formatting.WHITE,//Bound
                Formatting.AQUA,module.getName(),//[module]
                Formatting.WHITE, //To
                Formatting.AQUA,key == 0 ? "NONE" : arguments[2].toUpperCase()));//[key]
    }


    @Override
    public String getUsage() {
        return ".bind [module] [key]";
    }

    @Override
    public List<String> suggestArgs(String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return Client.moduleManager.getModules().stream()
                    .map(Module::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String partial = args[2].toLowerCase();
            return KeyCodeConverter.getKeyNames().stream()
                    .filter(k -> k.startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return List.of();
    }

}
