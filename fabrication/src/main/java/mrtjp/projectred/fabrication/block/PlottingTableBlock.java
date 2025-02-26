package mrtjp.projectred.fabrication.block;

import mrtjp.projectred.core.block.ProjectRedBlock;
import mrtjp.projectred.fabrication.init.FabricationBlocks;
import mrtjp.projectred.fabrication.tile.PlottingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PlottingTableBlock extends FabricationMachineBlock {

    public PlottingTableBlock() {
        super(ProjectRedBlock.STONE_MACHINE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlottingTableBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return FabricationBlocks.PLOTTING_TABLE_BLOCK_ENTITY.get();
    }
}
