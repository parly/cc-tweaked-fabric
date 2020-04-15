/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.pocket.inventory;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputerBase;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import dan200.computercraft.shared.network.container.ContainerData;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ContainerPocketComputer extends ContainerComputerBase
{
    public static final ContainerType<ContainerPocketComputer> TYPE = ContainerData.toType( ComputerContainerData::new, ContainerPocketComputer::new );

    private ContainerPocketComputer( int id, ServerComputer computer, ItemPocketComputer item, Hand hand )
    {
        super( TYPE, id, p -> {
            ItemStack stack = p.getStackInHand( hand );
            return stack.getItem() == item && ItemPocketComputer.getServerComputer( stack ) == computer;
        }, computer, item.getFamily() );
    }

    private ContainerPocketComputer( int id, PlayerInventory player, ComputerContainerData data )
    {
        super( TYPE, id, player, data );
    }

    public static class Factory implements NameableContainerFactory
    {

        private final ServerComputer computer;
        private final Text name;
        private final ItemPocketComputer item;
        private final Hand hand;

        public Factory( ServerComputer computer, ItemStack stack, ItemPocketComputer item, Hand hand )
        {
            this.computer = computer;
            this.name = stack.getName();
            this.item = item;
            this.hand = hand;
        }


        @Nonnull
        @Override
        public Text getDisplayName()
        {
            return name;
        }

        @Nullable
        @Override
        public Container createMenu( int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity entity )
        {
            return new ContainerPocketComputer( id, computer, item, hand );
        }
    }
}
