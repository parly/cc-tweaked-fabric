/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.client.util.math.Vector3f;

public class TileEntityMonitorRenderer extends BlockEntityRenderer<TileMonitor>
{
    /**
     * {@link TileMonitor#RENDER_MARGIN}, but a tiny bit of additional padding to ensure that there is no space between
     * the monitor frame and contents.
     */
    private static final float MARGIN = (float) (TileMonitor.RENDER_MARGIN * 1.1);

    private static final Matrix4f IDENTITY = Rotation3.identity().getMatrix();

    public TileEntityMonitorRenderer( BlockEntityRenderDispatcher rendererDispatcher )
    {
        super( rendererDispatcher );
    }

    @Override
    public void render( @Nonnull TileMonitor monitor, float partialTicks, @Nonnull MatrixStack transform, @Nonnull VertexConsumerProvider renderer, int lightmapCoord, int overlayLight )
    {
        // Render from the origin monitor
        ClientMonitor originTerminal = monitor.getClientMonitor();

        if( originTerminal == null ) return;
        TileMonitor origin = originTerminal.getOrigin();
        BlockPos monitorPos = monitor.getPos();

        // Ensure each monitor terminal is rendered only once. We allow rendering a specific tile
        // multiple times in a single frame to ensure compatibility with shaders which may run a
        // pass multiple times.
        long renderFrame = FrameInfo.getRenderFrame();
        if( originTerminal.lastRenderFrame == renderFrame && !monitorPos.equals( originTerminal.lastRenderPos ) )
        {
            return;
        }

        originTerminal.lastRenderFrame = renderFrame;
        originTerminal.lastRenderPos = monitorPos;

        BlockPos originPos = origin.getPos();

        // Determine orientation
        Direction dir = origin.getDirection();
        Direction front = origin.getFront();
        float yaw = dir.asRotation();
        float pitch = DirectionUtil.toPitchAngle( front );

        // Setup initial transform
        transform.push();
        transform.translate(
            originPos.getX() - monitorPos.getX() + 0.5,
            originPos.getY() - monitorPos.getY() + 0.5,
            originPos.getZ() - monitorPos.getZ() + 0.5
        );

        transform.multiply( Vector3f.NEGATIVE_Y.getDegreesQuaternion( yaw ) );
        transform.multiply( Vector3f.POSITIVE_X.getDegreesQuaternion( pitch ) );
        transform.translate(
            -0.5 + TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN,
            origin.getHeight() - 0.5 - (TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN) + 0,
            0.5
        );
        double xSize = origin.getWidth() - 2.0 * (TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER);
        double ySize = origin.getHeight() - 2.0 * (TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER);

        // Draw the contents
        Terminal terminal = originTerminal.getTerminal();
        if( terminal != null )
        {
            boolean redraw = originTerminal.pollTerminalChanged();
            if( originTerminal.buffer == null )
            {
                originTerminal.createBuffer();
                redraw = true;
            }
            VertexBuffer vbo = originTerminal.buffer;

            // Draw a terminal
            double xScale = xSize / (terminal.getWidth() * FixedWidthFontRenderer.FONT_WIDTH);
            double yScale = ySize / (terminal.getHeight() * FixedWidthFontRenderer.FONT_HEIGHT);
            transform.push();
            transform.scale( (float) xScale, (float) -yScale, 1.0f );

            float xMargin = (float) (MARGIN / xScale);
            float yMargin = (float) (MARGIN / yScale);

            Matrix4f matrix = transform.peek().getModel();

            if( redraw )
            {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder builder = tessellator.getBuffer();
                builder.begin( FixedWidthFontRenderer.TYPE.getDrawMode(), FixedWidthFontRenderer.TYPE.getVertexFormat() );
                FixedWidthFontRenderer.drawTerminalWithoutCursor(
                    IDENTITY, builder, 0, 0,
                    terminal, !originTerminal.isColour(), yMargin, yMargin, xMargin, xMargin
                );

                builder.end();
                vbo.upload( builder );
            }

            // Sneaky hack here: we get a buffer now in order to flush existing ones and set up the appropriate
            // render state. I've no clue how well this'll work in future versions of Minecraft, but it does the trick
            // for now.
            VertexConsumer buffer = renderer.getBuffer( FixedWidthFontRenderer.TYPE );
            FixedWidthFontRenderer.TYPE.startDrawing();

            vbo.bind();
            FixedWidthFontRenderer.TYPE.getVertexFormat().startDrawing( 0L );
            vbo.draw( matrix, FixedWidthFontRenderer.TYPE.getDrawMode() );
            VertexBuffer.unbind();
            FixedWidthFontRenderer.TYPE.getVertexFormat().endDrawing();

            // We don't draw the cursor with the VBO, as it's dynamic and so we'll end up refreshing far more than is
            // reasonable.
            FixedWidthFontRenderer.drawCursor( matrix, buffer, 0, 0, terminal, !originTerminal.isColour() );

            transform.pop();
        }
        else
        {
            FixedWidthFontRenderer.drawEmptyTerminal(
                transform.peek().getModel(), renderer,
                -MARGIN, MARGIN,
                (float) (xSize + 2 * MARGIN), (float) -(ySize + MARGIN * 2)
            );
        }

        FixedWidthFontRenderer.drawBlocker(
            transform.peek().getModel(), renderer,
            (float) -TileMonitor.RENDER_MARGIN, (float) TileMonitor.RENDER_MARGIN,
            (float) (xSize + 2 * TileMonitor.RENDER_MARGIN), (float) -(ySize + TileMonitor.RENDER_MARGIN * 2)
        );

        transform.pop();
    }
}
