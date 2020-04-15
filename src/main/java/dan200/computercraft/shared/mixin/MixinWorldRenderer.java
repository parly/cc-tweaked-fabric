/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.mixin;

import dan200.computercraft.client.render.CableHighlightRenderer;
import dan200.computercraft.client.render.MonitorHighlightRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( WorldRenderer.class )
public class MixinWorldRenderer
{
    @Inject( method = "render", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline" ), cancellable = true )
    private void render( MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info )
    {
        HitResult hit = ((WorldRenderer) (Object) this).client.crosshairTarget;
        BlockHitResult blockHit = (BlockHitResult) hit;
        if( CableHighlightRenderer.drawHighlight( camera, blockHit ) ||
            MonitorHighlightRenderer.drawHighlight( camera, blockHit ) )
        {
            info.cancel();
        }
    }
}
