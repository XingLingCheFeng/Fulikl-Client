package missu.epsilon.mixin.network.packet.c2s.play;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {

    @Renamer(obfuscated = false)
    @Accessor("yaw")
    void setYaw(float yaw);

    @Renamer(obfuscated = false)
    @Accessor("pitch")
    void setPitch(float pitch);


    @Renamer(obfuscated = false)
    @Accessor("changeLook")
    void setRotating(boolean rotating);

    @Renamer(obfuscated = false)
    @Accessor("changeLook")
    boolean getRotating();




}
