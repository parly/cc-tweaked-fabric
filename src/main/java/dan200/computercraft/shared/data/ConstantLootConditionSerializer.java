/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.Identifier;
import net.minecraft.loot.condition.LootCondition;

import javax.annotation.Nonnull;

public final class ConstantLootConditionSerializer<T extends LootCondition> extends LootCondition.Factory<T>
{
    private final T instance;

    private ConstantLootConditionSerializer( Identifier id, Class<T> klass, T instance )
    {
        super( id, klass );
        this.instance = instance;
    }

    public static <T extends LootCondition> LootCondition.Factory<T> of( Identifier id, Class<T> klass, T instance )
    {
        return new ConstantLootConditionSerializer<>( id, klass, instance );
    }

    @Override
    public void toJson( @Nonnull JsonObject json, @Nonnull T object, @Nonnull JsonSerializationContext context )
    {
    }

    @Nonnull
    @Override
    public T fromJson( @Nonnull JsonObject json, @Nonnull JsonDeserializationContext context )
    {
        return instance;
    }
}
