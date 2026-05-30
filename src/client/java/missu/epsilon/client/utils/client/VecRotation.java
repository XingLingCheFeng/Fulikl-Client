package missu.epsilon.client.utils.client;

import missu.epsilon.client.utils.entity.Rotation;
import net.minecraft.util.math.Vec3d;

public class VecRotation {
    public Vec3d vec;
    public Rotation rotation;

    public VecRotation(Vec3d vec, Rotation rotation) {
        this.vec = vec;
        this.rotation = rotation;
    }
}
