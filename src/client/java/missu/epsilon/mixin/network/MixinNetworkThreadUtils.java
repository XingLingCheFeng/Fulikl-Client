package missu.epsilon.mixin.network;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.SyncApplyPacketEvent;
import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static missu.epsilon.client.utils.Wrapper.mc;

@Mixin(NetworkThreadUtils.class)
public abstract class MixinNetworkThreadUtils {

    @Shadow @Final private static Logger LOGGER;

    /**
     * @author Jon_awa
     * @reason for SyncApplyPacketEvent
     */
    @Overwrite
    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ThreadExecutor<?> engine) throws OffThreadException {
        if (!engine.isOnThread()) {
            engine.executeSync(() -> {
                if (listener.accepts(packet)) {
                    try {
                        if (packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket) {
                            if (mc.player != null && entityVelocityUpdateS2CPacket.getEntityId() == mc.player.getId()) {
                                ClientData.setTicksSinceVelocity(0);
                            }
                        } else if (packet instanceof ExplosionS2CPacket explosionS2CPacket) {
                            if (explosionS2CPacket.playerKnockback().isPresent()) {
                                ClientData.setTicksSinceVelocity(0);
                            }
                        } else if (packet instanceof PlayerPositionLookS2CPacket) {
                            ClientData.setTicksSinceTeleport(0);
                        } else if (packet instanceof EntityPositionS2CPacket entityPositionS2CPacket) {
                            if (mc.world != null && mc.world.getEntityById(entityPositionS2CPacket.entityId()) == null) {
                                ClientData.setTicksSinceTeleport(0);
                            }
                        }
                        if (engine.isOnThread()) {
                            var event = new SyncApplyPacketEvent(packet);
                            Client.getInstance().getEventManager().call(event);
                            if (event.isCancelled()) {
                                return;
                            }
                        }
                        packet.apply(listener);
                    } catch (Exception var4) {
                        if (var4 instanceof CrashException crashException && crashException.getCause() instanceof OutOfMemoryError) {
                            throw NetworkThreadUtils.createCrashException(var4, packet, listener);
                        }
                        listener.onPacketException(packet, var4);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
                }
            });
            throw OffThreadException.INSTANCE;
        }
    }

}
