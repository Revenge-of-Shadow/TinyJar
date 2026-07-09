package tinyjarmod;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import tinyjarmod.client.render.TinyJarItemRenderer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TinyJarBlockItem extends BlockItem {
    public TinyJarBlockItem(TinyJar block, Properties properties){
        super(block, properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt){
        return new FluidHandlerItemStack(stack, TinyJarBlockEntity.CAPACITY_MB);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state){
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);

        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(itemHandler -> {
            FluidStack fromItem = itemHandler.getFluidInTank(0);
            if(!fromItem.isEmpty() && level.getBlockEntity(pos) instanceof TinyJarBlockEntity jar){
                jar.getTank().fill(fromItem, IFluidHandler.FluidAction.EXECUTE);
            }
        });
        return result;
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {
            private TinyJarItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    renderer = new TinyJarItemRenderer(
                            mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return renderer;
            }
        });
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            FluidStack fluidStack = handler.getFluidInTank(0);
            if (fluidStack.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.tinyjarmod.tiny_jar.empty"));
            } else {
                tooltip.add(Component.translatable("tooltip.tinyjarmod.tiny_jar.contains",
                        fluidStack.getAmount(), fluidStack.getDisplayName()));
            }
        });
    }
}
