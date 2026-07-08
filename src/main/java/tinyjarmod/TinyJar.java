package tinyjarmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Collections;
import java.util.List;

public class TinyJar extends Block implements EntityBlock {
    private static final VoxelShape SHAPE =
            net.minecraft.world.phys.shapes.Shapes.box(
                    5.0/16, 0.0, 5.0/16,
                    11.0/16,8.0/16,11.0/16
            );
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light", 0, 15);

    public TinyJar(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(LIGHT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter lv, BlockPos pos, CollisionContext cont){
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState st){
        return true;    //  Does not obstruct light.
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState st){
        return new TinyJarBlockEntity(pos, st);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lv, BlockState state, BlockEntityType<T> type){
        return lv.isClientSide ? null
                : (lvl, pos, st, be) -> {
            if(be instanceof TinyJarBlockEntity jar) jar.serverTick(lvl, pos, st);
        };
    }
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder){
        ItemStack stack = new ItemStack(this);
        var blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(blockEntity instanceof TinyJarBlockEntity jar){
            FluidStack fluid = jar.getTank().getFluid();
            if(!fluid.isEmpty()){
                stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler ->
                        handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE));
            }
        }

        return Collections.singletonList(stack);
    }
}
