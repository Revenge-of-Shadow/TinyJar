package tinyjarmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import tinyjarmod.TinyJarBlockEntity;

import static tinyjarmod.TinyJar.FACE;
import static tinyjarmod.TinyJar.FACING;

public class TinyJarRenderer implements BlockEntityRenderer<TinyJarBlockEntity> {

    public TinyJarRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TinyJarBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        AttachFace face = state.getValue(FACE);
        Direction facing = state.getValue(FACING);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(switch (face){
                    case FLOOR -> Axis.XP.rotationDegrees(0);
                    case CEILING -> Axis.XP.rotationDegrees(180);
                    case WALL -> switch (facing){
                        case NORTH -> Axis.XP.rotationDegrees(90);
                        case SOUTH -> Axis.XP.rotationDegrees(-90);
                        case EAST -> Axis.ZP.rotationDegrees(90);
                        case WEST -> Axis.ZP.rotationDegrees(-90);
                        default -> Axis.XP.rotationDegrees(0);
                    };
                }
        );
        poseStack.translate(-0.5, -0.5, -0.5);

        FluidStack fluidStack = be.getTank().getFluid();
        if (fluidStack.isEmpty()) return;

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(props.getStillTexture(fluidStack));

        int color = props.getTintColor(fluidStack);
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a <= 0f) a = 1f; // most fluids report 0xFF alpha; guard against fluids that report 0
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float fillRatio = (float) fluidStack.getAmount() / be.getTank().getCapacity();

        // 1px margin on every side of the 5,0,5 -> 11,8,11 jar box
        final float x0 = 6f / 16f, x1 = 10f / 16f;
        final float z0 = 6f / 16f, z1 = 10f / 16f;
        final float yFloor = 1f / 16f;
        final float yCeil = 7f / 16f;
        final float y1 = yFloor + fillRatio * (yCeil - yFloor);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();

        // top
        quad(buffer, pose, sprite, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
                0, 1, 0, r, g, b, a, packedLight, packedOverlay);
        // north / south / east / west walls of the liquid column
        quad(buffer, pose, sprite, x0, yFloor, z0, x1, yFloor, z0, x1, y1, z0, x0, y1, z0,
                0, 0, -1, r, g, b, a, packedLight, packedOverlay);
        quad(buffer, pose, sprite, x1, yFloor, z1, x0, yFloor, z1, x0, y1, z1, x1, y1, z1,
                0, 0, 1, r, g, b, a, packedLight, packedOverlay);
        quad(buffer, pose, sprite, x0, yFloor, z1, x0, yFloor, z0, x0, y1, z0, x0, y1, z1,
                -1, 0, 0, r, g, b, a, packedLight, packedOverlay);
        quad(buffer, pose, sprite, x1, yFloor, z0, x1, yFloor, z1, x1, y1, z1, x1, y1, z0,
                1, 0, 0, r, g, b, a, packedLight, packedOverlay);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float nx, float ny, float nz,
                             float r, float g, float b, float a, int light, int overlay) {
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        vertex(buffer, pose, x4, y4, z4, u0, v0, r, g, b, a, light, overlay, nx, ny, nz);
        vertex(buffer, pose, x3, y3, z3, u1, v0, r, g, b, a, light, overlay, nx, ny, nz);
        vertex(buffer, pose, x2, y2, z2, u1, v1, r, g, b, a, light, overlay, nx, ny, nz);
        vertex(buffer, pose, x1, y1, z1, u0, v1, r, g, b, a, light, overlay, nx, ny, nz);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v,
                               float r, float g, float b, float a,
                               int light, int overlay, float nx, float ny, float nz) {
        buffer.vertex(pose.pose(), x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }
}