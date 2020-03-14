/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.printer;

import net.minecraft.container.ArrayPropertyDelegate;
import net.minecraft.container.Container;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

import static dan200.computercraft.shared.peripheral.printer.TilePrinter.PROPERTY_PRINTING;

public class ContainerPrinter extends Container
{
    private final Inventory m_printer;
    private final PropertyDelegate properties;

    public ContainerPrinter( int id, PlayerInventory player, TilePrinter printer )
    {
        this( id, player, printer, printer );
    }

    public ContainerPrinter( int id, PlayerInventory player )
    {
        this( id, player, new BasicInventory( TilePrinter.INVENTORY_SIZE ), new ArrayPropertyDelegate( TilePrinter.PROPERTY_SIZE ) );
    }

    public ContainerPrinter( int id, PlayerInventory playerInventory, Inventory printer, PropertyDelegate printerInfo )
    {
        super( null, id );
        m_printer = printer;
        properties = printerInfo;

        // Ink slot
        addSlot( new Slot( printer, 0, 13, 35 ) );

        // In-tray
        for( int x = 0; x < 6; x++ ) addSlot( new Slot( printer, x + 1, 61 + x * 18, 22 ) );

        // Out-tray
        for( int x = 0; x < 6; x++ ) addSlot( new Slot( printer, x + 7, 61 + x * 18, 49 ) );

        // Player inv
        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 ) );
            }
        }

        // Player hotbar
        for( int x = 0; x < 9; x++ ) addSlot( new Slot( playerInventory, x, 8 + x * 18, 142 ) );

        addProperties( printerInfo );
    }

    public boolean isPrinting()
    {
        return properties.get( PROPERTY_PRINTING ) != 0;
    }

    @Override
    public boolean canUse( @Nonnull PlayerEntity player )
    {
        return m_printer.canPlayerUseInv( player );
    }

    @Nonnull
    @Override
    public ItemStack transferSlot( PlayerEntity player, int index )
    {
        Slot slot = slots.get( index );
        if( slot == null || !slot.hasStack() ) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack result = stack.copy();
        if( index < 13 )
        {
            // Transfer from printer to inventory
            if( !insertItem( stack, 13, 49, true ) ) return ItemStack.EMPTY;
        }
        else
        {
            // Transfer from inventory to printer
            if( stack.getItem() instanceof DyeItem )
            {
                if( !insertItem( stack, 0, 1, false ) ) return ItemStack.EMPTY;
            }
            else //if is paper
            {
                if( !insertItem( stack, 1, 13, false ) ) return ItemStack.EMPTY;
            }
        }

        if( stack.isEmpty() )
        {
            slot.setStack( ItemStack.EMPTY );
        }
        else
        {
            slot.markDirty();
        }

        if( stack.getCount() == result.getCount() ) return ItemStack.EMPTY;

        slot.onTakeItem( player, stack );
        return result;
    }
}
