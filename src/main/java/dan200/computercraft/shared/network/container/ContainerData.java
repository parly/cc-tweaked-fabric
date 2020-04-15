/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.network.container;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

/**
 * An extension over the basic {@link IForgeContainerType}/{@link NetworkHooks#openGui(ServerPlayerEntity, INamedContainerProvider, Consumer)}
 * hooks, with a more convenient way of reading and writing data.
 */
public interface ContainerData
{
    void toBytes( PacketByteBuf buf );

    default void open( PlayerEntity player, NameableContainerFactory owner )
    {
        ContainerProviderRegistry.INSTANCE.openContainer( (ServerPlayerEntity) player, new Identifier( ComputerCraft.MOD_ID, owner.getDisplayName().getString() ), this::toBytes );
    }

    static <C extends Container, T extends ContainerData> ContainerType<C> toType( Function<PacketByteBuf, T> reader, Factory<C, T> factory )
    {
        return ContainerTypeCompat.create( ( id, player, data ) -> factory.create( id, player, reader.apply( data ) ) );
    }

    interface Factory<C extends Container, T extends ContainerData>
    {
        C create( int id, @Nonnull PlayerInventory inventory, T data );
    }
}
