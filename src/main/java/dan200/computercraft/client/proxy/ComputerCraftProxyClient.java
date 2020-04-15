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
import net.fabricmc.api.EnvType;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber( modid = ComputerCraft.MOD_ID, value = EnvType.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD )
public final class ComputerCraftProxyClient
{
    @SubscribeEvent
    public static void setupClient( FMLClientSetupEvent event )
    {
        registerContainers();

        // While turtles themselves are not transparent, their upgrades may be.
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.turtleNormal, RenderLayer.getTranslucent() );
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.turtleAdvanced, RenderLayer.getTranslucent() );

        // Monitors' textures have transparent fronts and so count as cutouts.
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.monitorNormal, RenderLayer.getCutout() );
        RenderLayers.setRenderLayer( ComputerCraft.Blocks.monitorAdvanced, RenderLayer.getCutout() );

        // Setup TESRs
        ClientRegistry.bindTileEntityRenderer( TileMonitor.FACTORY_NORMAL, TileEntityMonitorRenderer::new );
        ClientRegistry.bindTileEntityRenderer( TileMonitor.FACTORY_ADVANCED, TileEntityMonitorRenderer::new );
        ClientRegistry.bindTileEntityRenderer( TileTurtle.FACTORY_NORMAL, TileEntityTurtleRenderer::new );
        ClientRegistry.bindTileEntityRenderer( TileTurtle.FACTORY_ADVANCED, TileEntityTurtleRenderer::new );
        // TODO: ClientRegistry.bindTileEntityRenderer( TileCable.FACTORY, x -> new TileEntityCableRenderer() );
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

    @Mod.EventBusSubscriber( modid = ComputerCraft.MOD_ID, value = EnvType.CLIENT )
    public static final class ForgeHandlers
    {
        @SubscribeEvent
        public static void onWorldUnload( WorldEvent.Unload event )
        {
            if( event.getWorld().isClient() )
            {
                ClientMonitor.destroyAll();
            }
        }
    }
}
