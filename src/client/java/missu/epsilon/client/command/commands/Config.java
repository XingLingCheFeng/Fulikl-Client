package missu.epsilon.client.command.commands;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.completion.ArgsCompleter;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.command.exceptions.SyntaxErrorException;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.util.Formatting;


import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config implements Command, ArgsCompleter {

    @Override
    public String[] getAliases() {
        return new String[]{"config", "cfg"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length < 2) throw new SyntaxErrorException(getUsage());

        switch (arguments[1].toLowerCase()) {
            case "save":
            case "load":
                if (arguments.length < 3) throw new SyntaxErrorException(getUsage());
                String fileName = arguments[2] + ".json";
                if (arguments[1].equalsIgnoreCase("save")) {
                    Client.configManager.saveConfig(fileName);
                } else {
                    Client.configManager.loadConfig(fileName, true);
                }
                break;

            case "folder":
                try {
                    Desktop.getDesktop().open(Client.configManager.configDir);
                    ClientUtils.displayChat(Formatting.WHITE + "Opening Config Folder...");
                } catch (Exception e) {
                    throw new CommandExecutionException("Failed to open Config Folder.");
                }
                break;

            default:
                throw new SyntaxErrorException(getUsage());
        }
    }

    @Override
    public List<String> suggestArgs(String[] args) {
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return Stream.of("save", "load", "folder")
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }


        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (!sub.equals("save") && !sub.equals("load")) return List.of();

            File[] files = Client.configManager.configDir.listFiles(
                    f -> f.isFile() && f.getName().endsWith(".json")
            );
            if (files == null) return List.of();

            String partial = args[2].toLowerCase();
            return Arrays.stream(files)
                    .map(f -> f.getName().replace(".json", ""))
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public String getUsage() {
        return ".config <save|load> [name] | .config folder";
    }

}