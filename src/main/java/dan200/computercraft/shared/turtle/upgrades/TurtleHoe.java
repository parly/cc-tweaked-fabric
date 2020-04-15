/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleVerb;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TurtleHoe extends TurtleTool
{
    public TurtleHoe( Identifier id, String adjective, Item item )
    {
        super( id, adjective, item );
    }

    public TurtleHoe( Identifier id, Item item )
    {
        super( id, item );
    }

    public TurtleHoe( Identifier id, ItemStack craftItem, ItemStack toolItem )
    {
        super( id, craftItem, toolItem );
    }

    @Override
    protected boolean canBreakBlock( BlockState state, World world, BlockPos pos, TurtlePlayer player )
    {
        if( !super.canBreakBlock( state, world, pos, player ) ) return false;

        Material material = state.getMaterial();
        return material == Material.PLANT ||
            material == Material.CACTUS ||
            material == Material.PUMPKIN ||
            material == Material.LEAVES ||
            material == Material.UNDERWATER_PLANT ||
            material == Material.REPLACEABLE_PLANT;
    }

    @Nonnull
    @Override
    public TurtleCommandResult useTool( @Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull Direction direction )
    {
        if( verb == TurtleVerb.DIG )
        {
            ItemStack hoe = item.copy();
            ItemStack remainder = TurtlePlaceCommand.deploy( hoe, turtle, direction, null, null );
            if( remainder != hoe )
            {
                return TurtleCommandResult.success();
            }
        }
        return super.useTool( turtle, side, verb, direction );
    }
}
