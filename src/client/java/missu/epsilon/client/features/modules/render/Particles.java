package missu.epsilon.client.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.miscs.RandomUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Particles", category = ModuleCategory.RENDER)
public class Particles extends Module {
    private final BoolValue FireFlies = new BoolValue("FireFlies", true);
    private final NumberValue ffcount = (NumberValue) new NumberValue("FFCount", 30, 20, 200, 10).displayable(FireFlies::get);
    private final NumberValue ffsize = (NumberValue) new NumberValue("FFSize", 1d, 0.1d, 2d, 0.1d).displayable(FireFlies::get);

    private final ListValue mode = new ListValue("Mode", new String[]{"Off", "SnowFlake", "Stars", "Hearts", "Dollars", "Bloom"}, "Off");
    private final NumberValue count = (NumberValue) new NumberValue("Count", 100, 20, 800, 10).displayable(() -> !mode.is("Off"));
    private final NumberValue size = (NumberValue) new NumberValue("Size", 1d, 0.1d, 6d, 0.1d).displayable(() -> !mode.is("Off"));
    private final ListValue physics = (ListValue) new ListValue("Physics", new String[]{"Fly", "Drop"}, "Fly").displayable(() -> !mode.is("Off"));

    private final NumberValue rotationSpeed = (NumberValue) new NumberValue("RotationSpeed", 00.5, 0.1, 10).displayable(() -> !mode.is("Off"));
    private final BoolValue randomRotation = (BoolValue) new BoolValue("RandomRotation", true).displayable(() -> !mode.is("Off"));

    private final ListValue colorMode = (ListValue) new ListValue("ColorMode", new String[]{"Sync", "Custom"}, "Sync").displayable(() -> !mode.is("Off"));

    private final NumberValue colorR = (NumberValue) new NumberValue("Color-R", 255, 0, 255, 1).displayable(() -> !mode.is("Off") && !colorMode.is("Sync"));
    private final NumberValue colorG = (NumberValue) new NumberValue("Color-G", 255, 0, 255, 1).displayable(() -> !mode.is("Off") && !colorMode.is("Sync"));
    private final NumberValue colorB = (NumberValue) new NumberValue("Color-B", 255, 0, 255, 1).displayable(() -> !mode.is("Off") && !colorMode.is("Sync"));


    private final ArrayList<ParticleBase> fireFlies = new ArrayList<>();
    private final ArrayList<ParticleBase> particles = new ArrayList<>();
    public static final Identifier firefly = Identifier.of("epsilon", "particles/firefly.png");
    public static final Identifier snowflake = Identifier.of("epsilon", "particles/snowflake.png");
    public static final Identifier star = Identifier.of("epsilon", "particles/star.png");
    public static final Identifier heart = Identifier.of("epsilon", "particles/heart.png");
    public static final Identifier dollar = Identifier.of("epsilon", "particles/dollar.png");

