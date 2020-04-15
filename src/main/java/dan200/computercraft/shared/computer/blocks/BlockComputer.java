/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.util.NamedTileEntityType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block.Settings;

public class BlockComputer extends BlockComputerBase<TileComputer>
{
    public static final EnumProperty<ComputerState> STATE = EnumProperty.of( "state", ComputerState.class );
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public BlockComputer( Settings settings, ComputerFamily family, NamedTileEntityType<? extends TileComputer> type )
    {
        super( settings, family, type );
        setDefaultState( getDefaultState()
            .with( FACING, Direction.NORTH )
            .with( STATE, ComputerState.OFF )
        );
    }

    @Override
    protected void appendProperties( StateManager.Builder<Block, BlockState> builder )
    {
        builder.add( FACING, STATE );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerFacing().getOpposite() );
    }

    @Nonnull
    @Override
    protected ItemStack getItem( TileComputerBase tile )
    {
        return tile instanceof TileComputer ? ComputerItemFactory.create( (TileComputer) tile ) : ItemStack.EMPTY;
    }
}
