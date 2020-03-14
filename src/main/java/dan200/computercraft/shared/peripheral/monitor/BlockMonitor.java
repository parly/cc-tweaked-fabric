/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.monitor;

import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.util.NamedBlockEntityType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockMonitor extends BlockGeneric
{
    public static final DirectionProperty ORIENTATION = DirectionProperty.of( "orientation",
        Direction.UP, Direction.DOWN, Direction.NORTH );

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    static final EnumProperty<MonitorEdgeState> STATE = EnumProperty.of( "state", MonitorEdgeState.class );

    public BlockMonitor( Settings settings, NamedBlockEntityType<? extends TileGeneric> type )
    {
        super( settings, type );
        setDefaultState( getStateManager().getDefaultState()
            .with( ORIENTATION, Direction.NORTH )
            .with( FACING, Direction.NORTH )
            .with( STATE, MonitorEdgeState.NONE ) );
    }

    @Override
    public RenderLayer getRenderLayer()
    {
        return RenderLayer.CUTOUT;
    }

    @Override
    protected void appendProperties( StateManager.Builder<Block, BlockState> builder )
    {
        builder.add( ORIENTATION, FACING, STATE );
    }

    @Override
    @Nullable
    public BlockState getPlacementState( ItemPlacementContext context )
    {
        float pitch = context.getPlayer() == null ? 0 : context.getPlayer().pitch;
        Direction orientation;
        if( pitch > 66.5f )
        {
            // If the player is looking down, place it facing upwards
            orientation = Direction.UP;
        }
        else if( pitch < -66.5f )
        {
            // If they're looking up, place it down.
            orientation = Direction.DOWN;
        }
        else
        {
            orientation = Direction.NORTH;
        }

        return getDefaultState()
            .with( FACING, context.getPlayerFacing().getOpposite() )
            .with( ORIENTATION, orientation );
    }

    @Override
    public void onPlaced( World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack )
    {
        super.onPlaced( world, pos, blockState, livingEntity, itemStack );

        BlockEntity entity = world.getBlockEntity( pos );
        if( entity instanceof TileMonitor && !world.isClient )
        {
            TileMonitor monitor = (TileMonitor) entity;
            monitor.contractNeighbours();
            monitor.contract();
            monitor.expand();
        }
    }
}
