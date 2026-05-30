package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.modules.player.AutoTool;
import missu.epsilon.client.features.modules.player.Blink;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.block.BreakUtils;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.VecRotation;
import missu.epsilon.client.utils.entity.BlinkUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;
import static missu.epsilon.client.utils.entity.RotationUtils.performRaytrace;

/**
 * Author Daniel
 * 26/10/25
 */

@ModuleInfo(name = "BedBreaker", description = "Automatically break bed in the game", category = ModuleCategory.WORLD)
public class BedBreaker extends Module {
    public static ListValue mode = new ListValue("BreakMode", new String[]{"ThroughWall", "Swap", "Legit"}, "Swap");
    public ListValue render = new ListValue("RenderMode", new String[]{"Top", "None"}, "Top");
    public static ListValue rotationMode = new ListValue("RotationMode", new String[]{"Normal", "Snap"}, "Normal");
    public static ListValue teams = new ListValue("Teams", new String[]{"None", "Hypixel"}, "Hypixel");
    public static BoolValue renderBox = new BoolValue("RenderBox", true);
    public static NumberValue range = new NumberValue("Range", 4.5, 0, 7, 0.01);
    public static BoolValue noDelay = new BoolValue("NoBreakDelay", true);
    public static BoolValue noHit = new BoolValue("NoHit",true);

    public static Integer targetX;
    public static Integer targetZ;
    public static BlockPos pos, oldPos;
    private int blockHitDelay = 0;
    private static final TimerUtils searchTimer = new TimerUtils();
    public static boolean hitBlock = false;
    public static float currentDamage = 0F;
    public static BlockPos breakingBlockPos = null;
    public static BreakState breakState = BreakState.NONE;

    public enum BreakState {
        NONE,
        PREPARE,
        BREAKING,
        FINISHING
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onWorld(WorldEvent event) {
        ClientUtils.send("/lang English");
    }

    @Override
    public void onEnable() {
        breakState = BreakState.NONE;
        if (mc.interactionManager == null || mc.getNetworkHandler() == null) return;
        if (pos != null && !mc.interactionManager.getCurrentGameMode().isCreative()) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
        }
        hitBlock = false;
        breakingBlockPos = null;
        currentDamage = 0F;
        pos = null;
    }

    @Override
    public void onDisable() {
        breakState = BreakState.NONE;
        currentDamage = 0F;
        pos = null;
        breakingBlockPos = null;
        hitBlock = false;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null || mc.world == null || event.eventState != CancellableEvent.EventState.POST) return;

        if (noHit.get() && (KillAura.currentTarget != null || !KillAura.targets.isEmpty())) {
            pos = null;
            breakingBlockPos = null;
            hitBlock = false;
            breakState = BreakState.NONE;
            currentDamage = 0F;
            oldPos = null;
        }

        if (!teams.get().equals("None")) {
            if (targetX != null) {
                if (mc.player.getZ() > targetZ - 10 && mc.player.getZ() < targetZ + 10 && mc.player.getX() > targetX - 10 && mc.player.getX() < targetX + 10) {
                    return;
                }
            }
        }

        if (pos == null || !(mc.world.getBlockState(pos).getBlock() instanceof BedBlock) || mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range.get() * range.get()) {
            //如果挖的不是床而是床旁边的方块，且我离开床外的时候，pos就设置成breakPos(总之别为null)
            if (breakingBlockPos != null) {
                pos = breakingBlockPos;
            } else pos = findBed();

            //CivBreak可能需要修改一下底层，有点难搞，不好改，到时候再看
            if (pos == null || mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range.get() * range.get() || mc.world.getBlockState(pos).isAir()) {
                hitBlock = false;
                breakingBlockPos = null;
                breakState = BreakState.NONE;
                pos = null;
            }
        }

        if (pos == null) {
            currentDamage = 0F;
            breakState = BreakState.NONE;
            return;
        }

