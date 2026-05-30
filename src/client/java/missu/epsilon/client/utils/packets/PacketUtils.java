package missu.epsilon.client.utils.packets;

import lombok.Getter;
import missu.epsilon.mixin.client.world.ClientWorldAccessor;
import missu.epsilon.mixin.network.packet.c2s.play.PlayerMoveC2SPacketAccessor;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

public class PacketUtils {
    @Getter
    public static final ArrayList<Packet<?>> packets = new ArrayList<>();

    public static boolean handleSendPacket(Packet<?> packet) {
        if (packets.contains(packet)) {
            packets.remove(packet);
            return true;
        }
        return false;
    }

    public static void sendPacketNoEvent(Packet<?> packet) {
        packets.add(packet);
        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);
    }

    @FunctionalInterface
    public interface PredictiveAction {
        Packet<?> predict(int sequence);
    }

    public static void sendSequencedPacket(PredictiveAction packetCreator) {
        if (mc.getNetworkHandler() != null && mc.world != null) {
            PendingUpdateManager predictionHandler =
                    ((ClientWorldAccessor) mc.world).getBlockStatePredictionHandler().incrementSequence();

            try {
                int sequence = predictionHandler.getSequence();
                Packet<?> packet = packetCreator.predict(sequence);
                if (packet != null) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
            } catch (Throwable var5) {
                if (predictionHandler != null) {
                    try {
                        predictionHandler.close();
                    } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                    }
                }
                throw var5;
            }

            if (predictionHandler != null) {
                predictionHandler.close();
            }
        }
    }

    public static void setPlayerMovePacketYaw(PlayerMoveC2SPacket packet, float yaw) {
        ((PlayerMoveC2SPacketAccessor) packet).setYaw(yaw);
    }

    public static void setPlayerMovePacketPitch(PlayerMoveC2SPacket packet, float pitch) {
        ((PlayerMoveC2SPacketAccessor) packet).setPitch(pitch);
    }
}