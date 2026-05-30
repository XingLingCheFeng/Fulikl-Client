package missu.epsilon.mixin.client;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.*;
import missu.epsilon.client.event.events.init.ClientInitEvent;
import missu.epsilon.client.event.events.network.RotationAppliedEvent;
import missu.epsilon.client.event.events.network.RotationEvent;
import missu.epsilon.client.features.modules.movement.GuiMove;
import missu.epsilon.client.features.modules.movement.NoSlow;
import missu.epsilon.client.features.modules.exploit.IllegalInteract;
import missu.epsilon.client.features.modules.render.ESP;
import missu.epsilon.client.features.modules.world.ContainerStealer;
import missu.epsilon.client.utils.SmartCacheManager;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.entity.ItemSpoofUtils;
import missu.epsilon.client.utils.entity.RaycastUtils;
import missu.epsilon.client.utils.entity.RotationUtils;
import net.minecraft.SharedConstants;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Unique
    private boolean isInteractable(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ChestBlock || block instanceof DoorBlock || block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof AnvilBlock || block instanceof BedBlock || block instanceof CraftingTableBlock || block instanceof EnderChestBlock || block instanceof FurnaceBlock;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Inject(at = @At("HEAD"), method = "run")
    private void run(CallbackInfo info) {
        Client.getInstance().getEventManager().call(new ClientInitEvent());
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> callbackInfoReturnable) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Client.started) {
            stringBuilder.append("\uD835\uDCD4\uD835\uDCF9\uD835\uDCFC\uD835\uDCF2\uD835\uDCF5\uD835\uDCF8\uD835\uDCF7").append(" ").append(Client.CLIENT_VERSION).append(" ").append(Client.username).append(" | ");
        }
        stringBuilder.append("Minecraft - ");
        stringBuilder.append(SharedConstants.getGameVersion().getName());
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
            if (MinecraftClient.getInstance().getServer() != null && !MinecraftClient.getInstance().getServer().isRemote()) {
                stringBuilder.append(I18n.translate("title.singleplayer"));
            } else if (serverInfo != null && serverInfo.isRealm()) {
                stringBuilder.append(I18n.translate("title.multiplayer.realms"));
            } else if (MinecraftClient.getInstance().getServer() == null && (serverInfo == null || !serverInfo.isLocal())) {
                stringBuilder.append(I18n.translate("title.multiplayer.other"));
            } else {
                stringBuilder.append(I18n.translate("title.multiplayer.lan"));
            }
        }
        callbackInfoReturnable.setReturnValue(stringBuilder.toString());
    }

    @Redirect(
            method = "setScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;unlockCursor()V")
    )
    private void onUnlockCursor(Mouse instance, Screen screen) {

        if (screen instanceof GenericContainerScreen && GuiMove.noUnlockMouse.get() && ContainerStealer.isVanillaChest(screen)) {
            return;
        }

        instance.unlockCursor();
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V", shift = At.Shift.BEFORE))
    public void hookRotation(CallbackInfo ci) {
        if (mc.player != null) {
            Client.getInstance().getEventManager().call(new RotationEvent());
            if (mc.currentScreen != null) {
                Client.getInstance().getEventManager().call(new RotationAppliedEvent());
            }
        }
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V", shift = At.Shift.BEFORE))
    public void hookRotationApplied(CallbackInfo ci) {
        Client.getInstance().getEventManager().call(new RotationAppliedEvent());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 2))
    public void onPreClientUpdate(CallbackInfo callbackInfo) {
        Client.getInstance().getEventManager().call(new PreClientUpdateEvent());
    }
    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void onPostClientUpdate(CallbackInfo callbackInfo) {
        Client.getInstance().getEventManager().call(new PostClientUpdateEvent());
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void onClick(CallbackInfo ci) {
        Client.getInstance().getEventManager().call(new ClickEvent());
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void redirectStopUsing(ClientPlayerInteractionManager manager, PlayerEntity player) {
        if (NoSlow.allowStopUsingItem()) {
            manager.stopUsingItem(player);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo info) {
        if (mc.player != null) {
            if (mc.player.isOnGround()) {
                ClientData.setOnGroundTicks(ClientData.getOnGroundTicks() + 1);
                ClientData.setOffGroundTicks(0);
            } else {
                ClientData.setOnGroundTicks(0);
                ClientData.setOffGroundTicks(ClientData.getOffGroundTicks() + 1);
            }
        } else {
            ClientData.setOnGroundTicks(0);
            ClientData.setOffGroundTicks(0);
        }
        if (Client.getInstance().getEventManager() != null) {
            TickEvent tickevent = new TickEvent();
            Client.getInstance().getEventManager().call(tickevent);
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void setWorld(ClientWorld world, CallbackInfo ci) {
        ItemSpoofUtils.reset();
        Client.getInstance().getEventManager().call(new WorldEvent());
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void onDoItemUseHead(CallbackInfo ci) {
        if (mc.player == null || mc.world == null) return;
        NoSlow.interactingBlockThisTick = false;
        if (!NoSlow.sword.get() || !NoSlow.swordMode.is("Hypixel")) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        if (mc.crosshairTarget instanceof BlockHitResult bhr) {
            BlockState state = mc.world.getBlockState(bhr.getBlockPos());
            if (isInteractable(state)) {
                NoSlow.interactingBlockThisTick = true;
            }
        }
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult injected$itemUse(ClientPlayerInteractionManager manager, PlayerEntity player, Hand hand) {
        if (Client.moduleManager.getModule(NoSlow.class).getState() && this.player != null && NoSlow.sword.get() && NoSlow.swordMode.is("Hypixel") && this.player.getMainHandStack().getItem() instanceof SwordItem) {
            return ActionResult.PASS;
        }
        return manager.interactItem(player, hand);
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"), cancellable = true)
    private void injectExtendedEntityInteract(CallbackInfo ci) {
        IllegalInteract illegalInteract = Client.moduleManager.getModule(IllegalInteract.class);
        if (mc.player == null || mc.world == null || mc.interactionManager == null || !illegalInteract.isEnabled() || !IllegalInteract.farInteract.get()) return;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            return;
        }
        EntityHitResult entityHit = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), IllegalInteract.interactRange.get(), IllegalInteract.throughEntity.get());
        if (entityHit == null) return;
        Entity entity = entityHit.getEntity();
        if (!entity.isAlive() || entity == mc.player || !mc.world.getWorldBorder().contains(entity.getBlockPos())) {
            return;
        }
        for (Hand hand : Hand.values()) {
            ItemStack stack = mc.player.getStackInHand(hand);
            if (!stack.isItemEnabled(mc.world.getEnabledFeatures())) continue;
            ActionResult result = mc.interactionManager.interactEntityAtLocation(mc.player, entity, entityHit, hand);
            if (!result.isAccepted()) {
                result = mc.interactionManager.interactEntity(mc.player, entity, hand);
            }
            if (result instanceof ActionResult.Success success) {
                if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                    mc.player.swingHand(hand);
                }
                ci.cancel();
                return;
            }
        }
    }

    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;" +
                            "interactBlock(" +
                            "Lnet/minecraft/client/network/ClientPlayerEntity;" +
                            "Lnet/minecraft/util/Hand;" +
                            "Lnet/minecraft/util/hit/BlockHitResult;" +
                            ")Lnet/minecraft/util/ActionResult;"))
    private ActionResult doItemUseRedirect(ClientPlayerInteractionManager manager, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        IllegalInteract illegalInteract = Client.moduleManager.getModule(IllegalInteract.class);
        if (illegalInteract.isEnabled()) {
            if (IllegalInteract.throughChest.get()) {
                var chest = RaycastUtils.findChestOnSightWithPoint(player, 4.5);
                if (chest != null) {
                    var hitVec3 = chest.hit().subtract(chest.face().getOffsetX() * 1.0e-4, chest.face().getOffsetY() * 1.0e-4, chest.face().getOffsetZ() * 1.0e-4);
                    if (hitVec3.distanceTo(player.getPos()) < 4.5) {
                        return manager.interactBlock(player, hand, new BlockHitResult(hitVec3, chest.face(), chest.pos(), false));
                    }
                }
            }
            // -- Entity --
            if (IllegalInteract.throughEntity.get()) {
                EntityHitResult entityHit = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), (IllegalInteract.farInteract.get()) ? IllegalInteract.interactRange.get() : 3.0, true);
                if (entityHit != null) {
                    Entity entity = entityHit.getEntity();
                    if (entity.isAlive() && entity != player && player.getWorld().getWorldBorder().contains(entity.getBlockPos())) {
                        ActionResult result = manager.interactEntityAtLocation(player, entity, entityHit, hand);
                        if (!result.isAccepted()) {
                            result = manager.interactEntity(player, entity, hand);
                        }
                        if (result instanceof ActionResult.Success success) {
                            if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                                player.swingHand(hand);
                            }
                        }
                        return result;
                    }
                }
            }
        }
        if (Client.moduleManager.getModule(NoSlow.class).getState() && NoSlow.sword.get() && NoSlow.swordMode.is("Hypixel") && player.getMainHandStack().getItem() instanceof SwordItem && !NoSlow.interactingBlockThisTick) {
            return ActionResult.PASS;
        }
        return manager.interactBlock(player, hand, hitResult);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Inject(at = @At("HEAD"), method = "stop")
    private void stop(CallbackInfo info) {
        // This code is injected into the start of MinecraftClient.run()V
        try {
            Client.stopClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = {"hasOutline"}, at = {@At("RETURN")}, cancellable = true)
    private void shouldEntityAppearGlowing(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ESP.shouldGlow(pEntity) && ESP.glow.get()) {
            cir.setReturnValue(true);
        }
    }

    @Shadow @Nullable public ClientWorld world;
    @Shadow @Final public GameOptions options;
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public Screen currentScreen;
    @Shadow @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Unique private int optimization$tickCounter = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onClientTick(CallbackInfo ci) {
        if (this.world != null) {
            this.optimization$tickCounter++;
            int OPTIMIZATION$CHECK_INTERVAL = 30000;
            if (this.optimization$tickCounter >= OPTIMIZATION$CHECK_INTERVAL) {
                SmartCacheManager.pruneOldEntries();
                this.optimization$tickCounter = 0;
            }
        }
    }

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen modifyScreen(Screen screen) {
        if (Client.getInstance().getEventManager() != null) {
            Client.getInstance().getEventManager().call(new ScreenEvent(screen));
        }
        return screen;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        System.out.println("[Epsilon] 检测到断开连接，正在强制清空缓存...");
        SmartCacheManager.clear();
        this.optimization$tickCounter = 0;
    }

}
