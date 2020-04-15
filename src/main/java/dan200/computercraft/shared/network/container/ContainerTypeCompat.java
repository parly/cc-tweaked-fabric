/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.network.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import net.minecraft.container.ContainerType;


class ContainerTypeCompat
{
    private static final MethodHandle mh_constructor;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Method method = ContainerType.class.getDeclaredConstructor(ContainerType.Factory.class);
        method.setAccessible(true);
        mh_constructor = lookup.unreflect(method).asType(ContainerType.class, ContainerType.Factory.class);
    }

    private ContainerTypeCompat() { }

    public static <C extends Container> ContainerType<C> create(ContainerType.Factory<C> factory) {
        return (ContainerType.Factory<C>) mh_constructor.invoke(factory);
    }
}
