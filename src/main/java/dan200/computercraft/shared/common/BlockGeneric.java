/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.common;

import dan200.computercraft.shared.util.NamedTileEntityType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.block.Block.Settings;

public abstract class BlockGeneric extends Block
{
    private final BlockEntityType<? extends TileGeneric> type;

    public BlockGeneric( Settings settings, NamedTileEntityType<? extends TileGeneric> type )
    {
        super( settings );
        this.type = type;
        type.setBlock( this );
    }

    @Override
    @Deprecated
    public final void onBlockRemoved( @Nonnull BlockState block, @Nonnull World world, @Nonnull BlockPos pos, BlockState replace, boolean bool )
    {
        if( block.getBlock() == replace.getBlock() ) return;

        BlockEntity tile = world.getBlockEntity( pos );
        super.onBlockRemoved( block, world, pos, replace, bool );
        world.removeBlockEntity( pos );
        if( tile instanceof TileGeneric ) ((TileGeneric) tile).destroy();
    }

    @Nonnull
    @Override
    @Deprecated
    public final ActionResult onUse( BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        return tile instanceof TileGeneric ? ((TileGeneric) tile).onActivate( player, hand, hit ) : ActionResult.PASS;
    }

    @Override
    @Deprecated
    public final void neighborUpdate( BlockState state, World world, BlockPos pos, Block neighbourBlock, BlockPos neighbourPos, boolean isMoving )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        if( tile instanceof TileGeneric ) ((TileGeneric) tile).onNeighbourChange( neighbourPos );
    }

    @Override
    public final void onNeighborChange( BlockState state, WorldView world, BlockPos pos, BlockPos neighbour )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        if( tile instanceof TileGeneric ) ((TileGeneric) tile).onNeighbourTileEntityChange( neighbour );
    }

    @Override
    @Deprecated
    public void scheduledTick( BlockState state, ServerWorld world, BlockPos pos, Random rand )
    {
        BlockEntity te = world.getBlockEntity( pos );
        if( te instanceof TileGeneric ) ((TileGeneric) te).blockTick();
    }

    @Override
    public boolean hasTileEntity( BlockState state )
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity( @Nonnull BlockState state, @Nonnull BlockView world )
    {
        return type.instantiate();
    }

    @Override
    public boolean canBeReplacedByLeaves( BlockState state, WorldView world, BlockPos pos )
    {
        return false;
    }
}
