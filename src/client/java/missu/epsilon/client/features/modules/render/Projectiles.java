package missu.epsilon.client.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.projectiles.ProjectileData;
import missu.epsilon.client.utils.render.projectiles.ProjectileDataManager;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Projectiles", category = ModuleCategory.RENDER)
public class Projectiles extends Module {
    private final BoolValue renderTNT = new BoolValue("Render TNT", true);
    private final BoolValue offhandSupport = new BoolValue("Offhand Support", true);

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ProjectileEntity) {
                    var projectileData = getProjectileDataByEntity(entity);
                    var matrixStack = event.getMatrixStack();

                    if (projectileData != null) {
                        matrixStack.push();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glDisable(2929);
                        GL11.glDepthMask(false);
                        GL11.glEnable(2848);
                        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                        this.render(matrixStack, entity, projectileData, projectileData.color(), mc.player, mc.world);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        GL11.glDisable(3042);
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                        GL11.glDisable(2848);
                        matrixStack.pop();
                    }
                }
            }
        }
    }

    @EventTarget
    private void onRender(Render3DEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        var pathResult = this.getPath(RenderUtils.getTickDelta(), mc.player, mc.world);

        if (pathResult == null && offhandSupport.get()) {
            var offhandPathResult = this.getPathFromOffhand(RenderUtils.getTickDelta(), mc.player, mc.world);
            if (offhandPathResult != null) {
                pathResult = offhandPathResult;
            }
        }

        if (pathResult != null) {
            var path = pathResult.getPath();

            if (path.size() >= 2) {
                var matrixStack = event.getMatrixStack();

                var projectileData = ProjectileDataManager.byItem(mc.player.getMainHandStack());
                if (projectileData.isEmpty() && offhandSupport.get()) {
                    projectileData = ProjectileDataManager.byItem(mc.player.getOffHandStack());
                }

                matrixStack.push();
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glEnable(2848);
                RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

                this.drawLine(matrixStack, path, path.getFirst(), projectileData.map(ProjectileData::color).orElse(0xFFFFFFFF));

                if (!path.isEmpty()) {
                    this.drawEndOfLine(matrixStack, path.getLast(), path.getFirst(), pathResult.result, RenderUtils.getTickDelta());
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(2848);
                matrixStack.pop();
            }
        }
    }

    private Path getPathFromOffhand(float partialTicks, ClientPlayerEntity player, ClientWorld world) {
        ArrayList<Vec3d> path = new ArrayList<>();
        ItemStack stack = player.getOffHandStack();
        Item item = stack.getItem();

        if (!stack.isEmpty() && this.isThrowable(item)) {
            double arrowPosX = player.lastRenderX + (player.getX() - player.lastRenderX) * (double) partialTicks;
            double arrowPosY = player.lastRenderY + (player.getY() - player.lastRenderY) * (double) partialTicks + (double) player.getStandingEyeHeight() - 0.1;
            double arrowPosZ = player.lastRenderZ + (player.getZ() - player.lastRenderZ) * (double) partialTicks;
            double arrowMotionFactor = item instanceof RangedWeaponItem ? 1.0 : 0.4;
            double yaw;
            double pitch;

            Rotation rotation = RotationManager.serverRotation;
            Rotation lastRotation = RotationManager.targetRotation;

            if (rotation != null && lastRotation != null) {
                yaw = Math.toRadians(MathHelper.lerp(partialTicks, lastRotation.getYaw(), rotation.getYaw()));
                pitch = Math.toRadians(MathHelper.lerp(partialTicks, lastRotation.getPitch(), rotation.getPitch()));
            } else {
                yaw = Math.toRadians(MathHelper.lerp(partialTicks, player.prevYaw, player.getYaw()));
                pitch = Math.toRadians(MathHelper.lerp(partialTicks, player.prevPitch, player.getPitch()));
            }

            double arrowMotionX = -Math.sin(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotionY = -Math.sin(pitch) * arrowMotionFactor;
            double arrowMotionZ = Math.cos(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotion = Math.sqrt(arrowMotionX * arrowMotionX + arrowMotionY * arrowMotionY + arrowMotionZ * arrowMotionZ);
            arrowMotionX /= arrowMotion;
            arrowMotionY /= arrowMotion;
            arrowMotionZ /= arrowMotion;

            if (item instanceof RangedWeaponItem) {
                float bowPower = (float) (72000 - player.getItemUseTimeLeft()) / 20.0F;
                bowPower = (bowPower * bowPower + bowPower * 2.0F) / 3.0F;
                if (bowPower > 1.0F || bowPower <= 0.1F) {
                    bowPower = 1.0F;
                }

                bowPower *= 3.0F;
                arrowMotionX *= bowPower;
                arrowMotionY *= bowPower;
                arrowMotionZ *= bowPower;
            } else {
                arrowMotionX *= 1.5;
                arrowMotionY *= 1.5;
                arrowMotionZ *= 1.5;
            }

            double gravity = this.getProjectileGravity(item);

            for (int i = 0; i < 1000; i++) {
                Vec3d arrowPos = new Vec3d(arrowPosX, arrowPosY, arrowPosZ);
                Vec3d postArrowPos = new Vec3d(arrowPosX + arrowMotionX, arrowPosY + arrowMotionY, arrowPosZ + arrowMotionZ);
                path.add(arrowPos);
                RaycastContext context = new RaycastContext(arrowPos, postArrowPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player);
                BlockHitResult clip = world.raycast(context);

                if (clip.getType() != HitResult.Type.MISS) {
                    return new Path(path, clip);
                }

                ArrowEntity fakeArrow = new ArrowEntity(world, arrowPosX, arrowPosY, arrowPosZ, ItemStack.EMPTY, null);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(
                        world,
                        fakeArrow,
                        arrowPos,
                        postArrowPos,
                        fakeArrow.getBoundingBox().stretch(new Vec3d(arrowMotionX, arrowMotionY, arrowMotionZ)).expand(1.0),
                        entity -> entity != player && entity instanceof LivingEntity
                );

                if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                    return new Path(path, entityHitResult);
                }

                arrowPosX += arrowMotionX;
                arrowPosY += arrowMotionY;
                arrowPosZ += arrowMotionZ;
                arrowMotionX *= 0.99;
                arrowMotionY *= 0.99;
                arrowMotionZ *= 0.99;
                arrowMotionY -= gravity;
            }

            return new Path(path, null);
        } else {
            return null;
        }
    }

    private void drawLine(MatrixStack matrixStack, List<Vec3d> path, Vec3d camPos, int color) {
        var matrix = matrixStack.peek().getPositionMatrix();
        var buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for (Vec3d point : path) {
            buffer.vertex(matrix, (float) (point.x - camPos.x), (float) (point.y - camPos.y), (float) (point.z - camPos.z)).color(color);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawEndOfLine(MatrixStack matrixStack, Vec3d end, Vec3d camPos, HitResult result, float partialTicks) {
        Box bb = new Box(0.15, 0.15, 0.15, 0.35, 0.35, 0.35);
        Color color = new Color(255, 255, 255);

        if (result != null) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) result;
                Direction direction = blockHitResult.getSide();

                if (direction == Direction.SOUTH) {
                    bb = new Box(0.0, 0.0, 0.0, 0.5, 0.5, 0.1);
                } else if (direction == Direction.NORTH) {
                    bb = new Box(0.0, 0.0, 0.4, 0.5, 0.5, 0.5);
                } else if (direction == Direction.EAST) {
                    bb = new Box(0.0, 0.0, 0.0, 0.1, 0.5, 0.5);
                } else if (direction == Direction.WEST) {
                    bb = new Box(0.4, 0.0, 0.0, 0.5, 0.5, 0.5);
                } else if (direction == Direction.UP) {
                    bb = new Box(0.0, 0.0, 0.0, 0.5, 0.1, 0.5);
                    color = new Color(0, 255, 0);
                } else if (direction == Direction.DOWN) {
                    bb = new Box(0.0, 0.4, 0.0, 0.5, 0.5, 0.5);
                }
            } else if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) result;
                color = new Color(255, 0, 0);
                Entity entity = entityHitResult.getEntity();
                double motionX = entity.getX() - entity.prevX;
                double motionY = entity.getY() - entity.prevY;
                double motionZ = entity.getZ() - entity.prevZ;
                Vec3d cameraPos = mc.getBlockEntityRenderDispatcher().camera.getPos();
                Box move = entity.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z)
                        .offset(-motionX, -motionY, -motionZ)
                        .offset((double) partialTicks * motionX, (double) partialTicks * motionY, (double) partialTicks * motionZ)
                        .expand(0.1);
                RenderUtils.drawBox(matrixStack, move, ColorUtils.reAlpha(color, 125), true, null, false);
            }
        }

        double renderX = end.x - camPos.x;
        double renderY = end.y - camPos.y;
        double renderZ = end.z - camPos.z;
        matrixStack.push();
        matrixStack.translate(renderX - 0.25, renderY - 0.25, renderZ - 0.25);
        RenderUtils.drawBox(matrixStack, bb, ColorUtils.reAlpha(color, 65), true, ColorUtils.reAlpha(color, 190), false);
        matrixStack.pop();
    }

    private Path getPath(float partialTicks, ClientPlayerEntity player, ClientWorld world) {
        ArrayList<Vec3d> path = new ArrayList<>();
        ItemStack stack = player.getMainHandStack();
        Item item = stack.getItem();

        if (!stack.isEmpty() && this.isThrowable(item)) {
            double arrowPosX = player.lastRenderX + (player.getX() - player.lastRenderX) * (double) partialTicks;
            double arrowPosY = player.lastRenderY + (player.getY() - player.lastRenderY) * (double) partialTicks + (double) player.getStandingEyeHeight() - 0.1;
            double arrowPosZ = player.lastRenderZ + (player.getZ() - player.lastRenderZ) * (double) partialTicks;
            double arrowMotionFactor = item instanceof RangedWeaponItem ? 1.0 : 0.4;
            double yaw;
            double pitch;

            Rotation rotation = RotationManager.serverRotation;
            Rotation lastRotation = RotationManager.targetRotation;

            if (rotation != null && lastRotation != null) {
                yaw = Math.toRadians(MathHelper.lerp(partialTicks, lastRotation.getYaw(), rotation.getYaw()));
                pitch = Math.toRadians(MathHelper.lerp(partialTicks, lastRotation.getPitch(), rotation.getPitch()));
            } else {
                yaw = Math.toRadians(MathHelper.lerp(partialTicks, player.prevYaw, player.getYaw()));
                pitch = Math.toRadians(MathHelper.lerp(partialTicks, player.prevPitch, player.getPitch()));
            }


            double arrowMotionX = -Math.sin(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotionY = -Math.sin(pitch) * arrowMotionFactor;
            double arrowMotionZ = Math.cos(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotion = Math.sqrt(arrowMotionX * arrowMotionX + arrowMotionY * arrowMotionY + arrowMotionZ * arrowMotionZ);
            arrowMotionX /= arrowMotion;
            arrowMotionY /= arrowMotion;
            arrowMotionZ /= arrowMotion;

            if (item instanceof RangedWeaponItem) {
                float bowPower = (float) (72000 - player.getItemUseTimeLeft()) / 20.0F;
                bowPower = (bowPower * bowPower + bowPower * 2.0F) / 3.0F;
                if (bowPower > 1.0F || bowPower <= 0.1F) {
                    bowPower = 1.0F;
                }

                bowPower *= 3.0F;
                arrowMotionX *= bowPower;
                arrowMotionY *= bowPower;
                arrowMotionZ *= bowPower;
            } else {
                arrowMotionX *= 1.5;
                arrowMotionY *= 1.5;
                arrowMotionZ *= 1.5;
            }

            double gravity = this.getProjectileGravity(item);

            for (int i = 0; i < 1000; i++) {
                Vec3d arrowPos = new Vec3d(arrowPosX, arrowPosY, arrowPosZ);
                Vec3d postArrowPos = new Vec3d(arrowPosX + arrowMotionX, arrowPosY + arrowMotionY, arrowPosZ + arrowMotionZ);
                path.add(arrowPos);
                RaycastContext context = new RaycastContext(arrowPos, postArrowPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player);
                BlockHitResult clip = world.raycast(context);

                if (clip.getType() != HitResult.Type.MISS) {
                    return new Path(path, clip);
                }

                ArrowEntity fakeArrow = new ArrowEntity(world, arrowPosX, arrowPosY, arrowPosZ, ItemStack.EMPTY, null);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(
                        world,
                        fakeArrow,
                        arrowPos,
                        postArrowPos,
                        fakeArrow.getBoundingBox().stretch(new Vec3d(arrowMotionX, arrowMotionY, arrowMotionZ)).expand(1.0),
                        entity -> entity != player && entity instanceof LivingEntity
                );

                if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                    return new Path(path, entityHitResult);
                }

                arrowPosX += arrowMotionX;
                arrowPosY += arrowMotionY;
                arrowPosZ += arrowMotionZ;
                arrowMotionX *= 0.99;
                arrowMotionY *= 0.99;
                arrowMotionZ *= 0.99;
                arrowMotionY -= gravity;
            }

            return new Path(path, null);
        } else {
            return null;
        }
    }

    private double getProjectileGravity(Item item) {
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return 0.05;
        } else if (item instanceof PotionItem) {
            return 0.4;
        } else if (item instanceof FishingRodItem) {
            return 0.15;
        } else {
            return item instanceof TridentItem ? 0.015 : 0.03;
        }
    }

    private boolean isThrowable(Item item) {
        return item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof EnderPearlItem
                || item instanceof SplashPotionItem
                || item instanceof LingeringPotionItem
                || item instanceof FishingRodItem
                || item instanceof TridentItem
                || item == Items.TNT && renderTNT.get();
    }

    public static BlockHitResult rayCast(Vec3d start, Vec3d end, boolean includeFluids, boolean ignoreBlockWithoutBoundingBox, Entity entity, World world) {
        return world.raycast(new RaycastContext(start, end, ignoreBlockWithoutBoundingBox ? RaycastContext.ShapeType.COLLIDER : RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
    }

    public static EntityHitResult calculateIntercept(Box instance, Vec3d var1, Vec3d var2) {
        Optional<Vec3d> e = instance.raycast(var1, var2);
        return e.map(vec3 -> new EntityHitResult(null, vec3)).orElse(null);
    }

    private void render(MatrixStack matrix, Entity entity, ProjectileData projectileInfo, int color, ClientPlayerEntity player, ClientWorld world) {
        if (entity != null) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            double posX = entity.getX();
            double posY = entity.getY();
            double posZ = entity.getZ();
            double motionX = entity.getVelocity().x;
            double motionY = entity.getVelocity().y;
            double motionZ = entity.getVelocity().z;
            this.drawVertex(color, builder, matrix, posX, posY, posZ);

            while (true) {
                float data1 = projectileInfo.renderRadius();
                float data2 = projectileInfo.collisionInflation();
                Box aabb = new Box(posX - (double) data1, posY, posZ - (double) data1, posX + (double) data1, posY + (double) data2, posZ + (double) data1);
                Vec3d vec3 = new Vec3d(posX, posY, posZ);
                Vec3d vec3WithMotion = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
                HitResult movingObj = rayCast(vec3, vec3WithMotion, false, entity instanceof ArrowEntity, entity, world);

                if (!movingObj.getType().equals(HitResult.Type.MISS)) {
                    vec3WithMotion = new Vec3d(movingObj.getPos().x, movingObj.getPos().y, movingObj.getPos().z);
                }

                List<Entity> getByAABBEntities = world.getOtherEntities(player, aabb.contract(motionX, motionY, motionZ).stretch(1.0, 1.0, 1.0));
                double lastMinDistance = 0.0;

                for (Entity aabbEntity : getByAABBEntities) {
                    if (aabbEntity instanceof LivingEntity && !(aabbEntity instanceof EndermanEntity) && aabbEntity.isCollidable() && !aabbEntity.equals(player)) {
                        aabb = aabbEntity.getBoundingBox().stretch(0.3, 0.3, 0.3);
                        EntityHitResult aabbMovingObj = calculateIntercept(aabb, vec3, vec3WithMotion);
                        if (aabbMovingObj != null) {
                            double distance = vec3.distanceTo(aabbMovingObj.getPos());
                            if (distance < lastMinDistance || lastMinDistance == 0.0) {
                                lastMinDistance = distance;
                                movingObj = aabbMovingObj;
                            }
                        }
                    }
                }

                posX += motionX;
                posY += motionY;
                posZ += motionZ;
                if (!movingObj.getType().equals(HitResult.Type.MISS)) {
                    posX = movingObj.getPos().x;
                    posY = movingObj.getPos().y;
                    posZ = movingObj.getPos().z;
                    break;
                }

                if (posY < -128.0) {
                    break;
                }

                motionX *= entity.isTouchingWater() ? 0.8 : 0.99;
                double var39 = motionY * (entity.isTouchingWater() ? 0.8 : 0.99);
                motionZ *= entity.isTouchingWater() ? 0.8 : 0.99;
                motionY = var39 - (double) projectileInfo.gravity();
                this.drawVertex(color, builder, matrix, posX + motionX, posY + motionY, posZ + motionZ);
            }

            BufferRenderer.drawWithGlobalProgram(builder.end());
        }
    }

    private void drawVertex(int color, BufferBuilder builder, MatrixStack stack, double x, double y, double z) {
        Entity entity = mc.getCameraEntity();

        if (entity != null) {
            double d0 = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double) RenderUtils.getTickDelta();
            double d1 = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double) RenderUtils.getTickDelta();
            double d2 = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double) RenderUtils.getTickDelta();
            builder.vertex(stack.peek().getPositionMatrix(), (float) (x - d0), (float) (y - d1) - 1.5F, (float) (z - d2)).color(color);
        }
    }

    private ProjectileData getProjectileDataByEntity(Entity entity) {
        if (entity.isOnGround()) {
            return null;
        } else if (entity.getX() == entity.lastRenderX && entity.getZ() == entity.lastRenderZ) {
            return null;
        } else {
            return ProjectileDataManager.byEntity(entity).orElse(null);
        }
    }

    @Getter
    public static class Path {
        private final List<Vec3d> path;
        private final HitResult result;

        public Path(List<Vec3d> path, HitResult result) {
            this.path = path;
            this.result = result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Path other)) {
                return false;
            } else if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$path = this.getPath();
                Object other$path = other.getPath();
                if (this$path == null ? other$path == null : this$path.equals(other$path)) {
                    Object this$result = this.getResult();
                    Object other$result = other.getResult();
                    return this$result == null ? other$result == null : this$result.equals(other$result);
                } else {
                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Path;
        }

        @Override
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $path = this.getPath();
            result = result * 59 + ($path == null ? 43 : $path.hashCode());
            Object $result = this.getResult();
            return result * 59 + ($result == null ? 43 : $result.hashCode());
        }

        @Override
        public String toString() {
            return "Projectile.Path(path=" + this.getPath() + ", result=" + this.getResult() + ")";
        }
    }
}