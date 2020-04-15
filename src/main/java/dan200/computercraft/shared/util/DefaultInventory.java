/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface DefaultInventory extends Inventory
{
    @Override
    default int getInvMaxStackAmount()
    {
        return 64;
    }

    @Override
    default void onInvOpen( @Nonnull PlayerEntity player )
    {
    }

    @Override
    default void onInvClose( @Nonnull PlayerEntity player )
    {
    }

    @Override
    default boolean isValidInvStack( int slot, @Nonnull ItemStack stack )
    {
        return true;
    }
}
