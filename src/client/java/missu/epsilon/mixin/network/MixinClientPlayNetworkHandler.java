package missu.epsilon.mixin.network;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.SendCommandEvent;
import missu.epsilon.client.event.events.game.SendMessageEvent;
import missu.epsilon.client.event.events.network.BundlePacketReceiveEvent;
import missu.epsilon.client.features.modules.player.AutoAcceptPolicy;
import missu.epsilon.client.utils.ReconnectUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements ClientPlayPacketListener {

    @Shadow private ClientWorld world;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoin(CallbackInfo ci) {
        ServerInfo info = mc.getCurrentServerEntry();
        if (info != null) {
            ReconnectUtils.lastServer = new ServerInfo(info.name, info.address, info.getServerType());
        }
    }

    @ModifyArg(method = "sendChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;<init>(Ljava/lang/String;Ljava/time/Instant;JLnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/network/message/LastSeenMessageList$Acknowledgment;)V"), index = 0)
    private String modifyChatMessage(String original) {
        SendMessageEvent sendMessageEvent = new SendMessageEvent(original);
        Client.getInstance().getEventManager().call(sendMessageEvent);
        return sendMessageEvent.message;
    }

    @ModifyVariable(method = "sendCommand", at = @At("HEAD"), argsOnly = true)
    private String modifyCommand(String command) {
        SendCommandEvent event = new SendCommandEvent(command);
        Client.getInstance().getEventManager().call(event);
        return event.command;
    }

    @ModifyVariable(method = "sendChatCommand", at = @At("HEAD"), argsOnly = true)
    private String modifyChatCommand(String command) {
        SendCommandEvent event = new SendCommandEvent(command);
        Client.getInstance().getEventManager().call(event);
        return event.command;
    }

    @Inject(method = "onBundle",at = @At("HEAD"),cancellable = true)
    public void onBundle(BundleS2CPacket packet, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        for(Packet<? super ClientPlayPacketListener> packet2 : packet.getPackets()) {
            BundlePacketReceiveEvent event = new BundlePacketReceiveEvent(packet2);
            Client.getInstance().getEventManager().call(event);
            if (!event.isCancelled()) {
                packet2.apply(this);
            }
        }
    }

    @Inject(method = "onOpenWrittenBook",at = @At("HEAD"),cancellable = true)
    public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet, CallbackInfo callbackInfo) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);

        ItemStack itemStack = this.client.player.getStackInHand(packet.getHand());
        BookScreen.Contents contents = BookScreen.Contents.create(itemStack);
        if (contents != null) {

            if (Client.moduleManager.getModule(AutoAcceptPolicy.class) != null) {
                AutoAcceptPolicy module = Client.moduleManager.getModule(AutoAcceptPolicy.class);
                if (module.findClickable(contents)) {
                    callbackInfo.cancel();
                    return;
                }
            }

            this.client.setScreen(new BookScreen(contents));
        }

        callbackInfo.cancel();
    }


}

