/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.*;
import dan200.computercraft.client.render.TileEntityMonitorRenderer;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.shared.common.ContainerHeldItem;
import dan200.computercraft.shared.computer.inventory.ContainerComputer;
import dan200.computercraft.shared.computer.inventory.ContainerViewComputer;
import dan200.computercraft.shared.peripheral.diskdrive.ContainerDiskDrive;
import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.peripheral.printer.ContainerPrinter;
import dan200.computercraft.shared.pocket.inventory.ContainerPocketComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.inventory.ContainerTurtle;
import net.minecraft.client.gui.screen.Screens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;

public final class ComputerCraftProxyClient
{
    public static void setupClient()
    {
        registerContainers();

        // While turtles themselves are not transparent, their upgrades may be.
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.turtleNormal, RenderLayer.getTranslucent() );
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.turtleAdvanced, RenderLayer.getTranslucent() );

        // Monitors' textures have transparent fronts and so count as cutouts.
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.monitorNormal, RenderLayer.getCutout() );
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.monitorAdvanced, RenderLayer.getCutout() );

        // Setup TESRs
        BlockEntityRendererRegistry.INSTANCE.register( TileMonitor.FACTORY_NORMAL, new TileEntityMonitorRenderer() );
        BlockEntityRendererRegistry.INSTANCE.register( TileMonitor.FACTORY_ADVANCED, new TileEntityMonitorRenderer() );
        BlockEntityRendererRegistry.INSTANCE.register( TileTurtle.FACTORY_NORMAL, new TileEntityTurtleRenderer() );
        BlockEntityRendererRegistry.INSTANCE.register( TileTurtle.FACTORY_ADVANCED, new TileEntityTurtleRenderer() );
        // TODO: BlockEntityRendererRegistry.INSTANCE.register( TileCable.FACTORY, new TileEntityCableRenderer() );
    }

    private static void registerContainers()
    {
        // My IDE doesn't think so, but we do actually need these generics.

        Screens.<ContainerComputer, GuiComputer<ContainerComputer>>register( ContainerComputer.TYPE, GuiComputer::create );
        Screens.<ContainerPocketComputer, GuiComputer<ContainerPocketComputer>>register( ContainerPocketComputer.TYPE, GuiComputer::createPocket );
        Screens.register( ContainerTurtle.TYPE, GuiTurtle::new );

        Screens.register( ContainerPrinter.TYPE, GuiPrinter::new );
        Screens.register( ContainerDiskDrive.TYPE, GuiDiskDrive::new );
        Screens.register( ContainerHeldItem.PRINTOUT_TYPE, GuiPrintout::new );

        Screens.<ContainerViewComputer, GuiComputer<ContainerViewComputer>>register( ContainerViewComputer.TYPE, GuiComputer::createView );
    }
}
