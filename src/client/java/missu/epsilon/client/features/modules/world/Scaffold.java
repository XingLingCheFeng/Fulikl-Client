package missu.epsilon.client.features.modules.world;

import lombok.Getter;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.ClickEvent;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.events.player.TickMovementEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.EnumAutoDisableType;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.AntiKnockback;
import missu.epsilon.client.features.modules.movement.AntiVoid;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.Priority;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.management.rotation.SmoothMode;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.*;
import missu.epsilon.client.utils.miscs.RandomUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.scaffold.PlaceInfo;
import missu.epsilon.client.utils.scaffold.ScaffoldUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;


/**
 * @author Jon_awa && XiaoMai
 */
@ModuleInfo(name = "Scaffold", category = ModuleCategory.WORLD, autoDisable = EnumAutoDisableType.GAME_END)
public class Scaffold extends Module implements Wrapper {
    private final ListValue mode = new ListValue("Mode", new String[]{"Normal", "Telly"}, "Normal");
    private final ListValue pickMode = new ListValue("Pick Mode", new String[]{"Normal", "Spoof", "Off"}, "Normal");
    private final ListValue swingMode = new ListValue("Swing Mode", new String[]{"Client", "Server", "Off"}, "Client");
    private final ListValue rotationPoint = new ListValue("Rotation Point", new String[]{"Normal", "Corner", "Nearest","Reduced"}, "Normal");
    public final NumberValue searchStep = new NumberValue("Rotation search step",0.02,0.01,0.1,0.01).displayable(() -> rotationPoint.is("Reduced"));
    public final NumberValue inset = new NumberValue("Inset",0.1,0.05,0.1,0.01).displayable(() -> rotationPoint.is("Reduced"));
    public final BoolValue jitter = new BoolValue("Jitter", true).displayable(() -> rotationPoint.is("Reduced"));
    private final ListValue rotationMode = new ListValue("Rotation Mode", new String[]{"Normal", "Smooth", "Hypixel","Heypixel"}, "Normal");
    private final ListValue searchMode = new ListValue("Search Mode", new String[]{"Normal", "Distance"}, "Normal");
    public static ListValue switchMode = new ListValue("SwitchBlock Mode",new String[]{"Normal","Biggest","Threshold"},"Biggest");
    public static NumberValue switchThreshold = new NumberValue("Switch Threshold", 5, 0, 64, 1);
    private final NumberValue rotationSpeed = (NumberValue) new NumberValue("Rotation Speed", 10, 1, 10, 1).displayable(() -> this.rotationMode.is("Smooth"));

    private final ListValue raycast = new ListValue("RayCast", new String[]{"Simple","Normal", "Medium", "Off"}, "Off");
    private final ListValue placeDelayMode = (ListValue) new ListValue("Placeable Mode", new String[]{"Check Height", "Check Tick", "Off"}, "Check Height").displayable(() -> this.mode.is("Telly") && !this.rotationMode.is("Hypixel"));
    private final NumberValue height = (NumberValue) new NumberValue("Height", 0.5, 0, 1.25, 0.05).displayable(() -> this.mode.is("Telly") && this.placeDelayMode.is("Check Height") && !this.rotationMode.is("Hypixel"));
    private final NumberValue upTellyHeight = (NumberValue) new NumberValue("UpTelly Height", 0.2, 0, 1.25, 0.05).displayable(() -> this.mode.is("Telly") && this.placeDelayMode.is("Check Height") && !this.rotationMode.is("Hypixel") && !addons.get("Keep Y"));
    private final NumberValue tick = (NumberValue) new NumberValue("Tick", 3, 0, 5, 1).displayable(() -> this.mode.is("Telly") && this.placeDelayMode.is("Check Tick") && !this.rotationMode.is("Hypixel"));
    private final NumberValue upTellyTick = (NumberValue) new NumberValue("UpTelly Tick", 1, 0, 5, 1).displayable(() -> this.mode.is("Telly") && this.placeDelayMode.is("Check Tick") && !this.rotationMode.is("Hypixel") && !addons.get("Keep Y"));
    private final NumberValue jumpDelayMax = (NumberValue) new NumberValue("Max Jump Delay", 2, 0, 3, 1).displayable(() -> this.mode.is("Telly"));
    private final NumberValue jumpDelayMin = (NumberValue) new NumberValue("Min Jump Delay", 1, 0, 3, 1).displayable(() -> this.mode.is("Telly"));
    private final NumberValue placeDelayMax = new NumberValue("Max Place Delay", 50, 0, 200, 1);
    private final NumberValue placeDelayMin = new NumberValue("Min Place Delay", 0, 0, 200, 1);

