package missu.epsilon.mixin.client.render.item;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.FastItems;
import missu.epsilon.client.utils.render.fastitems.SimpleItemModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Unique private static final SimpleItemModel fastitems$flattenedModel = new SimpleItemModel();
    @Unique private static ModelTransformationMode fastitems$displayContext;

    @Inject(method = "renderItem*", at = @At("HEAD"))
    private static void getRenderType(ModelTransformationMode ModelTransformationMode, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i, int j, int[] is, BakedModel bakedModel, RenderLayer renderType, ItemRenderState.Glint foilType, CallbackInfo ci) {
        fastitems$displayContext = ModelTransformationMode;
    }

    @ModifyArg(method = "renderItem*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;[IIILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"), index = 0)
    private static BakedModel ZuseFlattenItem(BakedModel model) {
        FastItems fastItems = Client.moduleManager.getModule(FastItems.class);
        if(fastItems.isEnabled() && !fastItems.addons.get("Render Side of Items") && !model.hasDepth() && fastitems$displayContext.equals(ModelTransformationMode.GROUND)) {
            fastitems$flattenedModel.setItem(model);
            return fastitems$flattenedModel;
        } else {
            return model;
        }
    }

}
