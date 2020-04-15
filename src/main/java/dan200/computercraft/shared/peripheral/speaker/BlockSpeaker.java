/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.shared.common.BlockGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

import net.minecraft.block.Block.Settings;

public class BlockSpeaker extends BlockGeneric
{
    private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public BlockSpeaker( Settings settings )
    {
        super( settings, TileSpeaker.FACTORY );
        setDefaultState( getStateManager().getDefaultState()
            .with( FACING, Direction.NORTH ) );
    }

    @Override
    protected void appendProperties( StateManager.Builder<Block, BlockState> properties )
    {
        properties.add( FACING );
    }

    @Nullable
    @Override
    public BlockState getPlacementState( ItemPlacementContext placement )
    {
        return getDefaultState().with( FACING, placement.getPlayerFacing().getOpposite() );
    }
}
