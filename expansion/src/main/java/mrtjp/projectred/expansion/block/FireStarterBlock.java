package mrtjp.projectred.expansion.block;

import mrtjp.projectred.core.tile.IBlockEventBlockEntity;
import mrtjp.projectred.expansion.init.ExpansionBlocks;
import mrtjp.projectred.expansion.tile.FireStarterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FireStarterBlock extends BaseDeviceBlock {

    public FireStarterBlock() {
        super(STONE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireStarterBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return ExpansionBlocks.FIRE_STARTER_BLOCK_ENTITY.get();
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader world, BlockPos pos, Direction side) {
        if (super.isFireSource(state, world, pos, side)) return true;

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof IBlockEventBlockEntity) return ((IBlockEventBlockEntity) tile).isFireSource(side.ordinal());
        return false;
    }
}
