package missu.epsilon.client.command.completion;

import missu.epsilon.client.Client;
import missu.epsilon.client.command.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter {

    public static List<String> getSuggestions(String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length <= 1) {
            // 补全命令名
            String partial = args[0].toLowerCase();
            for (Command cmd : Client.commandManager.getElements()) {
                for (String alias : cmd.getAliases()) {
                    if (alias.startsWith(partial)) result.add(alias);
                }
            }
            return result;
        }

        // 找到匹配的命令，走各自的参数补全
        Command matched = null;
        for (Command cmd : Client.commandManager.getElements()) {
            for (String alias : cmd.getAliases()) {
                if (alias.equalsIgnoreCase(args[0])) {
                    matched = cmd;
                    break;
                }
            }
        }

        if (matched == null) return result;

        // 第二个参数：如果命令支持 module 补全
        if (matched instanceof ArgsCompleter completer) {
            result.addAll(completer.suggestArgs(args));
        }

        return result;
    }
}
