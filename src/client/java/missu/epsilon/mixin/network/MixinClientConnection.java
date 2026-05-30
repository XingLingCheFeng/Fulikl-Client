package missu.epsilon.mixin.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.network.SyncSendPacketEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.utils.packets.PacketUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.concurrent.RejectedExecutionException;

@Renamer(obfuscated = false)
@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

    @Shadow private Channel channel;
    @Shadow private PacketListener packetListener;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int packetsReceivedCounter;
    @Shadow public abstract void disconnect(Text disconnectReason);
    @Shadow private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {}

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"), cancellable = true)
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (this.channel.isOpen()) {
            PacketListener packetListener = this.packetListener;
            if (packetListener == null) {
                throw new IllegalStateException("Received a packet before the packet listener was initialized");
            } else {
                if (packetListener.accepts(packet)) {
                    try {
                        PacketEvent event = new PacketEvent(packet, CancellableEvent.PacketState.RECEIVE);
                        Client.getInstance().getEventManager().call(event);

                        if (!event.isCancelled()) {
                            handlePacket(event.getPacket(), packetListener);
                        }
                    } catch (OffThreadException ignored) {
                    } catch (RejectedExecutionException e) {
                        this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
                    } catch (ClassCastException e) {
                        LOGGER.error("Received {} that couldn't be processed", packet.getClass(), e);
                        this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
                    }
                    this.packetsReceivedCounter++;
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void sendPackets(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (PacketUtils.getPackets().contains(packet)) {
            PacketUtils.getPackets().remove(packet);
        } else {
            SyncSendPacketEvent event = new SyncSendPacketEvent(packet);
            Client.getInstance().getEventManager().call(event);
            if (event.isCancelled()) {
                PacketUtils.getPackets().remove(packet);
                ci.cancel();
            } else {
                if (PacketUtils.getPackets().contains(packet)) {
                    PacketUtils.getPackets().remove(packet);
                } else {
                    PacketEvent packetEvent = new PacketEvent(packet, CancellableEvent.PacketState.SEND);
                    Client.getInstance().getEventManager().call(packetEvent);

                    if (packetEvent.isCancelled()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

}
