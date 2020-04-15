/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dan200.computercraft.shared.peripheral.printer.ContainerPrinter;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class GuiPrinter extends ContainerScreen<ContainerPrinter>
{
    private static final Identifier BACKGROUND = new Identifier( "computercraft", "textures/gui/printer.png" );

    public GuiPrinter( ContainerPrinter container, PlayerInventory player, Text title )
    {
        super( container, player, title );
    }

    @Override
    protected void drawForeground( int mouseX, int mouseY )
    {
        String title = getTitle().asFormattedString();
        font.draw( title, (containerWidth - font.getStringWidth( title )) / 2.0f, 6, 0x404040 );
        font.draw( I18n.translate( "container.inventory" ), 8, containerHeight - 96 + 2, 0x404040 );
    }

    @Override
    protected void drawBackground( float partialTicks, int mouseX, int mouseY )
    {
        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
        minecraft.getTextureManager().bindTexture( BACKGROUND );
        blit( x, y, 0, 0, containerWidth, containerHeight );

        if( getContainer().isPrinting() ) blit( x + 34, y + 21, 176, 0, 25, 45 );
    }

    @Override
    public void render( int mouseX, int mouseY, float partialTicks )
    {
        renderBackground();
        super.render( mouseX, mouseY, partialTicks );
        drawMouseoverTooltip( mouseX, mouseY );
    }
}