        BlockPos currentPos = pos;
        VecRotation spot = getBestAimVecForBed(currentPos);
        if (mode.is("Legit") || mode.is("Swap")) {
            BlockPos blockPos;
            if (mode.is("Swap")) {
                ClientPlayerEntity player = mc.player;
                World world = mc.world;

                // 如果当前有锁定方块，不再改变方块pos而导致重置挖掘进度
                if (breakingBlockPos != null) {
                    blockPos = breakingBlockPos;
                } else {
                    BlockState bedState = world.getBlockState(currentPos);
                    if (!(bedState.getBlock() instanceof BedBlock)) return;

                    Direction facing = bedState.get(BedBlock.FACING);
                    boolean isHead = bedState.get(BedBlock.PART) == BedPart.HEAD;
                    BlockPos otherBedPos = isHead ? currentPos.offset(facing.getOpposite()) : currentPos.offset(facing);

                    List<BlockPos> bedParts = new ArrayList<>();
                    bedParts.add(currentPos);
                    bedParts.add(otherBedPos);

                    Direction[] directions = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

                    List<BlockPos> solidBlocks = new ArrayList<>();
                    boolean hasAir = false;

                    for (BlockPos bedPart : bedParts) {
                        for (Direction dir : directions) {
                            BlockPos offsetPos = bedPart.offset(dir);
                            BlockState offsetState = world.getBlockState(offsetPos);

                            if (offsetState.isAir()) {
                                hasAir = true;
                                break;
                            }

                            if (!(offsetState.getBlock() instanceof BedBlock)) {
                                solidBlocks.add(offsetPos);
                            }
                        }
                        if (hasAir) break;
                    }

                    if (hasAir) {
                        blockPos = currentPos;
                    } else {
                        // 如果6格全部被围住就干脆选一个最容易挖的
                        double bestTime = Double.MAX_VALUE;
                        double bestDistance = Double.MAX_VALUE;
                        BlockPos bestPos = null;

                        for (BlockPos solidPos : solidBlocks) {
                            float relativeHardness = BreakUtils.calcBlockBreakingDelta(solidPos, world, solidPos);
                            if (relativeHardness <= 0) relativeHardness = 0.0001f;
                            double time = 1.0 / relativeHardness;
                            double distance = player.squaredDistanceTo(Vec3d.ofCenter(solidPos));

                            if (time < bestTime || (Math.abs(time - bestTime) < 1e-5 && distance < bestDistance)) {
                                bestTime = time;
                                bestDistance = distance;
                                bestPos = solidPos;
                            }
                        }

                        blockPos = bestPos != null ? bestPos : currentPos.up();
                    }

                    breakingBlockPos = blockPos;
                }
            } else {
                var hitResult = mc.world.raycast(new net.minecraft.world.RaycastContext(
                        mc.player.getEyePos(),
                        Vec3d.ofCenter(currentPos),
                        net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                        net.minecraft.world.RaycastContext.FluidHandling.NONE,
                        mc.player
                ));
                blockPos = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
                        ? hitResult.getBlockPos()
                        : null;
            }

            if (blockPos != null) {
                Block blockAtHit = mc.world.getBlockState(blockPos).getBlock();
                if (!(blockAtHit instanceof BedBlock)) {
                    pos = blockPos;
                    currentPos = pos;
                    spot = getBestAimVecForBed(currentPos);
                }
            }
        }

        if (oldPos != null && !oldPos.equals(currentPos)) {
            mc.world.setBlockBreakingInfo(mc.player.getId(), oldPos, -1);
            currentDamage = 0F;
        }
        oldPos = currentPos;
        if (blockHitDelay > 0 && !noDelay.get()) {
            blockHitDelay--;
            return;
        }
        if (spot != null && !hitBlock) {
            breakState = BreakState.PREPARE;
        }
        boolean validTarget =
                pos != null &&
                        mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= range.get() * range.get() &&
                        !mc.world.getBlockState(pos).isAir();

        if (spot != null && validTarget && (!hitBlock || rotationMode.is("Normal"))) {
            Client.rotationManager.setRotations(spot.rotation, 1, MovementFix.SILENT);
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isSpectator())return;

        if (Client.moduleManager.getModule(Blink.class).getState() || BlinkUtils.blinking) return;

        Rotation serverRotation = RotationManager.serverRotation;
        BlockPos currentPos = pos;

        if (mode.is("Swap") && currentPos != null && mc.world.getBlockState(currentPos).getBlock() instanceof BedBlock) {
            BlockState bedState = mc.world.getBlockState(currentPos);
            Direction facing = bedState.get(BedBlock.FACING);
            boolean isHead = bedState.get(BedBlock.PART) == BedPart.HEAD;
            BlockPos otherBedPos = isHead ? currentPos.offset(facing.getOpposite()) : currentPos.offset(facing);

            boolean hasAir = false;

            for (BlockPos bedPart : new BlockPos[]{currentPos, otherBedPos}) {
                for (Direction dir : Direction.values()) {
                    BlockPos offset = bedPart.offset(dir);
                    BlockState offsetState = mc.world.getBlockState(offset);
                    if (offsetState.isAir()) {
                        hasAir = true;
                        break;
                    }
                }
                if (hasAir) break;
            }

            if (!hasAir && breakingBlockPos != null && hitBlock) {
                breakingBlockPos = null;
                currentDamage = 0F;
                hitBlock = false;
                breakState = BreakState.NONE;
                mc.world.setBlockBreakingInfo(mc.player.getId(), currentPos, -1);
                return;
            }
        }


