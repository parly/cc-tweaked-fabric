/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.core.apis.http.websocket;

import dan200.computercraft.core.apis.http.HTTPRequestException;
import dan200.computercraft.core.apis.http.NetworkUtils;
import dan200.computercraft.core.tracking.TrackingField;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;


public class WebsocketHandler extends SimpleChannelInboundHandler<Object>
{
    private final Websocket websocket;
    private final WebSocketClientHandshaker handshaker;

    public WebsocketHandler( Websocket websocket, WebSocketClientHandshaker handshaker )
    {
        this.handshaker = handshaker;
        this.websocket = websocket;
    }

    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception
    {
        handshaker.handshake( ctx.channel() );
        super.channelActive( ctx );
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception
    {
        websocket.close( -1, "Websocket is inactive" );
        super.channelInactive( ctx );
    }

    @Override
    public void channelRead0( ChannelHandlerContext ctx, Object msg )
    {
        if( websocket.isClosed() ) return;

        if( !handshaker.isHandshakeComplete() )
        {
            handshaker.finishHandshake( ctx.channel(), (FullHttpResponse) msg );
            websocket.success( ctx.channel() );
            return;
        }

        if( msg instanceof FullHttpResponse )
        {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException( "Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString( CharsetUtil.UTF_8 ) + ')' );
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if( frame instanceof TextWebSocketFrame )
        {
            String data = ((TextWebSocketFrame) frame).text();

            websocket.environment().addTrackingChange( TrackingField.WEBSOCKET_INCOMING, data.length() );
            websocket.environment().queueEvent( MESSAGE_EVENT, new Object[] { websocket.address(), data, false } );
        }
        else if( frame instanceof BinaryWebSocketFrame )
        {
            byte[] converted = NetworkUtils.toBytes( frame.content() );

            websocket.environment().addTrackingChange( TrackingField.WEBSOCKET_INCOMING, converted.length );
            websocket.environment().queueEvent( MESSAGE_EVENT, new Object[] { websocket.address(), converted, true } );
        }
        else if( frame instanceof CloseWebSocketFrame )
        {
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            websocket.close( closeFrame.statusCode(), closeFrame.reasonText() );
        }
        else if( frame instanceof PingWebSocketFrame )
        {
            frame.content().retain();
            ctx.channel().writeAndFlush( new PongWebSocketFrame( frame.content() ) );
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause )
    {
        ctx.close();

        String message;
        if( cause instanceof WebSocketHandshakeException || cause instanceof HTTPRequestException )
        {
            message = cause.getMessage();
        }
        else if( cause instanceof TooLongFrameException )
        {
            message = "Message is too large";
        }
        else if( cause instanceof ReadTimeoutException || cause instanceof ConnectTimeoutException )
        {
            message = "Timed out";
        }
        else
        {
            message = "Could not connect";
        }

        if( handshaker.isHandshakeComplete() )
        {
            websocket.close( -1, message );
        }
        else
        {
            websocket.failure( message );
        }
    }
}
