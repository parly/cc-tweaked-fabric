/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.diskdrive;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerDiskDrive extends Container
{
    public static final ContainerType<ContainerDiskDrive> TYPE = new ContainerType<>( ContainerDiskDrive::new );

    private final Inventory inventory;

    public ContainerDiskDrive( int id, PlayerInventory player, Inventory inventory )
    {
        super( TYPE, id );

        this.inventory = inventory;

        addSlot( new Slot( this.inventory, 0, 8 + 4 * 18, 35 ) );

        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( player, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 ) );
            }
        }

        for( int x = 0; x < 9; x++ )
        {
            addSlot( new Slot( player, x, 8 + x * 18, 142 ) );
        }
    }

    private ContainerDiskDrive( int id, PlayerInventory player )
    {
        this( id, player, new BasicInventory( 1 ) );
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        return inventory.canPlayerUseInv( player );
    }

    @Nonnull
    @Override
    public ItemStack transferSlot( PlayerEntity player, int slotIndex )
    {
        Slot slot = slots.get( slotIndex );
        if( slot == null || !slot.hasStack() ) return ItemStack.EMPTY;

        ItemStack existing = slot.getStack().copy();
        ItemStack result = existing.copy();
        if( slotIndex == 0 )
        {
            // Insert into player inventory
            if( !insertItem( existing, 1, 37, true ) ) return ItemStack.EMPTY;
        }
        else
        {
            // Insert into drive inventory
            if( !insertItem( existing, 0, 1, false ) ) return ItemStack.EMPTY;
        }

        if( existing.isEmpty() )
        {
            slot.setStack( ItemStack.EMPTY );
        }
        else
        {
            slot.markDirty();
        }

        if( existing.getCount() == result.getCount() ) return ItemStack.EMPTY;

        slot.onTakeItem( player, existing );
        return result;
    }
}
