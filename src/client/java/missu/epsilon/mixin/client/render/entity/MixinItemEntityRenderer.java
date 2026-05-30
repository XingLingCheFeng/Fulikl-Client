package missu.epsilon.mixin.client.render.entity;


import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.FastItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import static net.minecraft.client.render.entity.ItemEntityRenderer.renderStack;

@Renamer(obfuscated = false)
@Mixin(ItemEntityRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class MixinItemEntityRenderer extends EntityRenderer<ItemEntity, ItemEntityRenderState> {

    @Final @Shadow private Random random;

    protected MixinItemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void render(ItemEntityRenderState itemEntityRenderState, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i, CallbackInfo ci) {
        FastItems fastItems = Client.moduleManager.getModule(FastItems.class);
        //CONFIG: early exit if mod is disabled
        if (!fastItems.isEnabled()) {
            return;
        }
        if (itemEntityRenderState.itemRenderState.isEmpty()) {
            return;
        }
        boolean gui3d = itemEntityRenderState.itemRenderState.hasDepth();
        //CONFIG: exit if model is 3D and not affecting 3D models enabled
        if (gui3d && !fastItems.addons.get("Affect 3D Models")) {
            return;
        }
        poseStack.push();
        //CONFIG: castShadows
        this.shadowRadius = fastItems.addons.get("Cast Shadows")? 0.15F : 0.0F;
        // up and down
        float g = MathHelper.sin(itemEntityRenderState.age / 10.0F + itemEntityRenderState.uniqueOffset) * 0.1F + 0.1F;
        float h = itemEntityRenderState.itemRenderState.getTransformation().scale.y();
        poseStack.translate(0.0F, g + 0.35f * h, 0.0F);
        // face to camera
        poseStack.multiply(dispatcher.getRotation());
        // count visual
        renderStack(poseStack, multiBufferSource, i, itemEntityRenderState, this.random);
        poseStack.pop();
        super.render(itemEntityRenderState, poseStack, multiBufferSource, i);
        ci.cancel();
    }

}