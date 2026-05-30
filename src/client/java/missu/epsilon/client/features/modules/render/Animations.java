package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.render.LegacyBlockingUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

@ModuleInfo(name = "Animations",description = "Block animations similar to old version(1.8).",category = ModuleCategory.RENDER)
public class Animations extends Module {
    public static final ListValue blockMode = new ListValue("Block Mode",
            new String[]{"1.8","Swing","Swank","E","Swack","Swung","Swong","Swang"}, "1.8");

    public static final NumberValue swingSpeed = new NumberValue("Swing time (Default:6)",6,1,10,1);
    public static final BoolValue shortSwing = new BoolValue("Smooth swing",false);

    public static final NumberValue xOffset = new NumberValue("X offset",0,-1,1,0.05);
    public static final NumberValue yOffset = new NumberValue("Y offset",0,-1,1,0.05);
    public static final NumberValue zOffset = new NumberValue("Z offset",0,-1,1,0.05);
    public static final NumberValue scale = new NumberValue("Scale",1,0.1,2,0.1);

    public static void applyTransforms(MatrixStack matrices, AbstractClientPlayerEntity player, Hand hand, float equippedProgress, float swingProgress) {
        float var15 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);

        if (!Client.moduleManager.getModule(Animations.class).isEnabled()){
            transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
            return;
        }

        switch (blockMode.get()) {
            case "Swing" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, swingProgress);
            }
            case "1.8" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
            }
            case "E" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2.5F, 0);
                matrices.translate(-0.1, 0.075, -0.1);
                matrices.translate(0, -var15 * 0.1, 0);
                Vector3f axisE = new Vector3f(-0.5F, 0.0F, 0.0F).normalize();
                matrices.multiply(RotationAxis.of(axisE).rotationDegrees(var15 * 30));
            }
            case "Swack" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
                matrices.translate(0, var15 * 0.4, -var15 * 0.1);
                Vector3f axis = new Vector3f(-0.5F, 0.0F, 1.0F).normalize();
                matrices.multiply(RotationAxis.of(axis).rotationDegrees(var15 * 25));
            }
            case "Swung" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(var15 * 15));
            }
            case "Swong" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
                Vector3f axis1 = new Vector3f(var15 / 2, 0.0F, 9.0F).normalize();
                matrices.multiply(RotationAxis.of(axis1).rotationDegrees(-var15 * 60 / 2.0F));
                Vector3f axis2 = new Vector3f(1.0F, var15 / 2, 0.0F).normalize();
                matrices.multiply(RotationAxis.of(axis2).rotationDegrees(-var15 * 50));
            }
            case "Swonk" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, 0);
                Vector3f axis = new Vector3f(-var15, 0.0F, 9.0F).normalize();
                matrices.multiply(RotationAxis.of(axis).rotationDegrees(var15 * 30 / 1.75F));
            }
            case "Swang" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, swingProgress);
                Vector3f axis1 = new Vector3f(-var15, 0.0F, 9.0F).normalize();
                matrices.multiply(RotationAxis.of(axis1).rotationDegrees(var15 * 30 / 2.0F));
                Vector3f axis2 = new Vector3f(1.0F, -var15 / 2, 0.0F).normalize();
                matrices.multiply(RotationAxis.of(axis2).rotationDegrees(var15 * 40));
            }
            case "Swank" -> {
                transformFirstPersonItem(matrices, player, hand, equippedProgress / 2, swingProgress);
                Vector3f axis1 = new Vector3f(-var15, 0.0F, 9.0F).normalize();
                matrices.multiply(RotationAxis.of(axis1).rotationDegrees(var15 * 30));
                Vector3f axis2 = new Vector3f(1.0F, -var15, 0.0F).normalize();
                matrices.multiply(RotationAxis.of(axis2).rotationDegrees(var15 * 40));
            }
        }
    }



    public static void transformFirstPersonItem(MatrixStack matrices, AbstractClientPlayerEntity player, Hand hand, float equipProgress, float swingProgress){
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);

        int direction = LegacyBlockingUtil.getHandMultiplier(player, hand);

        matrices.translate(direction * 0.56F, -0.52F, -0.72F);
        matrices.translate(0,equipProgress * -0.6F,0);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 45.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * f1 * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(direction * f1 * -80.0F));
        matrices.scale(0.4F, 0.4F, 0.4F);

    }
}
