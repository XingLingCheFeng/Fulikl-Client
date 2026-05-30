package missu.epsilon.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.Render2DEvent;
import missu.epsilon.client.event.events.render.RenderLastScreenEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.features.ModuleManager;
import missu.epsilon.client.features.modules.render.AntiBlind;
import missu.epsilon.client.features.modules.render.AspectRatio;
import missu.epsilon.client.features.modules.render.Camera;
import missu.epsilon.client.features.modules.render.WorldColor;
import missu.epsilon.client.features.modules.visual.Notification;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.nanovg.gl.States;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.instance.SimpleFrameBufferInstance;
import missu.epsilon.client.sxmurxy.instance.TextureTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.basic.screen.ScreenPositionUtils;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.render.LightmapTextureManagerAccess;
import missu.epsilon.client.utils.render.RenderHelper;
import missu.epsilon.client.utils.render.ViewBobbingStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow private float zoom;
    @Shadow private float zoomX;
    @Shadow private float zoomY;
    @Shadow private float viewDistance;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void onRenderScreenEvent(RenderTickCounter tickCounter, boolean tick, CallbackInfo callbackInfo) {

        if (ClientUtils.isNull()) {
            return;
        }
        if (mc.currentScreen instanceof MessageScreen) {
            return;
        }
        if (mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        if (mc.options.hudHidden) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        States.push();
        RenderHelper.beginRender();
        SimpleFrameBufferInstance.updateSimpleFrameBufferInstance();
        SimpleFrameBufferInstance.renderSimpleFrameBufferInstance();
        SimpleFrameBufferInstance.setShaderTexturePre();
        BlurTaskInstance.runTask();
        SimpleFrameBufferInstance.setShaderTexturePost();
        BlurTaskInstance.clearTask();
        /// Event
        DrawContext drawContext = new DrawContext(mc, mc.gameRenderer.buffers.getEntityVertexConsumers());
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Client.getInstance().getEventManager().call(new RenderNvgEvent(drawContext, matrix4f, tickCounter));

        if (Client.moduleManager != null && Client.moduleManager.getModule(Notification.class).isEnabled())
            NotificationManager.publish(mc.getWindow(), matrix4f);

        TextureTaskInstance.runTask();
        TextureTaskInstance.clearTask();
        SimpleFrameBufferInstance.updateSimpleFrameBufferInstance();
        SimpleFrameBufferInstance.renderSimpleFrameBufferInstance();
        SimpleFrameBufferInstance.setShaderTexturePre();
        BuiltBlur blur = Builder.blur().size(new SizeState(1, 1)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(5f).color(QuadColorState.TRANSPARENT).position(new PositionState(1, 1)).matrix4f(new DrawContext(mc, mc.gameRenderer.buffers.getEntityVertexConsumers()).getMatrices().peek().getPositionMatrix()).build();
        blur.render(blur.matrix4f(), blur.positionState().x(), blur.positionState().y());
        SimpleFrameBufferInstance.setShaderTexturePost();
        RenderHelper.endRender();
        States.pop();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
    }


    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onWorldStart(RenderTickCounter tickCounter, CallbackInfo ci) {
        ((LightmapTextureManagerAccess) client.gameRenderer.getLightmapTextureManager())
                .setWorldRendering(true);
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onWorldEnd(RenderTickCounter tickCounter, CallbackInfo ci) {
        ((LightmapTextureManagerAccess) client.gameRenderer.getLightmapTextureManager())
                .setWorldRendering(false);
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (Client.moduleManager.getModule(AspectRatio.class).isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective(fovDegrees * ((float)Math.PI / 180F), AspectRatio.ratio.get().floatValue(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Redirect(
            method = "renderHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getBasicProjectionMatrix(F)Lorg/joml/Matrix4f;")
    )
    public Matrix4f renderHandProjectionMatrix(GameRenderer instance, float fovDegrees) {
        Matrix4f matrix4f = new Matrix4f();
        if (zoom != 1.0F) {
            matrix4f.translate(zoomX, -zoomY, 0.0F);
            matrix4f.scale(zoom, zoom, 1.0F);
        }
        float aspect = (float) client.getWindow().getFramebufferWidth()
                / (float) client.getWindow().getFramebufferHeight();
        return matrix4f.perspective(fovDegrees * ((float) Math.PI / 180F), aspect, 0.05F, viewDistance * 4.0F);
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float hookNausea(float original) {
        if (!(AntiBlind.nausea.get() && Client.moduleManager.getModule(AntiBlind.class).isEnabled())) {
            return 0f;
        }
        return original;
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = At.Shift.BEFORE))
    public void onRender2DEvent(RenderTickCounter tickCounter, boolean tick, CallbackInfo callbackInfo) {
        if (ClientUtils.isNull()) {
            return;
        }
        if (mc.currentScreen instanceof MessageScreen) {
            return;
        }
        if (mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        RenderSystem.enableCull();
        /// Event
        DrawContext drawContext = new DrawContext(mc, mc.gameRenderer.buffers.getEntityVertexConsumers());
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Client.getInstance().getEventManager().call(new Render2DEvent(drawContext, matrix4f));
        RenderSystem.disableCull();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", ordinal = 1, shift = At.Shift.AFTER))
    public void onRenderClickGUI2DEvent(RenderTickCounter tickCounter, boolean tick, CallbackInfo callbackInfo) {
        if (ClientUtils.isNull()) {
            return;
        }
        if (mc.currentScreen instanceof MessageScreen) {
            return;
        }
        if (mc.currentScreen instanceof DownloadingTerrainScreen) {
            return;
        }
        RenderSystem.enableCull();
        States.push();
        RenderHelper.beginRender();
        /// Event
        DrawContext drawContext = new DrawContext(mc, mc.gameRenderer.buffers.getEntityVertexConsumers());
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Client.getInstance().getEventManager().call(new RenderLastScreenEvent(drawContext, matrix4f));
        RenderHelper.endRender();
        States.pop();
        RenderSystem.disableCull();
    }


    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void render3dHook(RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
        MatrixStack matrixStack = new MatrixStack();
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(mc.gameRenderer.getCamera().getYaw() + 180f));
        ScreenPositionUtils.positionMatrix.set(matrixStack.peek().getPositionMatrix());
        ScreenPositionUtils.projectMatrix.set(RenderSystem.getProjectionMatrix());
        ScreenPositionUtils.modelMatrix.set(RenderSystem.getModelViewMatrix());
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void noHurtCamHook(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            if (Objects.requireNonNull(Client.moduleManager.getModule(Camera.class)).isEnabled() && Camera.cameraOptions.get("No Hurt Tilt")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
        var material = new MatrixStack();
        material.multiplyPositionMatrix(matrix4f);
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Client.getInstance().getEventManager().call(new Render3DEvent(material, tickCounter, projectionMatrix));
    }

}
