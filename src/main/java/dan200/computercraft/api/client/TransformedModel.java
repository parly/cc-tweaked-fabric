/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */
package dan200.computercraft.api.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A model to render, combined with a transformation matrix to apply.
 */
public final class TransformedModel
{
    private final BakedModel model;
    private final Rotation3 matrix;

    public TransformedModel( @Nonnull BakedModel model, @Nonnull Rotation3 matrix )
    {
        this.model = Objects.requireNonNull( model );
        this.matrix = Objects.requireNonNull( matrix );
    }

    public TransformedModel( @Nonnull BakedModel model )
    {
        this.model = Objects.requireNonNull( model );
        this.matrix = Rotation3.identity();
    }

    public static TransformedModel of( @Nonnull ModelIdentifier location )
    {
        BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
        return new TransformedModel( modelManager.getModel( location ) );
    }

    public static TransformedModel of( @Nonnull ItemStack item, @Nonnull Rotation3 transform )
    {
        BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel( item );
        return new TransformedModel( model, transform );
    }

    @Nonnull
    public BakedModel getModel()
    {
        return model;
    }

    @Nonnull
    public Rotation3 getMatrix()
    {
        return matrix;
    }
}
