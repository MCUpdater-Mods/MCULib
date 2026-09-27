package com.mcupdater.mculib.block;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.helpers.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    public static final BooleanProperty ENABLED =  BlockStateProperties.ENABLED;

    public AbstractMachineBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false).setValue(ENABLED, true));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING,ACTIVE,ENABLED);
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.has(DataComponents.CUSTOM_NAME)) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AbstractMachineBlockEntity) {
                ((AbstractMachineBlockEntity) blockEntity).setCustomName(pStack.getHoverName());
            }
        }
        @Nullable IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null) {
            if (pLevel.getBlockEntity(pPos) instanceof AbstractMachineBlockEntity machineBlockEntity) {
                machineBlockEntity.getEnergyStorage().setEnergy(energyStorage.getEnergyStored());
            }
        }

    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof IMachineGuiProvider) {
                Map<Direction, String> adjacentNames = DataHelper.getAdjacentNames(pLevel, pPos);
                pPlayer.openMenu((MenuProvider) blockEntity, (buf -> {
                    buf.writeBlockPos(pPos);
                    DataHelper.writeDirectionMap(buf,adjacentNames);
                }));
            } else {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) dropInventory(pLevel, pPos);
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    /**
     * Called by onRemove to drop the contents of the machine. Override for custom behavior (i.e. excluding phantom slots)
     *
     * @param pLevel
     * @param pPos
     */
    protected void dropInventory(Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof AbstractConfigurableBlockEntity entity) {
            Container container = entity.getItemHandler();
            if (container != null) {
                Containers.dropContents(pLevel, pPos, container);
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractConfigurableBlockEntity entity) {
            switch (entity.comparatorBehavior.resourceType()) {
                case "power":
                    return entity.getEnergyStorage().getComparatorOutput(entity.comparatorBehavior.inverted());
                case "fluids":
                    return entity.getFluidHandler().getComparatorOutput(entity.comparatorBehavior.inverted());
                case "items":
                    return entity.getItemHandler().getComparatorOutput(entity.comparatorBehavior.inverted());
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        this.updateActive(level, pos, state);
    }

    protected void updateActive(Level level, BlockPos pos, BlockState state) {
        boolean currentState = state.getValue(ENABLED);
        boolean newState = false;
        boolean signal = level.hasNeighborSignal(pos);
        if (level.getBlockEntity(pos) instanceof AbstractConfigurableBlockEntity entity) {
            switch (entity.signalBehavior) {
                case IGNORE:
                    newState = true;
                    break;
                case REQUIRED:
                    newState = signal;
                    break;
                case INVERTED:
                    newState = !signal;
            }
            if (currentState != newState) {
                level.setBlock(pos, state.setValue(ENABLED, newState), 2);
            }
        }
    }
}
