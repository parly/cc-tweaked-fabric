/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Provides a delegate over inventories.
 *
 * This may be used both on {@link net.minecraft.tileentity.TileEntity}s to redirect the inventory to another tile,
 * and by other interfaces to have inventories which change their backing store.
 */
@FunctionalInterface
public interface InventoryDelegate extends Inventory
{
    Inventory getInventory();

    @Override
    default int getInvSize()
    {
        return getInventory().getInvSize();
    }

    @Override
    default boolean isInvEmpty()
    {
        return getInventory().isInvEmpty();
    }

    @Nonnull
    @Override
    default ItemStack getInvStack( int slot )
    {
        return getInventory().getInvStack( slot );
    }

    @Nonnull
    @Override
    default ItemStack takeInvStack( int slot, int count )
    {
        return getInventory().takeInvStack( slot, count );
    }

    @Nonnull
    @Override
    default ItemStack removeInvStack( int slot )
    {
        return getInventory().removeInvStack( slot );
    }

    @Override
    default void setInvStack( int slot, ItemStack stack )
    {
        getInventory().setInvStack( slot, stack );
    }

    @Override
    default int getInvMaxStackAmount()
    {
        return getInventory().getInvMaxStackAmount();
    }

    @Override
    default void markDirty()
    {
        getInventory().markDirty();
    }

    @Override
    default boolean canPlayerUseInv( @Nonnull PlayerEntity player )
    {
        return getInventory().canPlayerUseInv( player );
    }

    @Override
    default void onInvOpen( @Nonnull PlayerEntity player )
    {
        getInventory().onInvOpen( player );
    }

    @Override
    default void onInvClose( @Nonnull PlayerEntity player )
    {
        getInventory().onInvClose( player );
    }

    @Override
    default boolean isValidInvStack( int slot, @Nonnull ItemStack stack )
    {
        return getInventory().isValidInvStack( slot, stack );
    }

    @Override
    default void clear()
    {
        getInventory().clear();
    }

    @Override
    default int countInInv( @Nonnull Item stack )
    {
        return getInventory().countInInv( stack );
    }

    @Override
    default boolean containsAnyInInv( @Nonnull Set<Item> set )
    {
        return getInventory().containsAnyInInv( set );
    }
}
