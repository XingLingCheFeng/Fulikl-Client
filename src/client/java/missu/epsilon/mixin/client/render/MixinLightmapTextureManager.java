package missu.epsilon.mixin.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.AntiBlind;
import missu.epsilon.client.features.modules.render.FullBright;
import missu.epsilon.client.features.modules.render.WorldColor;
import missu.epsilon.client.sxmurxy.providers.ResourceProvider;
import missu.epsilon.client.utils.render.LightmapTextureManagerAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin({LightmapTextureManager.class})
public class MixinLightmapTextureManager implements LightmapTextureManagerAccess {

    @Unique private static final ShaderProgramKey LIGHTMAP_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("lightmap"), VertexFormats.BLIT_SCREEN, Defines.EMPTY);

    @Unique private SimpleFramebuffer tintedFramebuffer;
    @Unique private boolean worldRendering = false;


    @Shadow @Final
    private MinecraftClient client;

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0), index = 1)
    private float max(float b) {
        return Client.moduleManager.getModule(FullBright.class).isEnabled() && FullBright.mode.is("Gamma") ? b - this.client.options.getGamma().getValue().floatValue() + 20 : b;
    }

    @Redirect(method = "getDarknessFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/effect/StatusEffectInstance;"))
    private StatusEffectInstance injectAntiDarkness(ClientPlayerEntity instance, RegistryEntry<StatusEffect> registryEntry) {
        if (!(Client.moduleManager.getModule(AntiBlind.class).isEnabled() && AntiBlind.darkness.get())) {
            return null;
        }
        return instance.getStatusEffect(registryEntry);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(GameRenderer renderer, MinecraftClient client, CallbackInfo ci) {
        tintedFramebuffer = new SimpleFramebuffer(16, 16, false);
        tintedFramebuffer.setTexFilter(9729);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void bakeTinted(float delta, CallbackInfo ci) {
        ShaderProgram shader = RenderSystem.getShader();
        if (shader == null) return;

        boolean enabled = Client.moduleManager.getModule(WorldColor.class).isEnabled();

        if (enabled) {
            int color = WorldColor.color.get();
            shader.getUniformOrDefault("CustomTint").set(new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F
            ));
        } else {
            shader.getUniformOrDefault("CustomTint").set(new Vector3f(1.0F, 1.0F, 1.0F));
        }

        tintedFramebuffer.beginWrite(true);
        BufferBuilder buf = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.BLIT_SCREEN);
        buf.vertex(0,0,0); buf.vertex(1,0,0); buf.vertex(1,1,0); buf.vertex(0,1,0);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        tintedFramebuffer.endWrite();

        shader.getUniformOrDefault("CustomTint").set(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    @Inject(method = "enable", at = @At("HEAD"), cancellable = true)
    private void enableTinted(CallbackInfo ci) {
        if (worldRendering) {
            RenderSystem.setShaderTexture(2, tintedFramebuffer.getColorAttachment());
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closeTinted(CallbackInfo ci) {
        tintedFramebuffer.delete();
    }

    @ModifyArg(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Lnet/minecraft/client/gl/ShaderProgramKey;)Lnet/minecraft/client/gl/ShaderProgram;"
            ),
            index = 0
    )
    private ShaderProgramKey replaceShaderKey(ShaderProgramKey original) {
        return LIGHTMAP_SHADER_KEY;
    }

    @Override
    public void setWorldRendering(boolean value) {
        this.worldRendering = value;
    }

}
