package missu.epsilon.client.command.commands;

import missu.epsilon.client.command.Command;
import missu.epsilon.client.command.exceptions.CommandExecutionException;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

public final class IGN implements Command {

    @Override
    public String[] getAliases() {
        return new String[]{"name", "copy", "ign"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getGameProfile() == null) throw new CommandExecutionException("You're not in game or not logged in.");

        String name = player.getGameProfile().getName();
        MinecraftClient.getInstance().keyboard.setClipboard(name);

        ClientUtils.displayChat("Copied. Your IGN: " + Formatting.AQUA + name,true);
    }

    @Override
    public String getUsage() {
        return ".ign";
    }
}
