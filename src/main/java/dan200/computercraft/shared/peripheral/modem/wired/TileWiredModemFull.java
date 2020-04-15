/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.modem.wired;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.wired.IWiredElement;
import dan200.computercraft.api.network.wired.IWiredNode;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import dan200.computercraft.shared.command.CommandCopy;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.NamedTileEntityType;
import dan200.computercraft.shared.util.TickScheduler;
import dan200.computercraft.shared.wired.CapabilityWiredElement;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;


public class TileWiredModemFull extends TileGeneric implements IPeripheralTile
{
    public static final NamedTileEntityType<TileWiredModemFull> FACTORY = NamedTileEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "wired_modem_full" ),
        TileWiredModemFull::new
    );

    private static final String NBT_PERIPHERAL_ENABLED = "PeripheralAccess";

    private static final class FullElement extends WiredModemElement
    {
        private final TileWiredModemFull m_entity;

        private FullElement( TileWiredModemFull entity )
        {
            m_entity = entity;
        }

        @Override
        protected void attachPeripheral( String name, IPeripheral peripheral )
        {
            for( int i = 0; i < 6; i++ )
            {
                WiredModemPeripheral modem = m_entity.m_modems[i];
                if( modem != null ) modem.attachPeripheral( name, peripheral );
            }
        }

        @Override
        protected void detachPeripheral( String name )
        {
            for( int i = 0; i < 6; i++ )
            {
                WiredModemPeripheral modem = m_entity.m_modems[i];
                if( modem != null ) modem.detachPeripheral( name );
            }
        }

        @Nonnull
        @Override
        public World getWorld()
        {
            return m_entity.getWorld();
        }

        @Nonnull
        @Override
        public Vec3d getPosition()
        {
            BlockPos pos = m_entity.getPos();
            return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
        }
    }

    private WiredModemPeripheral[] m_modems = new WiredModemPeripheral[6];

    private boolean m_peripheralAccessAllowed = false;
    private WiredModemLocalPeripheral[] m_peripherals = new WiredModemLocalPeripheral[6];

    private boolean m_destroyed = false;
    private boolean m_connectionsFormed = false;

    private final ModemState m_modemState = new ModemState( () -> TickScheduler.schedule( this ) );
    private final WiredModemElement m_element = new FullElement( this );
    private LazyOptional<IWiredElement> elementCap;
    private final IWiredNode m_node = m_element.getNode();

    private final NonNullConsumer<LazyOptional<IWiredElement>> connectedNodeChanged = x -> connectionsChanged();

    public TileWiredModemFull()
    {
        super( FACTORY );
        for( int i = 0; i < m_peripherals.length; i++ ) m_peripherals[i] = new WiredModemLocalPeripheral();
    }

    private void doRemove()
    {
        if( world == null || !world.isClient )
        {
            m_node.remove();
            m_connectionsFormed = false;
        }
    }

    @Override
    public void destroy()
    {
        if( !m_destroyed )
        {
            m_destroyed = true;
            doRemove();
        }
        super.destroy();
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        doRemove();
    }

    @Override
    protected void invalidateCaps()
    {
        super.invalidateCaps();
        if( elementCap != null )
        {
            elementCap.invalidate();
            elementCap = null;
        }
    }

    @Override
    public void markRemoved()
    {
        super.markRemoved();
        doRemove();
    }

    @Override
    public void onNeighbourChange( @Nonnull BlockPos neighbour )
    {
        onNeighbourTileEntityChange( neighbour );
    }

    @Override
    public void onNeighbourTileEntityChange( @Nonnull BlockPos neighbour )
    {
        if( !world.isClient && m_peripheralAccessAllowed )
        {
            for( Direction facing : DirectionUtil.FACINGS )
            {
                if( getPos().offset( facing ).equals( neighbour ) )
                {
                    WiredModemLocalPeripheral peripheral = m_peripherals[facing.ordinal()];
                    if( peripheral.attach( world, getPos(), facing ) ) updateConnectedPeripherals();
                }
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult onActivate( PlayerEntity player, Hand hand, BlockHitResult hit )
    {
        if( getWorld().isClient ) return ActionResult.SUCCESS;

        // On server, we interacted if a peripheral was found
        Set<String> oldPeriphNames = getConnectedPeripheralNames();
        togglePeripheralAccess();
        Set<String> periphNames = getConnectedPeripheralNames();

        if( !Objects.equal( periphNames, oldPeriphNames ) )
        {
            sendPeripheralChanges( player, "chat.computercraft.wired_modem.peripheral_disconnected", oldPeriphNames );
            sendPeripheralChanges( player, "chat.computercraft.wired_modem.peripheral_connected", periphNames );
        }

        return ActionResult.SUCCESS;
    }

    private static void sendPeripheralChanges( PlayerEntity player, String kind, Collection<String> peripherals )
    {
        if( peripherals.isEmpty() ) return;

        List<String> names = new ArrayList<>( peripherals );
        names.sort( Comparator.naturalOrder() );

        LiteralText base = new LiteralText( "" );
        for( int i = 0; i < names.size(); i++ )
        {
            if( i > 0 ) base.append( ", " );
            base.append( CommandCopy.createCopyText( names.get( i ) ) );
        }

        player.addChatMessage( new TranslatableText( kind, base ), false );
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );
        m_peripheralAccessAllowed = nbt.getBoolean( NBT_PERIPHERAL_ENABLED );
        for( int i = 0; i < m_peripherals.length; i++ ) m_peripherals[i].read( nbt, Integer.toString( i ) );
    }

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        nbt.putBoolean( NBT_PERIPHERAL_ENABLED, m_peripheralAccessAllowed );
        for( int i = 0; i < m_peripherals.length; i++ ) m_peripherals[i].write( nbt, Integer.toString( i ) );
        return super.toTag( nbt );
    }

    private void updateBlockState()
    {
        BlockState state = getCachedState();
        boolean modemOn = m_modemState.isOpen(), peripheralOn = m_peripheralAccessAllowed;
        if( state.get( MODEM_ON ) == modemOn && state.get( PERIPHERAL_ON ) == peripheralOn ) return;

        getWorld().setBlockState( getPos(), state.with( MODEM_ON, modemOn ).with( PERIPHERAL_ON, peripheralOn ) );
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        TickScheduler.schedule( this );
    }

    @Override
    public void blockTick()
    {
        if( getWorld().isClient ) return;

        if( m_modemState.pollChanged() ) updateBlockState();

        if( !m_connectionsFormed )
        {
            m_connectionsFormed = true;

            connectionsChanged();
            if( m_peripheralAccessAllowed )
            {
                for( Direction facing : DirectionUtil.FACINGS )
                {
                    m_peripherals[facing.ordinal()].attach( world, getPos(), facing );
                }
                updateConnectedPeripherals();
            }
        }
    }

    private void connectionsChanged()
    {
        if( getWorld().isClient ) return;

        World world = getWorld();
        BlockPos current = getPos();
        for( Direction facing : DirectionUtil.FACINGS )
        {
            BlockPos offset = current.offset( facing );
            if( !world.isAreaLoaded( offset, 0 ) ) continue;

            LazyOptional<IWiredElement> element = ComputerCraftAPI.getWiredElementAt( world, offset, facing.getOpposite() );
            if( !element.isPresent() ) continue;

            element.addListener( connectedNodeChanged );
            m_node.connectTo( element.orElseThrow( NullPointerException::new ).getNode() );
        }
    }

    private void togglePeripheralAccess()
    {
        if( !m_peripheralAccessAllowed )
        {
            boolean hasAny = false;
            for( Direction facing : DirectionUtil.FACINGS )
            {
                WiredModemLocalPeripheral peripheral = m_peripherals[facing.ordinal()];
                peripheral.attach( world, getPos(), facing );
                hasAny |= peripheral.hasPeripheral();
            }

            if( !hasAny ) return;

            m_peripheralAccessAllowed = true;
            m_node.updatePeripherals( getConnectedPeripherals() );
        }
        else
        {
            m_peripheralAccessAllowed = false;

            for( WiredModemLocalPeripheral peripheral : m_peripherals ) peripheral.detach();
            m_node.updatePeripherals( Collections.emptyMap() );
        }

        updateBlockState();
    }

    private Set<String> getConnectedPeripheralNames()
    {
        if( !m_peripheralAccessAllowed ) return Collections.emptySet();

        Set<String> peripherals = new HashSet<>( 6 );
        for( WiredModemLocalPeripheral peripheral : m_peripherals )
        {
            String name = peripheral.getConnectedName();
            if( name != null ) peripherals.add( name );
        }
        return peripherals;
    }

    private Map<String, IPeripheral> getConnectedPeripherals()
    {
        if( !m_peripheralAccessAllowed ) return Collections.emptyMap();

        Map<String, IPeripheral> peripherals = new HashMap<>( 6 );
        for( WiredModemLocalPeripheral peripheral : m_peripherals ) peripheral.extendMap( peripherals );
        return peripherals;
    }

    private void updateConnectedPeripherals()
    {
        Map<String, IPeripheral> peripherals = getConnectedPeripherals();
        if( peripherals.isEmpty() )
        {
            // If there are no peripherals then disable access and update the display state.
            m_peripheralAccessAllowed = false;
            updateBlockState();
        }

        m_node.updatePeripherals( peripherals );
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability( @Nonnull Capability<T> capability, @Nullable Direction facing )
    {
        if( capability == CapabilityWiredElement.CAPABILITY )
        {
            if( elementCap == null ) elementCap = LazyOptional.of( () -> m_element );
            return elementCap.cast();
        }
        return super.getCapability( capability, facing );
    }

    public IWiredElement getElement()
    {
        return m_element;
    }

    // IPeripheralTile

    @Override
    public IPeripheral getPeripheral( @Nonnull Direction side )
    {
        if( m_destroyed ) return null;

        WiredModemPeripheral peripheral = m_modems[side.ordinal()];
        if( peripheral == null )
        {
            WiredModemLocalPeripheral localPeripheral = m_peripherals[side.ordinal()];
            peripheral = m_modems[side.ordinal()] = new WiredModemPeripheral( m_modemState, m_element )
            {
                @Nonnull
                @Override
                protected WiredModemLocalPeripheral getLocalPeripheral()
                {
                    return localPeripheral;
                }

                @Nonnull
                @Override
                public Vec3d getPosition()
                {
                    BlockPos pos = getPos().offset( side );
                    return new Vec3d( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 );
                }
            };
        }
        return peripheral;
    }
}