    private final BoolValue slow = new BoolValue("Slow", false);
    private static final MultiBoolValue addons = new MultiBoolValue("Addons", new BoolValue[]{
            new BoolValue("Keep Y", false),
            new BoolValue("Auto Sneak", false),
            new BoolValue("Auto Stuck", false),
            new BoolValue("Render Block", false)
    });
    public final ListValue countMode = new ListValue("Counter", new String[]{"Top", "None"}, "Top");
    public static final BoolValue attack = new BoolValue("Attack", false);

    private final TimerUtils placeTimer = new TimerUtils();
    public static int blockCount = 0;
    public int tempCount = 0;
    private PlaceInfo lastPlaceInfo;
    private Rotation lastRotation;
    private boolean isSpoofing;
    private boolean canRotate;
    @Getter
    private boolean canPlace;
    @Getter
    private boolean enabledStuck;
    private boolean notification;
    private int rotationTick;
    private int stuckTicks;
    private int placeY;
    private int baseY;
    private int stage;
    private boolean enabledOnAir;
    public static boolean canAttack = false;
    private boolean yawNormal;
    public int offGroundTicks;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            canAttack = false;
            this.baseY = this.placeY = (int) Math.floor(mc.player.getY() - 1);
            this.lastPlaceInfo = null;
            this.lastRotation = null;
            this.placeTimer.reset();
            this.enabledStuck = false;
            this.isSpoofing = false;
            this.canRotate = false;
            this.stuckTicks = 0;
            this.notification = false;
            offGroundTicks = 0;
            blockCount = getBlockCount();

            if (this.pickMode.is("Spoof")) {
                ItemSpoofUtils.startSpoof();
                this.isSpoofing = true;
            }

