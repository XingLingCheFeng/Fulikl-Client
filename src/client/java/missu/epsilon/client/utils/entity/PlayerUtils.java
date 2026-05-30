package missu.epsilon.client.utils.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.modules.player.AntiBot;
import missu.epsilon.client.features.modules.player.Teams;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.math.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

public class PlayerUtils {
    public static int ticksSinceTeleport = 0;
    private static final ObjectArrayList<LivingEntity> targets = new ObjectArrayList<>();
    public static int findSlot(Item item) {
        if (mc.player == null) return -1;
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == item) {
                slot = i;
            }
        }
        return slot;
    }
    @EventTarget
    public void onPacket(PacketEvent event){
        if (event.packetState != CancellableEvent.PacketState.RECEIVE) return;

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
            ticksSinceTeleport = 0;
    }

    public static int getSlotCount(Item item) {
        if (mc.player == null) return 0;

        int count = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public static boolean isOnGround(Entity entity, double height) {
        return entity.getWorld().getBlockCollisions(entity, entity.getBoundingBox().offset(0, -height, 0)).iterator().hasNext();
    }

    public static int findInventoryItem(Item item) {
        if (mc.player == null) return -1;
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().main.get(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                slot = i;
            }
        }
        return slot;
    }
    public static int findAllItem(Item item) {
        if (mc.player == null) return -1;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void setDelta(float deltaYaw) {
        double radian = Math.toRadians(deltaYaw);
        double x = Math.sin(radian);
        double z = Math.cos(radian);

        if (z > 0.5) {
            mc.options.forwardKey.setPressed(true);
        } else if (z < -0.5) {
            mc.options.backKey.setPressed(true);
        }
        if (x > 0.5) {
            mc.options.rightKey.setPressed(true);
        } else if (x < -0.5) {
            mc.options.leftKey.setPressed(true);
        }
    }

    public static void resetKeys() {
        KeyBinding forward = mc.options.forwardKey;
        KeyBinding back = mc.options.backKey;
        KeyBinding left = mc.options.leftKey;
        KeyBinding right = mc.options.rightKey;

        forward.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), forward.getDefaultKey().getCode()));
        back.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), back.getDefaultKey().getCode()));
        left.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), left.getDefaultKey().getCode()));
        right.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), right.getDefaultKey().getCode()));
    }

    public static void sendClick(final int button, final boolean state) {
        final InputUtil.Key keyBind = button == 0 ? mc.options.attackKey.getDefaultKey() : mc.options.useKey.getDefaultKey();

        KeyBinding.setKeyPressed(keyBind, state);

        if (state) {
            KeyBinding.onKeyPressed(keyBind);
        }
    }
    public static void doItemUseWithoutBlock() {
        if (!mc.interactionManager.isBreakingBlock()) {
            mc.itemUseCooldown = 4;
            if (!mc.player.isRiding()) {
                for(Hand hand : Hand.values()) {
                    ItemStack itemStack = mc.player.getStackInHand(hand);
                    if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
                        return;
                    }

                    if (mc.crosshairTarget != null) {
                        if (Objects.requireNonNull(mc.crosshairTarget.getType()) == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) mc.crosshairTarget;
                            Entity entity = entityHitResult.getEntity();
                            if (!mc.world.getWorldBorder().contains(entity.getBlockPos())) {
                                return;
                            }

                            ActionResult actionResult = mc.interactionManager.interactEntityAtLocation(mc.player, entity, entityHitResult, hand);
                            if (!actionResult.isAccepted()) {
                                actionResult = mc.interactionManager.interactEntity(mc.player, entity, hand);
                            }

                            if (actionResult instanceof ActionResult.Success) {
                                ActionResult.Success success = (ActionResult.Success) actionResult;
                                if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                                    mc.player.swingHand(hand);
                                }

                                return;
                            }
                        }
                    }

                    if (!itemStack.isEmpty()) {
                        ActionResult actionResult3 = interactItem(mc.player, hand);
                        if (actionResult3 instanceof ActionResult.Success) {
                            ActionResult.Success success3 = (ActionResult.Success)actionResult3;
                            if (success3.swingSource() == ActionResult.SwingSource.CLIENT) {
                                mc.player.swingHand(hand);
                            }

                            return;
                        }
                    }
                }

            }
        }
    }
    public static ActionResult interactItem(PlayerEntity player, Hand hand) {
        mc.interactionManager.syncSelectedSlot();
        MutableObject<ActionResult> mutableObject = new MutableObject();
        mc.interactionManager.sendSequencedPacket(mc.world, (sequence) -> {
            PlayerInteractItemC2SPacket playerInteractItemC2SPacket = new PlayerInteractItemC2SPacket(hand, sequence, RotationUtils.getRotationOrElseMC().getYaw(), RotationUtils.getRotationOrElseMC().getPitch());
            ItemStack itemStack = player.getStackInHand(hand);
            if (player.getItemCooldownManager().isCoolingDown(itemStack)) {
                mutableObject.setValue(ActionResult.PASS);
                return playerInteractItemC2SPacket;
            } else {
                ActionResult actionResult = itemStack.use(mc.world, player, hand);
                ItemStack itemStack2;
                if (actionResult instanceof ActionResult.Success) {
                    ActionResult.Success success = (ActionResult.Success)actionResult;
                    itemStack2 = (ItemStack)Objects.requireNonNullElseGet(success.getNewHandStack(), () -> player.getStackInHand(hand));
                } else {
                    itemStack2 = player.getStackInHand(hand);
                }
                if (itemStack2 != itemStack) {
                    player.setStackInHand(hand, itemStack2);
                }
                mutableObject.setValue(actionResult);
                return playerInteractItemC2SPacket;
            }
        });
        return (ActionResult)mutableObject.getValue();
    }

    public static boolean isAirAbove(Entity entity, World world) {
        return !world.getBlockCollisions(entity, entity.getBoundingBox().offset(0, 1, 0)).iterator().hasNext();
    }

    public static double getBPS(Entity entity) {
        double bps = Math.hypot(entity.getX() - entity.prevX, entity.getZ() - entity.prevZ) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

    public static boolean isMoving() {
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }

    public static Block getBlockUnderPlayer(PlayerEntity player) {
        return mc.world != null ? mc.world.getBlockState((new BlockPos(MathUtils.floor_double(player.getX()), MathUtils.floor_double(player.getY() - 1.0), MathUtils.floor_double(player.getZ())))).getBlock() : null;
    }

    public static ObjectArrayList<BlockEntity> getBlockEntities(double range) {
        if (mc.world == null || mc.player == null) {
            return new ObjectArrayList<>();
        }

        var blockEntities = new ObjectArrayList<BlockEntity>();

        for (var blockEntity : getAllBlockEntities()) {
            var pos = blockEntity.getPos();

            if (Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos)) <= range) {
                blockEntities.add(blockEntity);
            }
        }

        return blockEntities;
    }

    private static ObjectArrayList<BlockEntity> getAllBlockEntities() {
        if (ClientUtils.isNull()) {
            return new ObjectArrayList<>();
        }

        var blockEntities = new ObjectArrayList<BlockEntity>();
        var renderDistance = mc.options.getViewDistance().getValue();
        var playerChunkX = mc.player.getBlockX() >> 4;
        var playerChunkZ = mc.player.getBlockZ() >> 4;

        for (var x = -renderDistance; x <= renderDistance; x++) {
            for (var z = -renderDistance; z <= renderDistance; z++) {
                var chunkX = playerChunkX + x;
                var chunkZ = playerChunkZ + z;

                if (mc.world.isChunkLoaded(chunkX, chunkZ)) {
                    blockEntities.addAll(mc.world.getChunk(chunkX, chunkZ).getBlockEntities().values());
                }
            }
        }

        return blockEntities;
    }

    public static void register() {
        Client.getInstance().getEventManager().subscribe(new PlayerUtils());
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        ticksSinceTeleport++;

        targets.clear();

        for (var entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && isTarget(livingEntity, true) && livingEntity != mc.player) {
                targets.add(livingEntity);
            }
        }
    }

    public static ObjectArrayList<LivingEntity> getAttackableTargets(double range) {
        if (mc.world == null || mc.player == null) {
            return new ObjectArrayList<>();
        }

        if (range <= 0) {
            return targets;
        }

        var targets = new ObjectArrayList<LivingEntity>();

        for (var livingEntity : PlayerUtils.targets) {
            if (mc.player.getEyePos().distanceTo(RotationUtils.getEntityNearestVec(mc.player, livingEntity)) <= range) {
                targets.add(livingEntity);
            }
        }

        return targets;
    }

    public static boolean isTarget(Entity entity, boolean canAttackCheck) {
        if (entity instanceof LivingEntity livingEntity && !livingEntity.equals(mc.player)) {
            if (livingEntity instanceof PlayerEntity player) {
                if (canAttackCheck) {
                    if (player.isSpectator() || player.isDead() || !player.isAlive() || AntiBot.isBot(player)) {
                        return false;
                    }


                    return !Client.moduleManager.getModule(Teams.class).isEnabled() || !Teams.isInYourTeam(player);
                }

                return true;
            }
        }

        return false;
    }
}
