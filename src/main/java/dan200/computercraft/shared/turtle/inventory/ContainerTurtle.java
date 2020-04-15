/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.inventory;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputerBase;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import dan200.computercraft.shared.network.container.ContainerData;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.util.SingleIntArray;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.container.ArrayPropertyDelegate;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class ContainerTurtle extends ContainerComputerBase
{
    public static final ContainerType<ContainerTurtle> TYPE = ContainerData.toType( ComputerContainerData::new, ContainerTurtle::new );

    public static final int PLAYER_START_Y = 134;
    public static final int TURTLE_START_X = 175;

    private final PropertyDelegate properties;

    private ContainerTurtle(
        int id, Predicate<PlayerEntity> canUse, IComputer computer, ComputerFamily family,
        PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties
    )
    {
        super( TYPE, id, canUse, computer, family );
        this.properties = properties;

        addProperties( properties );

        // Turtle inventory
        for( int y = 0; y < 4; y++ )
        {
            for( int x = 0; x < 4; x++ )
            {
                addSlot( new Slot( inventory, x + y * 4, TURTLE_START_X + 1 + x * 18, PLAYER_START_Y + 1 + y * 18 ) );
            }
        }

        // Player inventory
        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlot( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, PLAYER_START_Y + 1 + y * 18 ) );
            }
        }

        // Player hotbar
        for( int x = 0; x < 9; x++ )
        {
            addSlot( new Slot( playerInventory, x, 8 + x * 18, PLAYER_START_Y + 3 * 18 + 5 ) );
        }
    }

    public ContainerTurtle( int id, PlayerInventory player, TurtleBrain turtle )
    {
        this(
            id, p -> turtle.getOwner().canPlayerUseInv( p ), turtle.getOwner().createServerComputer(), turtle.getFamily(),
            player, turtle.getInventory(), (SingleIntArray) turtle::getSelectedSlot
        );
    }

    private ContainerTurtle( int id, PlayerInventory player, ComputerContainerData data )
    {
        this(
            id, x -> true, getComputer( player, data ), data.getFamily(),
            player, new BasicInventory( TileTurtle.INVENTORY_SIZE ), new ArrayPropertyDelegate( 1 )
        );
    }

    public int getSelectedSlot()
    {
        return properties.get( 0 );
    }

    @Nonnull
    private ItemStack tryItemMerge( PlayerEntity player, int slotNum, int firstSlot, int lastSlot, boolean reverse )
    {
        Slot slot = slots.get( slotNum );
        ItemStack originalStack = ItemStack.EMPTY;
        if( slot != null && slot.hasStack() )
        {
            ItemStack clickedStack = slot.getStack();
            originalStack = clickedStack.copy();
            if( !insertItem( clickedStack, firstSlot, lastSlot, reverse ) )
            {
                return ItemStack.EMPTY;
            }

            if( clickedStack.isEmpty() )
            {
                slot.setStack( ItemStack.EMPTY );
            }
            else
            {
                slot.markDirty();
            }

            if( clickedStack.getCount() != originalStack.getCount() )
            {
                slot.onTakeItem( player, clickedStack );
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }
        return originalStack;
    }

    @Nonnull
    @Override
    public ItemStack transferSlot( PlayerEntity player, int slotNum )
    {
        if( slotNum >= 0 && slotNum < 16 )
        {
            return tryItemMerge( player, slotNum, 16, 52, true );
        }
        else if( slotNum >= 16 )
        {
            return tryItemMerge( player, slotNum, 0, 16, false );
        }
        return ItemStack.EMPTY;
    }
}
