/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public final class InventoryUtil
{
    private InventoryUtil() {}
    // Methods for comparing things:

    public static boolean areItemsEqual( @Nonnull ItemStack a, @Nonnull ItemStack b )
    {
        return a == b || ItemStack.areEqualIgnoreDamage( a, b );
    }

    public static boolean areItemsStackable( @Nonnull ItemStack a, @Nonnull ItemStack b )
    {
        return a == b || (a.getItem() == b.getItem() && ItemStack.areTagsEqual( a, b ));
    }

    /**
     * Determines if two items are "mostly" equivalent. Namely, they have the same item and damage, and identical
     * share stacks.
     *
     * This is largely based on {@link net.minecraftforge.common.crafting.IngredientNBT#test(ItemStack)}. It is
     * sufficient to ensure basic information (such as enchantments) are the same, while not having to worry about
     * capabilities.
     *
     * @param a The first stack to check
     * @param b The second stack to check
     * @return If these items are largely the same.
     */
    public static boolean areItemsSimilar( @Nonnull ItemStack a, @Nonnull ItemStack b )
    {
        if( a == b ) return true;
        if( a.isEmpty() ) return !b.isEmpty();

        if( a.getItem() != b.getItem() ) return false;

        // A more expanded form of ItemStack.areShareTagsEqual, but allowing an empty tag to be equal to a
        // null one.
        CompoundTag shareTagA = a.getItem().getShareTag( a );
        CompoundTag shareTagB = b.getItem().getShareTag( b );
        if( shareTagA == shareTagB ) return true;
        if( shareTagA == null ) return shareTagB.isEmpty();
        if( shareTagB == null ) return shareTagA.isEmpty();
        return shareTagA.equals( shareTagB );
    }

    // Methods for finding inventories:

    public static Inventory getInventory( World world, BlockPos pos, Direction side )
    {
        // Look for tile with inventory
        BlockEntity tileEntity = world.getBlockEntity( pos );
        if( tileEntity instanceof Inventory )
        {
            return (Inventory) tileEntity;
        }

        // Look for entity with inventory
        Vec3d vecStart = new Vec3d(
            pos.getX() + 0.5 + 0.6 * side.getOffsetX(),
            pos.getY() + 0.5 + 0.6 * side.getOffsetY(),
            pos.getZ() + 0.5 + 0.6 * side.getOffsetZ()
        );
        Direction dir = side.getOpposite();
        Vec3d vecDir = new Vec3d(
            dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ()
        );
        Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities( world, vecStart, vecDir, 1.1 );
        if( hit != null )
        {
            Entity entity = hit.getKey();
            if( entity instanceof Inventory )
            {
                return (Inventory) entity;
            }
        }
        return null;
    }

    // Methods for placing into inventories:

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack itemstack, ItemStorage inventory, int begin )
    {
        return storeItems( itemstack, inventory, 0, inventory.getSlots(), begin );
    }

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack itemstack, ItemStorage inventory )
    {
        return storeItems( itemstack, inventory, 0, inventory.getSlots(), 0 );
    }

    @Nonnull
    public static ItemStack storeItems( @Nonnull ItemStack stack, ItemStorage inventory, int start, int range, int begin )
    {
        if( stack.isEmpty() ) return ItemStack.EMPTY;

        // Inspect the slots in order and try to find empty or stackable slots
        ItemStack remainder = stack.copy();
        for( int i = 0; i < range; i++ )
        {
            int slot = start + (i + begin - start) % range;
            if( remainder.isEmpty() ) break;
            remainder = inventory.insertItem( slot, remainder, false );
        }
        return areItemsEqual( stack, remainder ) ? stack : remainder;
    }

    // Methods for taking out of inventories

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory, int begin )
    {
        return takeItems( count, inventory, 0, inventory.getSlots(), begin );
    }

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory )
    {
        return takeItems( count, inventory, 0, inventory.getSlots(), 0 );
    }

    @Nonnull
    public static ItemStack takeItems( int count, ItemStorage inventory, int start, int range, int begin )
    {
        // Combine multiple stacks from inventory into one if necessary
        ItemStack partialStack = ItemStack.EMPTY;
        for( int i = 0; i < range; i++ )
        {
            int slot = start + (i + begin - start) % range;

            // If we've extracted all items, return
            if( count <= 0 ) break;

            // If this doesn't slot, abort.
            ItemStack stack = inventory.getStackInSlot( slot );
            if( !stack.isEmpty() && (partialStack.isEmpty() || areItemsStackable( stack, partialStack )) )
            {
                ItemStack extracted = inventory.extractItem( slot, count, false );
                if( !extracted.isEmpty() )
                {
                    if( partialStack.isEmpty() )
                    {
                        // If we've extracted for this first time, then limit the count to the maximum stack size.
                        partialStack = extracted;
                        count = Math.min( count, extracted.getMaxCount() );
                    }
                    else
                    {
                        partialStack.increment( extracted.getCount() );
                    }

                    count -= extracted.getCount();
                }
            }

        }

        return partialStack;
    }
}
