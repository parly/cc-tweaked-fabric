/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.render;

import net.minecraft.client.util.math.MatrixStack;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


/**
 * Emulates map and item-frame rendering for printouts.
 */
@Mod.EventBusSubscriber( modid = ComputerCraft.MOD_ID, value = EnvType.CLIENT )
public final class ItemPrintoutRenderer extends ItemMapLikeRenderer
{
    private static final ItemPrintoutRenderer INSTANCE = new ItemPrintoutRenderer();

    private ItemPrintoutRenderer()
    {
    }

    @SubscribeEvent
    public static void onRenderInHand( RenderHandEvent event )
    {
        ItemStack stack = event.getItemStack();
        if( !(stack.getItem() instanceof ItemPrintout) ) return;

        event.setCanceled( true );
        INSTANCE.renderItemFirstPerson(
            event.getMatrixStack(), event.getBuffers(), event.getLight(),
            event.getHand(), event.getInterpolatedPitch(), event.getEquipProgress(), event.getSwingProgress(), event.getItemStack()
        );
    }

    @Override
    protected void renderItem( MatrixStack transform, VertexConsumerProvider render, ItemStack stack )
    {
        transform.multiply( Vector3f.POSITIVE_X.getDegreesQuaternion( 180f ) );
        transform.scale( 0.42f, 0.42f, -0.42f );
        transform.translate( -0.5f, -0.48f, 0.0f );

        drawPrintout( transform, render, stack );
    }

    public static boolean onRenderInFrame( ItemStack stack, MatrixStack transform, VertexConsumerProvider buffers )
    {
        if( !(stack.getItem() instanceof ItemPrintout) ) return false;

        // Move a little bit forward to ensure we're not clipping with the frame
        transform.translate( 0.0f, 0.0f, -0.001f );
        transform.multiply( Vector3f.POSITIVE_Z.getDegreesQuaternion( 180f ) );
        transform.scale( 0.95f, 0.95f, -0.95f );
        transform.translate( -0.5f, -0.5f, 0.0f );

        drawPrintout( transform, buffers, stack );

        return true;
    }

    private static void drawPrintout( MatrixStack transform, VertexConsumerProvider render, ItemStack stack )
    {
        int pages = ItemPrintout.getPageCount( stack );
        boolean book = ((ItemPrintout) stack.getItem()).getType() == ItemPrintout.Type.BOOK;

        double width = LINE_MAX_LENGTH * FONT_WIDTH + X_TEXT_MARGIN * 2;
        double height = LINES_PER_PAGE * FONT_HEIGHT + Y_TEXT_MARGIN * 2;

        // Non-books will be left aligned
        if( !book ) width += offsetAt( pages );

        double visualWidth = width, visualHeight = height;

        // Meanwhile books will be centred
        if( book )
        {
            visualWidth += 2 * COVER_SIZE + 2 * offsetAt( pages );
            visualHeight += 2 * COVER_SIZE;
        }

        double max = Math.max( visualHeight, visualWidth );

        // Scale the printout to fit correctly.
        float scale = (float) (1.0 / max);
        transform.scale( scale, scale, scale );
        transform.translate( (max - width) / 2.0, (max - height) / 2.0, 0.0 );

        Matrix4f matrix = transform.peek().getModel();
        drawBorder( matrix, render, 0, 0, -0.01f, 0, pages, book );
        drawText( matrix, render,
            X_TEXT_MARGIN, Y_TEXT_MARGIN, 0, ItemPrintout.getText( stack ), ItemPrintout.getColours( stack )
        );
    }
}