    private Color getColor() {
        if (!colorMode.is("Sync")) {
            return new Color(
                    colorR.getValue().intValue(),
                    colorG.getValue().intValue(),
                    colorB.getValue().intValue());
        } else {
            return ClientSettings.color();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        fireFlies.removeIf(ParticleBase::tick);
        particles.removeIf(ParticleBase::tick);

        for (int i = fireFlies.size(); i < ffcount.getValue(); i++) {
            if (FireFlies.get())
                fireFlies.add(new FireFly(
                        (float) (mc.player.getX() + RandomUtils.nextFloat(-25f, 25f)),
                        (float) (mc.player.getY() + RandomUtils.nextFloat(2f, 15f)),
                        (float) (mc.player.getZ() + RandomUtils.nextFloat(-25f, 25f)),
                        RandomUtils.nextFloat(-0.2f, 0.2f),
                        RandomUtils.nextFloat(-0.1f, 0.1f),
                        RandomUtils.nextFloat(-0.2f, 0.2f)));
        }

        for (int j = particles.size(); j < count.getValue(); j++) {
            boolean drop = physics.is("Drop");
            if (!mode.is("Off")) {
                float rotationAngle = randomRotation.get() ?
                        RandomUtils.nextFloat(0f, 360f) : 0f;

                particles.add(new ParticleBase(
                        (float) (mc.player.getX() + RandomUtils.nextFloat(-48f, 48f)),
                        (float) (mc.player.getY() + RandomUtils.nextFloat(2, 48f)),
                        (float) (mc.player.getZ() + RandomUtils.nextFloat(-48f, 48f)),
                        drop ? 0 : RandomUtils.nextFloat(-0.4f, 0.4f),
                        drop ? RandomUtils.nextFloat(-0.2f, -0.05f) : RandomUtils.nextFloat(-0.1f, 0.1f),
                        drop ? 0 : RandomUtils.nextFloat(-0.4f, 0.4f),
                        rotationAngle));
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        MatrixStack stack = event.getMatrixStack();
        if (FireFlies.get()) {
            stack.push();
            RenderSystem.setShaderTexture(0, firefly);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            fireFlies.forEach(p -> p.render(bufferBuilder));
            RenderUtils.endBuilding(bufferBuilder);
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            stack.pop();
        }

        if (!mode.is("Off")) {
            stack.push();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            particles.forEach(p -> p.render(bufferBuilder));
            RenderUtils.endBuilding(bufferBuilder);
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            stack.pop();
        }
    }

    public class FireFly extends ParticleBase {
        private final List<Trail> trails = new ArrayList<>();

        public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            super(posX, posY, posZ, motionX, motionY, motionZ);
        }

        @Override
        public boolean tick() {

            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age -= 4;
            else if (!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age -= 8;
            else age--;

            if (age < 0)
                return true;

            trails.removeIf(Trail::update);

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            trails.add(new Trail(new Vec3d(prevposX, prevposY, prevposZ), new Vec3d(posX, posY, posZ), getColor()));

            motionX *= 0.99f;
            motionY *= 0.99f;
            motionZ *= 0.99f;

            return false;
        }

        @Override
        public void render(BufferBuilder bufferBuilder) {
            RenderSystem.setShaderTexture(0, firefly);
            if (!trails.isEmpty()) {
                net.minecraft.client.render.Camera camera = mc.gameRenderer.getCamera();
                for (Trail ctx : trails) {
                    Vec3d pos = ctx.interpolate(1f);
                    MatrixStack matrices = new MatrixStack();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(pos.x, pos.y, pos.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    Matrix4f matrix = matrices.peek().getPositionMatrix();

                    bufferBuilder.vertex(matrix, 0, -ffsize.getValue().floatValue(), 0).texture(0f, 1f).color(RenderUtils.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(RenderUtils.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, -ffsize.getValue().floatValue(), -ffsize.getValue().floatValue(), 0).texture(1f, 1f).color(RenderUtils.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(RenderUtils.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, -ffsize.getValue().floatValue(), 0, 0).texture(1f, 0).color(RenderUtils.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(RenderUtils.getTickDelta()))).getRGB());
                    bufferBuilder.vertex(matrix, 0, 0, 0).texture(0, 0).color(RenderUtils.injectAlpha(ctx.color(), (int) (255 * ((float) age / (float) maxAge) * ctx.animation(RenderUtils.getTickDelta()))).getRGB());
                }
            }
        }
    }

    public static class Trail {
        private final Vec3d from;
        private final Vec3d to;
        private final Color color;
        private int ticks, prevTicks;

        public Trail(Vec3d from, Vec3d to, Color color) {
            this.from = from;
            this.to = to;
            this.ticks = 10;
            this.color = color;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / 10.;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public Color color() {
            return color;
        }
    }

    public class ParticleBase {

        protected float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        protected int age, maxAge;
        protected float rotationAngle;
        protected float prevRotationAngle;
        protected float rotationSpeed;

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            this(posX, posY, posZ, motionX, motionY, motionZ, 0f);
        }

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ, float initialRotation) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = RandomUtils.nextInt(100, 300);
            maxAge = age;

            this.rotationAngle = initialRotation;
            this.prevRotationAngle = initialRotation;
            this.rotationSpeed = RandomUtils.nextFloat(
                    -Particles.this.rotationSpeed.getValue().floatValue() / 5F,
                    Particles.this.rotationSpeed.getValue().floatValue() / 5F
            );
        }

        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 4096) age -= 8;
            else age--;

            if (age < 0)
                return true;

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            prevRotationAngle = rotationAngle;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;


            rotationAngle += rotationSpeed;

            motionX *= 0.9f;
            if (physics.is("Fly"))
                motionY *= 0.9f;
            motionZ *= 0.9f;

            motionY -= 0.001f;

            return false;
        }

        public void render(BufferBuilder bufferBuilder) {
            switch (mode.get()) {
                case "Bloom" -> RenderSystem.setShaderTexture(0, firefly);
                case "SnowFlake" -> RenderSystem.setShaderTexture(0, snowflake);
                case "Dollars" -> RenderSystem.setShaderTexture(0, dollar);
                case "Hearts" -> RenderSystem.setShaderTexture(0, heart);
                case "Stars" -> RenderSystem.setShaderTexture(0, star);
            }

            Camera camera = mc.gameRenderer.getCamera();
            Color color1 = getColor();
            Vec3d pos = RenderUtils.interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            float currentRotation = prevRotationAngle + (rotationAngle - prevRotationAngle) * RenderUtils.getTickDelta();

            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            //rot
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentRotation));

            Matrix4f matrix1 = matrices.peek().getPositionMatrix();

            float lifeProgress = 1.0f - ((float) age / (float) maxAge);
            float fadeInDuration = 0.2f;
            float fadeOutDuration = 0.3f;

            float alpha;
            if (lifeProgress < fadeInDuration) {
                alpha = lifeProgress / fadeInDuration;
            } else if (lifeProgress > 1.0f - fadeOutDuration) {
                alpha = (1.0f - lifeProgress) / fadeOutDuration;
            } else {
                alpha = 1.0f;
            }

            //apply alpha
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            int alphaValue = (int) (255 * alpha);

            bufferBuilder.vertex(matrix1, 0, -size.getValue().floatValue(), 0).texture(0f, 1f).color(RenderUtils.injectAlpha(color1, alphaValue).getRGB());
            bufferBuilder.vertex(matrix1, -size.getValue().floatValue(), -size.getValue().floatValue(), 0).texture(1f, 1f).color(RenderUtils.injectAlpha(color1, alphaValue).getRGB());
            bufferBuilder.vertex(matrix1, -size.getValue().floatValue(), 0, 0).texture(1f, 0).color(RenderUtils.injectAlpha(color1, alphaValue).getRGB());
            bufferBuilder.vertex(matrix1, 0, 0, 0).texture(0, 0).color(RenderUtils.injectAlpha(color1, alphaValue).getRGB());
        }
    }
}