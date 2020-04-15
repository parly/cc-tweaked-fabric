/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.computer.apis;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import dan200.computercraft.shared.util.NBTUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Property;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;


public class CommandAPI implements ILuaAPI
{
    private TileCommandComputer m_computer;

    public CommandAPI( TileCommandComputer computer )
    {
        m_computer = computer;
    }

    // ILuaAPI implementation

    @Override
    public String[] getNames()
    {
        return new String[] { "commands" };
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "exec",
            "execAsync",
            "list",
            "getBlockPosition",
            "getBlockInfos",
            "getBlockInfo",
        };
    }

    private static Object createOutput( String output )
    {
        return new Object[] { output };
    }

    private Object[] doCommand( String command )
    {
        MinecraftServer server = m_computer.getWorld().getServer();
        if( server == null || !server.areCommandBlocksEnabled() )
        {
            return new Object[] { false, createOutput( "Command blocks disabled by server" ) };
        }

        CommandManager commandManager = server.getCommandManager();
        TileCommandComputer.CommandReceiver receiver = m_computer.getReceiver();
        try
        {
            receiver.clearOutput();
            int result = commandManager.execute( m_computer.getSource(), command );
            return new Object[] { result > 0, receiver.copyOutput(), result };
        }
        catch( Throwable t )
        {
            if( ComputerCraft.logPeripheralErrors ) ComputerCraft.log.error( "Error running command.", t );
            return new Object[] { false, createOutput( "Java Exception Thrown: " + t ) };
        }
    }

    private static Object getBlockInfo( World world, BlockPos pos )
    {
        // Get the details of the block
        BlockState state = world.getBlockState( pos );
        Block block = state.getBlock();

        Map<Object, Object> table = new HashMap<>();
        table.put( "name", ForgeRegistries.BLOCKS.getKey( block ).toString() );

        Map<Object, Object> stateTable = new HashMap<>();
        for( Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries().entrySet() )
        {
            Property<?> property = entry.getKey();
            stateTable.put( property.getName(), getPropertyValue( property, entry.getValue() ) );
        }
        table.put( "state", stateTable );

        BlockEntity tile = world.getBlockEntity( pos );
        if( tile != null ) table.put( "nbt", NBTUtil.toLua( tile.toTag( new CompoundTag() ) ) );

        return table;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private static Object getPropertyValue( Property property, Comparable value )
    {
        if( value instanceof String || value instanceof Number || value instanceof Boolean ) return value;
        return property.name( value );
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
    {
        switch( method )
        {
            case 0: // exec
            {
                final String command = getString( arguments, 0 );
                return context.executeMainThreadTask( () -> doCommand( command ) );
            }
            case 1: // execAsync
            {
                final String command = getString( arguments, 0 );
                long taskID = context.issueMainThreadTask( () -> doCommand( command ) );
                return new Object[] { taskID };
            }
            case 2:
                // list
                return context.executeMainThreadTask( () ->
                {
                    MinecraftServer server = m_computer.getWorld().getServer();

                    if( server == null ) return new Object[] { Collections.emptyMap() };
                    CommandNode<ServerCommandSource> node = server.getCommandManager().getDispatcher().getRoot();
                    for( int j = 0; j < arguments.length; j++ )
                    {
                        String name = getString( arguments, j );
                        node = node.getChild( name );
                        if( !(node instanceof LiteralCommandNode) ) return new Object[] { Collections.emptyMap() };
                    }

                    List<String> result = new ArrayList<>();
                    for( CommandNode<?> child : node.getChildren() )
                    {
                        if( child instanceof LiteralCommandNode<?> ) result.add( child.getName() );
                    }
                    return new Object[] { result };
                } );
            case 3: // getBlockPosition
            {
                // This is probably safe to do on the Lua thread. Probably.
                BlockPos pos = m_computer.getPos();
                return new Object[] { pos.getX(), pos.getY(), pos.getZ() };
            }
            case 4:
            {
                // getBlockInfos
                final int minX = getInt( arguments, 0 );
                final int minY = getInt( arguments, 1 );
                final int minZ = getInt( arguments, 2 );
                final int maxX = getInt( arguments, 3 );
                final int maxY = getInt( arguments, 4 );
                final int maxZ = getInt( arguments, 5 );
                return context.executeMainThreadTask( () ->
                {
                    // Get the details of the block
                    World world = m_computer.getWorld();
                    BlockPos min = new BlockPos(
                        Math.min( minX, maxX ),
                        Math.min( minY, maxY ),
                        Math.min( minZ, maxZ )
                    );
                    BlockPos max = new BlockPos(
                        Math.max( minX, maxX ),
                        Math.max( minY, maxY ),
                        Math.max( minZ, maxZ )
                    );
                    if( !World.isValid( min ) || !World.isValid( max ) )
                    {
                        throw new LuaException( "Co-ordinates out of range" );
                    }

                    int blocks = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
                    if( blocks > 4096 ) throw new LuaException( "Too many blocks" );

                    List<Object> results = new ArrayList<>( blocks );
                    for( int y = min.getY(); y <= max.getY(); y++ )
                    {
                        for( int z = min.getZ(); z <= max.getZ(); z++ )
                        {
                            for( int x = min.getX(); x <= max.getX(); x++ )
                            {
                                BlockPos pos = new BlockPos( x, y, z );
                                results.add( getBlockInfo( world, pos ) );
                            }
                        }
                    }
                    return new Object[] { results };
                } );
            }
            case 5:
            {
                // getBlockInfo
                final int x = getInt( arguments, 0 );
                final int y = getInt( arguments, 1 );
                final int z = getInt( arguments, 2 );
                return context.executeMainThreadTask( () ->
                {
                    // Get the details of the block
                    World world = m_computer.getWorld();
                    BlockPos position = new BlockPos( x, y, z );
                    if( World.isValid( position ) )
                    {
                        return new Object[] { getBlockInfo( world, position ) };
                    }
                    else
                    {
                        throw new LuaException( "Co-ordinates out of range" );
                    }
                } );
            }
            default:
            {
                return null;
            }
        }
    }
}
