/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.network.client;

import dan200.computercraft.client.ClientTableFormatter;
import dan200.computercraft.shared.command.text.TableBuilder;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;

public class ChatTableClientMessage implements NetworkMessage
{
    private TableBuilder table;

    public ChatTableClientMessage( TableBuilder table )
    {
        if( table.getColumns() < 0 ) throw new IllegalStateException( "Cannot send an empty table" );
        this.table = table;
    }

    public ChatTableClientMessage()
    {
    }

    @Override
    public void toBytes( @Nonnull PacketByteBuf buf )
    {
        buf.writeVarInt( table.getId() );
        buf.writeVarInt( table.getColumns() );
        buf.writeBoolean( table.getHeaders() != null );
        if( table.getHeaders() != null )
        {
            for( Text header : table.getHeaders() ) buf.writeText( header );
        }

        buf.writeVarInt( table.getRows().size() );
        for( Text[] row : table.getRows() )
        {
            for( Text column : row ) buf.writeText( column );
        }

        buf.writeVarInt( table.getAdditional() );
    }

    @Override
    public void fromBytes( @Nonnull PacketByteBuf buf )
    {
        int id = buf.readVarInt();
        int columns = buf.readVarInt();
        TableBuilder table;
        if( buf.readBoolean() )
        {
            Text[] headers = new Text[columns];
            for( int i = 0; i < columns; i++ ) headers[i] = buf.readText();
            table = new TableBuilder( id, headers );
        }
        else
        {
            table = new TableBuilder( id );
        }

        int rows = buf.readVarInt();
        for( int i = 0; i < rows; i++ )
        {
            Text[] row = new Text[columns];
            for( int j = 0; j < columns; j++ ) row[j] = buf.readText();
            table.row( row );
        }

        table.setAdditional( buf.readVarInt() );
        this.table = table;
    }

    @Override
    @Environment( EnvType.CLIENT )
    public void handle( NetworkEvent.Context context )
    {
        ClientTableFormatter.INSTANCE.display( table );
    }
}
