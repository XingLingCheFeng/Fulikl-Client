package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "SpeedMine",category = ModuleCategory.WORLD,description = "End mining early when you mine")
public class SpeedMine extends Module {

    public static MultiBoolValue addons = new MultiBoolValue("Addons",new BoolValue[]{
            new BoolValue("Normal",true),
            new BoolValue("Packet",false),
            new BoolValue("Potion",false)}
    );
    public static BoolValue onGround = new BoolValue("OnGround",true);
    public static NumberValue strength = (NumberValue) new NumberValue("PotionStrength",1,1,3,1).displayable(() -> addons.get("Potion"));
    public static NumberValue damage = (NumberValue) new NumberValue("ExpectedDamage",0.65,0,1,0.01).displayable(() -> addons.get("Normal"));

    public static boolean bzs = false;
    public static double bzx = 0.0;
    public static BlockPos pos = null;
    public static Direction face = null;
    public static boolean setEffect = false;

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (setEffect) {
            mc.player.removeStatusEffect(StatusEffects.HASTE);
            setEffect = false;
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || !ClientData.clientOnGround() && onGround.get() || mc.interactionManager == null || mc.getNetworkHandler() == null) {
            return;
        }
        mc.interactionManager.blockBreakingCooldown = 0;

        if (addons.get("Packet")) {
            if (bzs) {
                BlockState block = mc.world.getBlockState(pos);
                bzx += (block.calcBlockBreakingDelta(mc.player, mc.world, pos) * 1.4F);
                if (bzx >= 1.0) {
                    mc.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
                    bzx = 0.0;
                    bzs = false;
                }
            }
        }

        if (addons.get("Normal")) {
            if (mc.interactionManager.currentBreakingProgress > damage.get()) {
                mc.interactionManager.currentBreakingProgress = 1f;
            }
            if (BedBreaker.currentDamage > damage.get()) {
                BedBreaker.currentDamage = 1F;
            }
        }

        if (addons.get("Potion")) {
            if (!mc.player.hasStatusEffect(StatusEffects.HASTE)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 1337, strength.get().intValue()-1, false, false));
                setEffect = true;
            }
        } else if (setEffect) {
            mc.player.removeStatusEffect(StatusEffects.HASTE);
            setEffect = false;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packetState == CancellableEvent.PacketState.RECEIVE) return;
        if (addons.get("Packet")) {
            if (event.packet instanceof PlayerActionC2SPacket packet && mc.interactionManager != null) {
                if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                    bzs = true;
                    pos = packet.getPos();
                    face = packet.getDirection();
                } else if (packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK || packet.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                    bzs = false;
                    pos = null;
                    face = null;
                }
            }
        }
    }
}
