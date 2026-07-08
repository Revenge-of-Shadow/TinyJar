package tinyjarmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.antlr.v4.runtime.DiagnosticErrorListener;

import java.util.Collections;
import java.util.List;

public class TinyJar extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light", 0, 15);

    public TinyJar(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(LIGHT_LEVEL, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
        );
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();

        if (clickedFace == Direction.UP) {
            return defaultBlockState()
                    .setValue(FACE, AttachFace.FLOOR)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        if (clickedFace == Direction.DOWN) {
            return defaultBlockState()
                    .setValue(FACE, AttachFace.CEILING)
                    .setValue(FACING, context.getHorizontalDirection());
        }

        return defaultBlockState()
                .setValue(FACE, AttachFace.WALL)
                .setValue(FACING, clickedFace.getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(LIGHT_LEVEL, FACING, FACE);
    }

    @Override
    public VoxelShape getShape(BlockState st, BlockGetter lv, BlockPos pos, CollisionContext cont){
        return switch (st.getValue(FACE)) {
            case FLOOR -> net.minecraft.world.phys.shapes.Shapes.box(
                    5.0/16, 0.0, 5.0/16,
                    11.0/16,8.0/16,11.0/16
            );
            case CEILING -> net.minecraft.world.phys.shapes.Shapes.box(
                    5.0/16, 8.0/16, 5.0/16,
                    11.0/16,16.0/16,11.0/16
            );
            case WALL -> switch (st.getValue(FACING)) {
                case NORTH -> net.minecraft.world.phys.shapes.Shapes.box(
                    5.0/16, 5.0/16, 0.0,
                    11.0/16,11.0/16,8.0/16
                );
                case SOUTH -> net.minecraft.world.phys.shapes.Shapes.box(
                        5.0/16, 5.0/16, 8.0/16,
                        11.0/16,11.0/16,16.0/16
                );
                case EAST  -> net.minecraft.world.phys.shapes.Shapes.box(
                        8.0/16, 5.0/16, 5.0/16,
                        16.0/16,11.0/16,11.0/16
                );
                case WEST -> net.minecraft.world.phys.shapes.Shapes.box(
                        0.0, 5.0/16, 5.0/16,
                        8.0/16,11.0/16,11.0/16
                );
                default -> net.minecraft.world.phys.shapes.Shapes.box(
                        5.0/16, 0.0, 5.0/16,
                        11.0/16,8.0/16,11.0/16
                );
            };
        };
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
