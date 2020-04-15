/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.modem.wired;

import dan200.computercraft.ComputerCraft;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;


import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Direction;

public abstract class ItemBlockCable extends BlockItem
{
    private String translationKey;

    public ItemBlockCable( BlockCable block, Settings settings )
    {
        super( block, settings );
    }

    boolean placeAt( World world, BlockPos pos, BlockState state, PlayerEntity player )
    {
        // TODO: Check entity collision.
        if( !state.canPlaceAt( world, pos ) ) return false;

        world.setBlockState( pos, state, 3 );
        BlockSoundGroup soundType = state.getBlock().getSoundType( state, world, pos, player );
        world.playSound( null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F );

        BlockEntity tile = world.getBlockEntity( pos );
        if( tile instanceof TileCable )
        {
            TileCable cable = (TileCable) tile;
            cable.modemChanged();
            cable.connectionsChanged();
        }

        return true;
    }

    boolean placeAtCorrected( World world, BlockPos pos, BlockState state )
    {
        return placeAt( world, pos, correctConnections( world, pos, state ), null );
    }

    @Override
    public void appendStacks( @Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> list )
    {
        if( isIn( group ) ) list.add( new ItemStack( this ) );
    }

    @Nonnull
    @Override
    public String getTranslationKey()
    {
        if( translationKey == null )
        {
            translationKey = Util.createTranslationKey( "block", ForgeRegistries.ITEMS.getKey( this ) );
        }
        return translationKey;
    }

    public static class WiredModem extends ItemBlockCable
    {
        public WiredModem( BlockCable block, Settings settings )
        {
            super( block, settings );
        }

        @Nonnull
        @Override
        public ActionResult place( ItemPlacementContext context )
        {
            ItemStack stack = context.getStack();
            if( stack.isEmpty() ) return ActionResult.FAIL;

            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();
            BlockState existingState = world.getBlockState( pos );

            // Try to add a modem to a cable
            if( existingState.getBlock() == ComputerCraft.Blocks.cable && existingState.get( MODEM ) == CableModemVariant.None )
            {
                Direction side = context.getSide().getOpposite();
                BlockState newState = existingState
                    .with( MODEM, CableModemVariant.from( side ) )
                    .with( CONNECTIONS.get( side ), existingState.get( CABLE ) );
                if( placeAt( world, pos, newState, context.getPlayer() ) )
                {
                    stack.decrement( 1 );
                    return ActionResult.SUCCESS;
                }
            }

            return super.place( context );
        }
    }

    public static class Cable extends ItemBlockCable
    {
        public Cable( BlockCable block, Settings settings )
        {
            super( block, settings );
        }

        @Nonnull
        @Override
        public ActionResult place( ItemPlacementContext context )
        {
            ItemStack stack = context.getStack();
            if( stack.isEmpty() ) return ActionResult.FAIL;

            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();

            // Try to add a cable to a modem inside the block we're clicking on.
            BlockPos insidePos = pos.offset( context.getSide().getOpposite() );
            BlockState insideState = world.getBlockState( insidePos );
            if( insideState.getBlock() == ComputerCraft.Blocks.cable && !insideState.get( BlockCable.CABLE )
                && placeAtCorrected( world, insidePos, insideState.with( BlockCable.CABLE, true ) ) )
            {
                stack.decrement( 1 );
                return ActionResult.SUCCESS;
            }

            // Try to add a cable to a modem adjacent to this block
            BlockState existingState = world.getBlockState( pos );
            if( existingState.getBlock() == ComputerCraft.Blocks.cable && !existingState.get( BlockCable.CABLE )
                && placeAtCorrected( world, pos, existingState.with( BlockCable.CABLE, true ) ) )
            {
                stack.decrement( 1 );
                return ActionResult.SUCCESS;
            }

            return super.place( context );
        }
    }
}
