/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.printer;

import dan200.computercraft.shared.common.BlockGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.stat.Stats;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block.Settings;

public class BlockPrinter extends BlockGeneric
{
    private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    static final BooleanProperty TOP = BooleanProperty.of( "top" );
    static final BooleanProperty BOTTOM = BooleanProperty.of( "bottom" );

    public BlockPrinter( Settings settings )
    {
        super( settings, TilePrinter.FACTORY );
        setDefaultState( getStateManager().getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( TOP, false )
            .with( BOTTOM, false ) );
    }

    @Override
    protected void appendProperties( StateManager.Builder<Block, BlockState> properties )
    {
        properties.add( FACING, TOP, BOTTOM );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerFacing().getOpposite() );
    }

    @Override
    public void afterBreak( @Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity te, ItemStack stack )
    {
        if( te instanceof Nameable && ((Nameable) te).hasCustomName() )
        {
            player.incrementStat( Stats.MINED.getOrCreateStat( this ) );
            player.addExhaustion( 0.005F );

            ItemStack result = new ItemStack( this );
            result.setCustomName( ((Nameable) te).getCustomName() );
            dropStack( world, pos, result );
        }
        else
        {
            super.afterBreak( world, player, pos, state, te, stack );
        }
    }

    @Override
    public void onPlaced( World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack )
    {
        if( stack.hasCustomName() )
        {
            BlockEntity tileentity = world.getBlockEntity( pos );
            if( tileentity instanceof TilePrinter ) ((TilePrinter) tileentity).customName = stack.getName();
        }
    }
}
