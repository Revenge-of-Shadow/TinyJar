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
        poseStack.mulPose(switch (face) {
                    case FLOOR -> Axis.XP.rotationDegrees(0);
                    case CEILING -> Axis.XP.rotationDegrees(180);
                    case WALL -> switch (facing) {
                        case NORTH -> Axis.XP.rotationDegrees(90);
                        case SOUTH -> Axis.XP.rotationDegrees(-90);
                        case EAST -> Axis.ZP.rotationDegrees(90);
                        case WEST -> Axis.ZP.rotationDegrees(-90);
                        default -> Axis.XP.rotationDegrees(0);
                    };
                }
        );
        poseStack.translate(-0.5, -0.5, -0.5);

        FluidRenderHelper.render(be.getTank().getFluid(), be.getTank().getCapacity(), poseStack, bufferSource, packedLight, packedOverlay);
    }
}