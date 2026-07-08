package tinyjarmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TinyJarBlockEntity extends BlockEntity {
    public static final int CAPACITY_MB = 90;

    private final FluidTank tank = new FluidTank(CAPACITY_MB){
        @Override
        protected void onContentsChanged(){
            setChanged();
            updateLight();
        }
    };

    private final LazyOptional<FluidTank> tankCap = LazyOptional.of(()->tank);

    public TinyJarBlockEntity(BlockPos pos, BlockState st){
        super(TinyJarMod.TINY_JAR_ENTITY.get(), pos, st);
    }

    public FluidTank getTank() {
        return tank;
    }

    private void updateLight(){
        if(level == null || level.isClientSide) return;
        int target = tank.getFluid().isEmpty() ? 0
                : Math.min(15, tank.getFluid().getFluid().getFluidType().getLightLevel(tank.getFluid()));
        System.out.println("[TinyJar] fluid=" + tank.getFluid() + " target light=" + target);

        BlockState current = getBlockState();
        if(current.getValue(TinyJar.LIGHT_LEVEL) != target){
            level.setBlock(worldPosition, current.setValue(TinyJar.LIGHT_LEVEL, target), 3);
        }
    }

    public void serverTick(Level level, BlockPos pos, BlockState state){}

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
        if(cap == ForgeCapabilities.FLUID_HANDLER) return tankCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        tankCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }
    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
    }

}