            if (ClientData.getOffGroundTicks() > 0){
                enabledOnAir = true;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        yawNormal = false;
        this.notification = false;
        enabledOnAir = false;
        offGroundTicks = 0;
        canAttack = false;
        if (this.isSpoofing) {
            ItemSpoofUtils.stopSpoof();
            this.isSpoofing = false;
        }

        if (this.enabledStuck) {
            Client.moduleManager.getModule(AntiVoid.class).setState(false);
        }

        mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.sneakKey.getDefaultKey().getCode()));
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (tempCount != getBlockCount() && getBlockCount() <= 10) {
                NotificationManager.post(NotificationType.WARRING, "Low Block Count " + getBlockCount() + " Block");
                tempCount = getBlockCount();
            }

            if (addons.get("Auto Sneak")) {
                if (PlayerUtils.getBlockUnderPlayer(mc.player) instanceof AirBlock) {
                    if (mc.player.isOnGround()) {
                        mc.options.sneakKey.setPressed(true);
                    }
                } else if (mc.player.isOnGround()) {
                    mc.options.sneakKey.setPressed(false);
                }
            }
        }
    }

    @EventTarget
    public void onTickMovement(ClickEvent event) {
        if (mc.player == null) {
            return;
        }

        if (enabledOnAir && ClientData.getOnGroundTicks() > 0){
            enabledOnAir = false;
        }

        if (mc.player != null && mc.world != null) {
            if (!this.pickMode.is("Off") && !ItemUtils.isValidBlock(mc.player.getOffHandStack(), true)) {
                var inv = mc.player.getInventory();
                int currentSlot = inv.selectedSlot;
                ItemStack current = inv.getStack(currentSlot);

                boolean needSwitch = current.isEmpty() || !(current.getItem() instanceof BlockItem blockItem) || !ItemUtils.isNotInBlockBlacklist(blockItem.getBlock()) || current.getCount() <= switchThreshold.get() || !switchMode.is("Threshold");

                if (needSwitch) {
                    int blockSlot = getBlockSlot();
                    if (blockSlot != -1 && blockSlot != currentSlot) {
                        inv.selectedSlot = blockSlot;
                    } else if (blockSlot == -1) {
                        NotificationManager.post(NotificationType.WARRING, "No blocks");
                        this.setState(false);
                    }
                }
            }
        }

        if (mc.player.onGround) {
            this.offGroundTicks = 0;
        } else if (shouldBuild()){
            this.offGroundTicks++;
        }

        if (mc.player.onGround && ClientData.getOnGroundTicks() <= 1) {
            float ny = MathHelper.wrapDegrees(mc.player.getYaw() - getHardOffset());
            float normalizedYaw = (ny % 360 + 360) % 360;
            float quad = normalizedYaw % 90;
            yawNormal = quad < 45;
        }

        if (mc.world != null && mc.interactionManager != null) {
            if (this.mode.is("Telly") && (this.rotationMode.is("Hypixel") || this.rotationMode.is("Heypixel"))) {
                if (this.rotationTick > 0) {
                    this.rotationTick--;
                }

                if (mc.player.isOnGround()) {
                    if (this.stage > 0) {
                        this.stage--;
                    }

                    if (this.stage < 0) {
                        this.stage++;
                    }

                    if (this.stage == 0 && !addons.get("Keep Y") && !mc.options.jumpKey.isPressed()) {
                        this.stage = 1;
                    }

                    this.canRotate = false;
                    this.rotationTick = 0;
                }
            }

            this.placeY = this.getPlaceY();

            if (this.mode.is("Telly") && !this.rotationMode.is("Hypixel") && !this.rotationMode.is("Heypixel")) {
                switch (this.placeDelayMode.getValue()) {
                    case "Check Height" -> {
                        if (ClientData.getFallDistance() == 0 && mc.player.getY() < this.placeY + 1 + (mc.options.jumpKey.isPressed() && !addons.get("Keep Y") ? this.upTellyHeight.getValue() : this.height.getValue()) && mc.player.hurtTime == 0) {
                            this.canPlace = false;
                            return;
                        }
                    }

                    case "Check Tick" -> {
                        if (offGroundTicks < (mc.options.jumpKey.isPressed() && !addons.get("Keep Y") ? this.upTellyTick.getValue() : this.tick.getValue())) {
                            this.canPlace = false;
                            return;
                        }
                    }
                }
            }

            var basePos = BlockPos.ofFloored(mc.player.getX(), this.placeY, mc.player.getZ());

            var placeInfo = ScaffoldUtils.getPlaceInfo(basePos, switch (this.searchMode.getValue()) {
                case "Normal" -> ScaffoldUtils.SearchMode.Normal;
                case "Distance" -> ScaffoldUtils.SearchMode.Hypixel;
                default -> throw new IllegalStateException("Unexpected value: " + this.searchMode.getValue());
            });

            if (placeInfo == null && this.lastPlaceInfo != null) {
                placeInfo = this.lastPlaceInfo;
            }

            if (placeInfo != null) {
                this.lastPlaceInfo = placeInfo;

                var pair = switch (this.rotationPoint.getValue()) {
                    case "Normal" ->
                            placeInfo.getRotationAndHitVec(PlaceInfo.RotatePoint.Normal, (this.rotationMode.is("Hypixel") || this.rotationMode.is("Heypixel")) ? (this.lastRotation != null ? this.lastRotation : RotationUtils.getRotationOrElseMC()) : RotationManager.serverRotation);
                    case "Corner" ->
                            placeInfo.getRotationAndHitVec(PlaceInfo.RotatePoint.Corner, (this.rotationMode.is("Hypixel") || this.rotationMode.is("Heypixel")) ? (this.lastRotation != null ? this.lastRotation : RotationUtils.getRotationOrElseMC()) : RotationManager.serverRotation);
                    case "Nearest" ->
                            placeInfo.getRotationAndHitVec(PlaceInfo.RotatePoint.Nearest, (this.rotationMode.is("Hypixel") || this.rotationMode.is("Heypixel")) ? (this.lastRotation != null ? this.lastRotation : RotationUtils.getRotationOrElseMC()) : RotationManager.serverRotation);
                    case "Reduced" ->
                            placeInfo.getRotationAndHitVec(PlaceInfo.RotatePoint.Reduced, (this.rotationMode.is("Hypixel") || this.rotationMode.is("Heypixel")) ? (this.lastRotation != null ? this.lastRotation : RotationUtils.getRotationOrElseMC()) : RotationManager.serverRotation);


                    default -> throw new IllegalStateException("Unexpected value: " + this.rotationPoint.getValue());
                };

                if (pair != null) {
                    var rotation = pair.getLeft();
                    var hitVec = pair.getRight();

                    if (rotation != null) {
                        this.canPlace = true;

                        switch (this.rotationMode.getValue()) {
                            case "Normal" -> Client.rotationManager.setRotations(rotation, MovementFix.SILENT,false, SmoothMode.ADVANCED,180F,0,0, Priority.MEDIUM);
                            case "Smooth" ->
                                    Client.rotationManager.setRotations(rotation, MovementFix.SILENT,true, SmoothMode.ADVANCED, rotationSpeed.get().floatValue() / 10.0F * 180F,0,0, Priority.MEDIUM);
                            case "Hypixel" -> {
                                if (this.lastRotation == null) {
                                    this.lastRotation = RotationManager.serverRotation;
                                }


                                if ((this.canRotate && (mc.player.getVelocity().y > 0 || mc.player.getY() > this.placeY + 1))) {
                                    float speed;
                                    if (enabledOnAir) {
                                        speed = offGroundTicks <= 1 ? 100 : 30;
                                    } else if (slow.get()) {
                                        speed = offGroundTicks <= 1 ? 90 : 37;
                                    } else {
                                        speed = offGroundTicks <= 1 ? 127 : 35;
                                    }

                                    var deltaYaw = MathHelper.wrapDegrees(rotation.getYaw() - this.lastRotation.getYaw());
                                    if (Math.abs(deltaYaw) > speed) {
                                        rotation.setYaw(RotationUtils.quantize(this.lastRotation.getYaw() + RotationUtils.clampYaw(deltaYaw, speed)));
                                        this.rotationTick = 0;
                                    }

                                }

                                if (mc.player.isOnGround()
                                        && (mc.options.forwardKey.isPressed() != mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() != mc.options.rightKey.isPressed())
                                        && PlayerUtils.isAirAbove(mc.player, mc.world)
                                ) {
                                    boolean sb = MathHelper.wrapDegrees(lastRotation.getYaw() - mc.player.getYaw() - getHardOffset()) > 0;
                                    if (this.stage > 0 || (!addons.get("Keep Y") && mc.options.jumpKey.isPressed())) {
                                        if (ClientData.getOnGroundTicks() >= jumpDelayMin.get()) {
                                            if (rotationMode.get().equals("Hypixel")) {
                                                rotation = new Rotation(mc.player.getYaw() - getHardOffset() + (isDiagonal() ? yawNormal ? 157 : -157 : sb ? 157 : -157), 80f);
                                                this.rotationTick = 0;
                                                this.canRotate = true;
                                            } else {
                                                rotation = new Rotation(mc.player.getYaw() - getHardOffset() - 180, 80);
                                                this.rotationTick = 0;
                                                this.canRotate = true;
                                            }
                                        }
                                    }
                                }

                                Client.rotationManager.setRotations(this.lastRotation = rotation, AntiKnockback.shouldStrict ? MovementFix.STRICT : MovementFix.SILENT,false, SmoothMode.ADVANCED,180F,0,0, Priority.MEDIUM);
                            }
                            case "Heypixel" -> {
                                if (this.lastRotation == null) {
                                    this.lastRotation = RotationUtils.getRotationOrElseMC();
                                }

                                if (this.canRotate && (mc.player.getVelocity().y > 0 || mc.player.getY() > this.placeY + 1)) {
                                    var speed = this.rotationTick >= 2 ? RandomUtils.getRandom(90.0F, 95.0F) : RandomUtils.getRandom(30.0F, 36.0F);
                                    var deltaYaw = MathHelper.wrapDegrees(rotation.getYaw() - this.lastRotation.getYaw());

                                    if (Math.abs(deltaYaw) > speed) {
                                        rotation.setYaw(RotationUtils.quantize(this.lastRotation.getYaw() + RotationUtils.clampYaw(deltaYaw, speed)));
                                        this.rotationTick = 0;
                                    }
                                }

                                if (mc.player.isOnGround()
                                        && (mc.options.forwardKey.isPressed() != mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() != mc.options.rightKey.isPressed())
                                        && PlayerUtils.isAirAbove(mc.player, mc.world)
                                ) {
                                    if (this.stage > 0 || (!addons.get("Keep Y") && mc.options.jumpKey.isPressed())) {
                                        rotation = RotationUtils.getRotationOrElseMC();
                                        this.rotationTick = 3;
                                        this.canRotate = true;
                                    }
                                }

                                Client.rotationManager.setRotations(this.lastRotation = rotation, MovementFix.SILENT,false, SmoothMode.ADVANCED,180F,1,0, Priority.MEDIUM);

                            }
                        }

                        var mainHandHasBlock = ItemUtils.isNotInBlockBlacklist(mc.player.getMainHandStack());
                        var offHandHasBlock = ItemUtils.isNotInBlockBlacklist(mc.player.getOffHandStack());

                        if (!offHandHasBlock && !mainHandHasBlock) {
                            return;
                        }

                        if (hitVec != null && shouldBuild()) {
                            if (ScaffoldUtils.canPlaceAt(BlockPos.ofFloored(mc.player.getX(), this.placeY, mc.player.getZ()))) {
                                var placeDelay = RandomUtils.getRandom(Math.min(this.placeDelayMin.getValue(), this.placeDelayMax.getValue()), Math.max(this.placeDelayMin.getValue(), this.placeDelayMax.getValue()));

                                if (placeDelay != 0 && !this.placeTimer.hasTimeElapsed((long) placeDelay, true)) {
                                    return;
                                }

                                var hand = mainHandHasBlock ? Hand.MAIN_HAND : Hand.OFF_HAND;
                                var hitResult = new BlockHitResult(hitVec, placeInfo.getDirection(), placeInfo.getBlockPos(), false);

                                if (mc.player.getEyePos().squaredDistanceTo(hitVec) > 20.25 && rotationMode.is("Heypixel")) {
                                    return;
                                }

                                if (this.stuckTicks < 0) {
                                    ++this.stuckTicks;
                                }

                                if (addons.get("Auto Stuck")
                                        && ((mc.player.getEyePos().distanceTo(hitVec) > 3 && mc.player.getY() - this.placeY + 1 < 0.15)
                                        || (mc.player.getEyePos().distanceTo(hitVec) > 2 && mc.player.getY() - this.placeY + 1 < 0.1)
                                        || (mc.player.getEyePos().distanceTo(hitVec) > 4 && mc.player.getY() - this.placeY + 1 < 0.25)
                                        || (mc.player.getEyePos().distanceTo(hitVec) > 4.2)
                                        || (ClientData.getFallDistance() > 2))
                                        && this.stuckTicks >= 0
                                        && this.stuckTicks <= 20
                                        && !PlayerUtils.isOnGround(mc.player, 2)
                                        && !PlayerUtils.isOnGround(mc.player, 1.5)
                                        && !PlayerUtils.isOnGround(mc.player, 1)
                                        && !PlayerUtils.isOnGround(mc.player, 0.5)
                                        && !mc.player.isOnGround()
                                ) {
//                                    antiVoid.setState(true);
                                    this.enabledStuck = true;
                                    ++this.stuckTicks;
                                } else {
//                                    antiVoid.setState(false);
                                    this.enabledStuck = false;

                                    if (this.stuckTicks > 0) {
                                        this.stuckTicks = -2;
                                    }
                                }

                                switch (this.raycast.getValue()) {
                                    case "Simple" ->{
                                        Rotation calculated = pair.getLeft();
                                        var deltaYaw = MathHelper.wrapDegrees(rotation.getYaw() - calculated.getYaw());
                                        if (Math.abs(deltaYaw) > 35.0f) return;
                                    }
                                    case "Normal" -> {
                                        if (!placeInfo.getBlockPos().equals(RaycastUtils.rayCastBlock(4.5))) {
                                            return;
                                        }
                                    }
                                    case "Medium" -> {
                                        if (!placeInfo.getBlockPos().equals(RaycastUtils.rayCastBlock(RotationManager.serverRotation, 4.5))) {
                                            return;
                                        }
                                    }
                                }

                                if (!this.mode.is("Telly") || (offGroundTicks > 2 || mc.player.onGround && ClientData.getOnGroundTicks() < jumpDelayMin.get()) && !rotationMode.is("Heypixel")) {
                                    mc.interactionManager.interactBlock(mc.player, hand, hitResult);
                                    if (this.swingMode.is("Client")) {
                                        mc.player.swingHand(hand);
                                    } else if (this.swingMode.is("Server")) {
                                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                                    }

                                    this.placeTimer.reset();
                                }

                                if (rotationMode.is("Heypixel")) {
                                    if (mc.interactionManager.interactBlock(mc.player, hand, hitResult) == ActionResult.SUCCESS) {
                                        if (this.swingMode.is("Client")) {
                                            mc.player.swingHand(hand);
                                        } else if (this.swingMode.is("Server")) {
                                            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                                        }

                                        this.placeTimer.reset();
                                    }
                                }
                            }
                        }
                    } else {
                        this.canPlace = false;
                    }
                } else {
                    this.canPlace = false;
                }
            } else {
                this.canPlace = false;
            }
        }
    }

    private boolean shouldBuild() {
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());
        return mc.world.isAir(playerPos);
    }


    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.mode.is("Telly") && PlayerUtils.isMoving()) {
                var jumpDelay = RandomUtils.getRandom(Math.min(this.jumpDelayMin.getValue(), this.jumpDelayMax.getValue()), Math.max(this.jumpDelayMin.getValue(), this.jumpDelayMax.getValue()));
                event.jump = jumpDelay == 0 || ClientData.getOnGroundTicks() >= jumpDelay;
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player != null && addons.get("Render Block") && this.lastPlaceInfo != null) {
            RenderUtils.drawBox(event.getMatrixStack(), new Box(this.lastPlaceInfo.getBlockPos().offset(this.lastPlaceInfo.getDirection())), ClientSettings.color(0), false, null, true);
        }
    }

    @EventTarget
    public void onWorldChange(WorldEvent event) {
        this.setState(false);
    }

    private int getPlaceY() {
        assert mc.player != null;

        if (mc.options.jumpKey.isPressed() && !addons.get("Keep Y")) {
            return this.baseY = (int) Math.floor(mc.player.getY() - 1);
        } else {
            return (this.baseY > mc.player.getY() - 1 || mc.player.isOnGround()) ? this.baseY = (int) Math.floor(mc.player.getY() - 1) : this.baseY;
        }
    }

    public static int getBlockSlot() {
        assert mc.player != null;

        if (switchMode.is("Biggest") || switchMode.is("Threshold")) {
            int bestSlot = -1;
            int maxCount = -1;
            var inv = mc.player.getInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
                if (!ItemUtils.isNotInBlockBlacklist(blockItem.getBlock())) continue;

                int count = stack.getCount();
                if (count > maxCount) {
                    maxCount = count;
                    bestSlot = i;
                }
            }
            return bestSlot;
        }

        if (switchMode.is("Normal")) {
            for (int i = 0; i < 9; ++i) {
                ItemStack itemStack = mc.player.getInventory().getStack(i);
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem && ItemUtils.isNotInBlockBlacklist(blockItem.getBlock())) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean isDiagonal() {
        if (mc.player == null) return false;
        return Math.abs(mc.player.getVelocity().x) > 0.07 && Math.abs(mc.player.getVelocity().z) > 0.07;
    }


    public static float getHardOffset() {
        if (mc.currentScreen != null) {
            return 180;
        }

        float simpleYaw = 0F;
        boolean w = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        boolean s = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        boolean a = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        boolean d = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());

        boolean dupe = a & d;

        if (w) {
            simpleYaw -= 180;
            if (!dupe) {
                if (a) simpleYaw += 45;
                if (d) simpleYaw -= 45;
            }
        } else if (!s) {
            simpleYaw -= 180;
            if (!dupe) {
                if (a) simpleYaw += 90;
                if (d) simpleYaw -= 90;
            }
        } else {
            if (!dupe) {
                if (a) simpleYaw -= 45;
                if (d) simpleYaw += 45;
            }
        }
        return simpleYaw;
    }

    public static int getBlockCount() {
        assert mc.player != null;

        int count = 0;

        if (ItemUtils.isNotInBlockBlacklist(mc.player.getOffHandStack())) {
            count += mc.player.getOffHandStack().getCount();
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && ItemUtils.isNotInBlockBlacklist(stack)) {
                count += stack.getCount();
            }
        }

        return Client.moduleManager.getModule(Scaffold.class).isEnabled() ? count : 0;
    }
}