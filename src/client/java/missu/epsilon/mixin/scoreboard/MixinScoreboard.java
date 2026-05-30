package missu.epsilon.mixin.scoreboard;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.exploit.Disabler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public abstract class MixinScoreboard {

    @Shadow @Final private Object2ObjectMap<String, Team> teamsByScoreHolder;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract @Nullable Team getScoreHolderTeam(String scoreHolderName);

    /**
     * @author Jon_awa
     * @reason For fix heypixel crash.
     */
    @Inject(method = "removeScoreHolderFromTeam", at = @At("HEAD"), cancellable = true)
    public void removeScoreHolderFromTeam(String scoreHolderName, Team team, CallbackInfo ci) {
        var protocol = Client.moduleManager.getModule(Disabler.class);
        if (protocol.isEnabled() && Disabler.heypixel.get("Heypixel Protocol")) {
            if (this.getScoreHolderTeam(scoreHolderName) != team) {
                LOGGER.error("Player is either on another team or not on any team. Cannot remove from team '{}'.", team.getName());
            } else {
                this.teamsByScoreHolder.remove(scoreHolderName);
                team.getPlayerList().remove(scoreHolderName);
            }
            ci.cancel();
        }
    }

}
