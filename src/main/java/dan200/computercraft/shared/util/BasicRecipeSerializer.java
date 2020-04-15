/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * A {@link IRecipeSerializer} which implements all the Forge registry entries.
 *
 * @param <T> The reciep serializer
 */
public abstract class BasicRecipeSerializer<T extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T>
{
}
