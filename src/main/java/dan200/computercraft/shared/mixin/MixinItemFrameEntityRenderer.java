/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.mixin;

import dan200.computercraft.client.render.ItemPrintoutRenderer;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( ItemFrameEntityRenderer.class )
public class MixinItemFrameEntityRenderer
{
    @Inject( method = "render", at = @At( "HEAD" ), cancellable = true )
    private void render( ItemFrameEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info )
    {
        ItemStack stack = entity.getHeldItemStack();
        if( ItemPrintoutRenderer.onRenderInFrame( stack, matrices, vertexConsumers ) )
        {
            info.cancel();
        }
    }
}
