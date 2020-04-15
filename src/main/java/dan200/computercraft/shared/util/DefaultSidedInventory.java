/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DefaultSidedInventory extends DefaultInventory, SidedInventory
{
    @Override
    default boolean canInsertInvStack( int slot, @Nonnull ItemStack stack, @Nullable Direction side )
    {
        return isValidInvStack( slot, stack );
    }

    @Override
    default boolean canExtractInvStack( int slot, @Nonnull ItemStack stack, @Nonnull Direction side )
    {
        return true;
    }
}
