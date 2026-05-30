package missu.epsilon.mixin.client.gui.hud;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.exploit.MurderMystery;
import missu.epsilon.client.features.modules.player.HackDefender;
import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(PlayerListHud.class)
public class MixinPlayerList {

    @Unique
    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        String playerName = entry.getProfile().getName();
        boolean isHacker = false;
        int vl = 0;
        if (Client.moduleManager.getModule(HackDefender.class).getState()) {
            if (HackDefender.violationLevels != null) {
                for (PlayerEntity player : HackDefender.violationLevels.keySet()) {
                    if (player instanceof PlayerEntity) {
                        if (player.getName().getString().equals(playerName)) {
                            vl = HackDefender.getViolationLevel(player);
                            isHacker = true;
                            break;
                        }
                    }
                }
            }
        }
        if (entry.getProfile().getName().equals(Objects.requireNonNull(mc.player).getName().getString())) {
            Text originalText = entry.getDisplayName() != null ? entry.getDisplayName().copy() : Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName()));
            Text prefixText = Text.literal(Formatting.GREEN + "[Self]" + Formatting.RESET);
            MutableText combinedText = prefixText.copy().append(originalText).append(Formatting.AQUA + "(" + Client.username + ")");
            Text formattedText = this.applyGameModeFormatting(entry, combinedText);
            cir.setReturnValue(formattedText);
        } else if (ClientData.isCoolUser(entry.getProfile().getName())) {
            ClientData.UserData userData = ClientData.userData.get(entry.getProfile().getName());
            Text originalText = entry.getDisplayName() != null ? entry.getDisplayName().copy() : Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName()));
            Text prefixText = getPrefixText(Formatting.AQUA + "[" + userData.client + "]" + Formatting.RESET, entry, Formatting.AQUA + "[" + userData.client + "]" + Formatting.RED + "[Murderer]" + Formatting.RESET, Formatting.AQUA + "[" + userData.client + "]" + Formatting.BLUE + "[Bow]" + Formatting.RESET);
            MutableText combinedText = prefixText.copy().append(originalText).append(Formatting.AQUA + "(" + userData.name + ")");
            Text formattedText = this.applyGameModeFormatting(entry, combinedText);
            cir.setReturnValue(formattedText);
        }else {
            Text originalText = entry.getDisplayName() != null ? entry.getDisplayName().copy() : Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName()));
            Text prefixText = getPrefixText("", entry, Formatting.RED + "[Murderer]" + Formatting.RESET, Formatting.GREEN + "[Bow]" + Formatting.RESET);
            if (isHacker && Client.moduleManager.getModule(HackDefender.class).getState()) {
                prefixText = Text.literal(Formatting.DARK_RED + "[Hacker VL: " + vl + "]" + Formatting.RESET);
            }
            MutableText combinedText = prefixText.copy().append(originalText);
            Text formattedText = this.applyGameModeFormatting(entry, combinedText);
            cir.setReturnValue(formattedText);
        }
    }

    @Unique
    private static @NotNull Text getPrefixText(String AQUA, PlayerListEntry entry, String AQUA1, String AQUA2) {
        Text prefixText = Text.literal(AQUA);
        if (!MurderMystery.murdererName.isEmpty() && MurderMystery.murdererName.contains(entry.getProfile().getName())) {
            prefixText = Text.literal(AQUA1);
        } else if (!MurderMystery.bowName.isEmpty() && MurderMystery.bowName.contains(entry.getProfile().getName())) {
            prefixText = Text.literal(AQUA2);
        }
        return prefixText;
    }

}
