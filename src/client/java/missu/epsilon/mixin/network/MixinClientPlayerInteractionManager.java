package missu.epsilon.mixin.network;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.player.ClickBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V"))
    public void attackEntity(PlayerEntity entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        Client.getInstance().getEventManager().call(new AttackEvent(targetEntity));
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.BEFORE))
    private void onClickBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (mc == null || mc.world == null || mc.player == null) return;
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isAir()) {
            Client.getInstance().getEventManager().call(new ClickBlockEvent(pos,direction));
        }
    }

}
