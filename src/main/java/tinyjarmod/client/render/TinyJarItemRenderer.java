package tinyjarmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

public class TinyJarItemRenderer extends BlockEntityWithoutLevelRenderer {

    public TinyJarItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel shellModel = Minecraft.getInstance().getModelManager()
                .getModel(new ResourceLocation("tinyjarmod", "item/tiny_jar_shell"));


        itemRenderer.renderModelLists(shellModel, stack, packedLight, packedOverlay,
                poseStack, bufferSource.getBuffer(RenderType.cutout()));

        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            FluidStack fluidStack = handler.getFluidInTank(0);
            FluidRenderHelper.render(fluidStack, handler.getTankCapacity(0),
                    poseStack, bufferSource, packedLight, packedOverlay);
        });
    }
}