        BlockHitResult raytrace = performRaytrace(currentPos, serverRotation, range.get());

        if (raytrace == null && (!hitBlock || mode.is("Normal"))) {
            breakState = BreakState.NONE;
            return;
        }

        AutoTool autoTool = Client.moduleManager.getModule(AutoTool.class);
        if (Client.moduleManager.getModule(AutoTool.class).getState()) {
            autoTool.switchSlot(currentPos);
        }

        Block block = mc.world.getBlockState(currentPos).getBlock();
        if (block == null) {
            return;
        }

        if (currentDamage == 0F) {
            breakState = BreakState.PREPARE;
            if (!Client.moduleManager.getModule(KillAura.class).getState() || KillAura.currentTarget == null || KillAura.noWorking) {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, Objects.requireNonNull(currentPos), Objects.requireNonNull(raytrace).getSide()));
                hitBlock = true;
                breakState = BreakState.BREAKING;
                if (mc.player.isCreative() || mc.world.getBlockState(currentPos).getHardness(mc.world, currentPos) == 0.0f) {
                    mc.world.breakBlock(currentPos, true); // 立即破坏
                    currentDamage = 0F;
                    pos = null;
                    return;
                }
            }
        }

        BlockState state = mc.world.getBlockState(currentPos);

        // 原版的破坏进度计算(死妈微软每次改用法名都不说跟你妈逼的摸黑走路一样)
        float relativeHardness = state.calcBlockBreakingDelta(mc.player, mc.world, currentPos);
        if (relativeHardness <= 0) return;

        currentDamage += relativeHardness;

        if (hitBlock) {
            breakState = BreakState.BREAKING;
        }

        int breakStage = (int) (currentDamage * 10F);
        if (breakStage > 9) breakStage = 9;

        //让视觉看起来跟他妈原版挖掘一样
        mc.particleManager.addBlockBreakingParticles(currentPos, Direction.byId(breakStage));
        mc.world.setBlockBreakingInfo(mc.player.getId(), currentPos, breakStage);

        if (currentDamage >= 1F) {
            breakState = BreakState.FINISHING;
            if (!Client.moduleManager.getModule(KillAura.class).getState() || KillAura.currentTarget == null || KillAura.noWorking) {
                hitBlock = false;
                if (raytrace == null) {
                    breakState = BreakState.NONE;
                    return;
                }

                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(
                        new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, Objects.requireNonNull(currentPos), raytrace.getSide())
                );

                mc.world.breakBlock(currentPos, true);

                blockHitDelay = 4;
                currentDamage = 0F;
                mc.world.setBlockBreakingInfo(mc.player.getId(), currentPos, -1);
                pos = null;

                breakingBlockPos = null;

                breakState = BreakState.NONE;
            }
        }

    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;
        if (renderBox.get() && pos != null) {
            Box originalBox = new Box(pos);

            float scaleX = currentDamage;
            float scaleY = currentDamage;
            float scaleZ = currentDamage;

            double newWidth = (originalBox.maxX - originalBox.minX) * scaleX;
            double newHeight = (originalBox.maxY - originalBox.minY) * scaleY;
            double newDepth = (originalBox.maxZ - originalBox.minZ) * scaleZ;

            double centerX = (originalBox.minX + originalBox.maxX) / 2.0;
            double centerY = (originalBox.minY + originalBox.maxY) / 2.0;
            double centerZ = (originalBox.minZ + originalBox.maxZ) / 2.0;

            Box scaledBox = new Box(
                    centerX - newWidth / 2.0, centerY - newHeight / 2.0, centerZ - newDepth / 2.0,
                    centerX + newWidth / 2.0, centerY + newHeight / 2.0, centerZ + newDepth / 2.0
            );

            RenderUtils.drawBox(event.getMatrixStack(), scaledBox, ColorUtils.reAlpha(ClientSettings.firstColor.getColor(), 140), false, null, true);
        }
    }

    public static void updateClosestBlockPos() {
        if (mc.player == null || mc.world == null) return;
        if (!searchTimer.hasTimeElapsed(500L)) {
            return;
        }
        searchTimer.reset();

        double posX = mc.player.getX();
        double posY = mc.player.getY();
        double posZ = mc.player.getZ();
        List<BlockPos> targetBlockList = new ArrayList<>();
        int searchDistance = 10;
        for (int SearchX = (int) (posX - searchDistance); SearchX < (int) (posX + searchDistance); SearchX++) {
            for (int SearchY = (int) (posY - searchDistance); SearchY < (int) (posY + searchDistance); SearchY++) {
                for (int SearchZ = (int) (posZ - searchDistance); SearchZ < (int) (posZ + searchDistance); SearchZ++) {
                    BlockPos blp = new BlockPos(SearchX, SearchY, SearchZ);
                    if (mc.world.getBlockState(blp).getBlock() != net.minecraft.block.Blocks.AIR) {
                        Block block = mc.world.getBlockState(blp).getBlock();
                        if (block instanceof BedBlock) {
                            targetBlockList.add(blp);
                        }
                    }
                }
            }
        }
        if (targetBlockList.isEmpty()) {
            targetX = null;
            targetZ = null;
        } else {
            BlockPos closestBlp = getClosestBlock(mc.player.getX(), mc.player.getY(), mc.player.getZ(), targetBlockList);
            if (closestBlp != null) {
                targetX = closestBlp.getX();
                targetZ = closestBlp.getZ();
            }
        }
    }

    private static BlockPos getClosestBlock(double posX, double posY, double posZ, List<BlockPos> blpList) {
        blpList.sort((blockPosA, blockPosB) -> {
            double distanceA = blockPosA.getSquaredDistance(posX, posY, posZ);
            double distanceB = blockPosB.getSquaredDistance(posX, posY, posZ);
            return Double.compare(distanceA, distanceB);
        });
        return blpList.isEmpty() ? null : blpList.getFirst();
    }

    private BlockPos findBed() {
        ClientPlayerEntity player = Wrapper.mc.player;
        World world = Wrapper.mc.world;
        if (player == null || world == null) return null;

        int radius = (int) (range.get() + 3);
        double nearestDistance = Double.MAX_VALUE;
        BlockPos nearest = null;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = player.getBlockPos().add(x, y, z);
                    Block block = world.getBlockState(checkPos).getBlock();
                    if (!(block instanceof BedBlock)) continue;

                    double distSq = player.squaredDistanceTo(Vec3d.ofCenter(checkPos));

                    if (distSq > range.get() * range.get()) continue;

                    if (distSq < nearestDistance && (isHittable(checkPos) || mode.is("Legit") || mode.is("Swap"))) {
                        nearestDistance = distSq;
                        nearest = checkPos;
                    }
                }
            }
        }
        return nearest;
    }


    private boolean isHittable(BlockPos blockPos) {
        if (mc.world == null) return false;

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = blockPos.offset(direction);
            BlockState offsetState = mc.world.getBlockState(offsetPos);

            if (!offsetState.isFullCube(mc.world, offsetPos)) {
                return true;
            }
        }
        return false;
    }

    //By ChatGPT (操你妈AI写的算法比我厉害)
    private VecRotation getBestAimVecForBed(BlockPos bedPos) {
        if (mc.player == null) return null;
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d bestHitVec = null;
        Rotation bestRot = null;
        double bestDist = Double.MAX_VALUE;

        // 遍历方块 3x3x3 面采样点（选取可见的）
        for (double x = 0; x <= 1.0; x += 0.5) {
            for (double y = 0; y <= 1.0; y += 0.5) {
                for (double z = 0; z <= 1.0; z += 0.5) {
                    Vec3d hitVec = new Vec3d(bedPos.getX() + x, bedPos.getY() + y, bedPos.getZ() + z);
                    Rotation rot = RotationUtils.toRotation(hitVec, false);
                    // 模拟视线检测，判断是否能直接打到这个点
                    BlockHitResult hit = performRaytrace(bedPos, rot, range.get());
                    if (hit == null || !hit.getBlockPos().equals(bedPos)) continue;

                    double dist = eyePos.squaredDistanceTo(hitVec);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestHitVec = hitVec;
                        bestRot = rot;
                    }
                }
            }
        }
        // 如果没找到合适点，则 fallback 到方块中心
        if (bestHitVec == null) {
            Vec3d center = Vec3d.ofCenter(bedPos);
            bestRot = RotationUtils.toRotation(center, false);
            bestHitVec = center;
        }

        return new VecRotation(bestHitVec, bestRot);
    }
}
