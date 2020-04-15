/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft;

import com.google.common.collect.MapMaker;
import dan200.computercraft.api.ComputerCraftAPI.IComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.api.network.IPacketNetwork;
import dan200.computercraft.api.network.wired.IWiredElement;
import dan200.computercraft.api.network.wired.IWiredNode;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.core.apis.ApiFactories;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.ResourceMount;
import dan200.computercraft.shared.*;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessNetwork;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.wired.CapabilityWiredElement;
import dan200.computercraft.shared.wired.WiredNode;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

public final class ComputerCraftAPIImpl implements IComputerCraftAPI
{
    public static final ComputerCraftAPIImpl INSTANCE = new ComputerCraftAPIImpl();

    private ComputerCraftAPIImpl()
    {
    }

    private WeakReference<ReloadableResourceManager> currentResources;
    private final Map<Identifier, ResourceMount> mountCache = new MapMaker().weakValues().concurrencyLevel( 1 ).makeMap();

    @Nonnull
    @Override
    public String getInstalledVersion()
    {
        return "${version}";
    }

    @Override
    public int createUniqueNumberedSaveDir( @Nonnull World world, @Nonnull String parentSubPath )
    {
        return IDAssigner.getNextId( parentSubPath );
    }

    @Override
    public IWritableMount createSaveDirMount( @Nonnull World world, @Nonnull String subPath, long capacity )
    {
        try
        {
            return new FileMount( new File( IDAssigner.getDir(), subPath ), capacity );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    @Override
    public IMount createResourceMount( @Nonnull String domain, @Nonnull String subPath )
    {
        ReloadableResourceManager manager = ServerLifecycleHooks.getCurrentServer().getDataManager();
        ResourceMount mount = ResourceMount.get( domain, subPath, manager );
        return mount.exists( "" ) ? mount : null;
    }

    @Override
    public void registerPeripheralProvider( @Nonnull IPeripheralProvider provider )
    {
        Peripherals.register( provider );
    }

    @Override
    public void registerTurtleUpgrade( @Nonnull ITurtleUpgrade upgrade )
    {
        TurtleUpgrades.register( upgrade );
    }

    @Override
    public void registerBundledRedstoneProvider( @Nonnull IBundledRedstoneProvider provider )
    {
        BundledRedstone.register( provider );
    }

    @Override
    public int getBundledRedstoneOutput( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Direction side )
    {
        return BundledRedstone.getDefaultOutput( world, pos, side );
    }

    @Override
    public void registerMediaProvider( @Nonnull IMediaProvider provider )
    {
        MediaProviders.register( provider );
    }

    @Override
    public void registerPocketUpgrade( @Nonnull IPocketUpgrade upgrade )
    {
        PocketUpgrades.register( upgrade );
    }

    @Nonnull
    @Override
    public IPacketNetwork getWirelessNetwork()
    {
        return WirelessNetwork.getUniversal();
    }

    @Override
    public void registerAPIFactory( @Nonnull ILuaAPIFactory factory )
    {
        ApiFactories.register( factory );
    }

    @Nonnull
    @Override
    public IWiredNode createWiredNodeForElement( @Nonnull IWiredElement element )
    {
        return new WiredNode( element );
    }

    @Nonnull
    @Override
    public LazyOptional<IWiredElement> getWiredElementAt( @Nonnull BlockView world, @Nonnull BlockPos pos, @Nonnull Direction side )
    {
        BlockEntity tile = world.getBlockEntity( pos );
        if( tile instanceof TileCable )
        {
            return ((TileCable) tile).getElement( side );
        }
        else if( tile instanceof TileWiredModemFull )
        {
            return ((TileWiredModemFull) tile).getElement();
        }
        return null;
    }
}
