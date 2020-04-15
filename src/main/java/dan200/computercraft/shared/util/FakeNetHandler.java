/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ConfirmGuiActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.GuiCloseC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectVillagerTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class FakeNetHandler extends ServerPlayNetworkHandler
{
    public FakeNetHandler( @Nonnull FakePlayer player )
    {
        super( player.getServerWorld().getServer(), new FakeNetworkManager(), player );
    }

    @Override
    public void tick()
    {
    }

    @Override
    public void disconnect( @Nonnull Text reason )
    {
    }

    @Override
    public void onDisconnected( Text reason )
    {
    }

    @Override
    public void sendPacket( @Nonnull Packet<?> packet )
    {
    }

    @Override
    public void sendPacket( @Nonnull Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> whenSent )
    {
    }

    @Override
    public void onPlayerInput( PlayerInputC2SPacket packet )
    {
    }

    @Override
    public void onVehicleMove( VehicleMoveC2SPacket packet )
    {
    }

    @Override
    public void onTeleportConfirm( TeleportConfirmC2SPacket packet )
    {
    }

    @Override
    public void onRecipeBookData( RecipeBookDataC2SPacket packet )
    {
    }

    @Override
    public void onAdvancementTab( AdvancementTabC2SPacket packet )
    {
    }

    @Override
    public void onRequestCommandCompletions( RequestCommandCompletionsC2SPacket packet )
    {
    }

    @Override
    public void onUpdateCommandBlock( @Nonnull UpdateCommandBlockC2SPacket packet )
    {
    }

    @Override
    public void onUpdateCommandBlockMinecart( @Nonnull UpdateCommandBlockMinecartC2SPacket packet )
    {
    }

    @Override
    public void onPickFromInventory( PickFromInventoryC2SPacket packet )
    {
    }

    @Override
    public void onRenameItem( @Nonnull RenameItemC2SPacket packet )
    {
    }

    @Override
    public void onUpdateBeacon( @Nonnull UpdateBeaconC2SPacket packet )
    {
    }

    @Override
    public void onStructureBlockUpdate( @Nonnull UpdateStructureBlockC2SPacket packet )
    {
    }

    @Override
    public void onJigsawUpdate( @Nonnull UpdateJigsawC2SPacket packet )
    {
    }

    @Override
    public void onVillagerTradeSelect( SelectVillagerTradeC2SPacket packet )
    {
    }

    @Override
    public void onBookUpdate( BookUpdateC2SPacket packet )
    {
    }

    @Override
    public void onQueryEntityNbt( @Nonnull QueryEntityNbtC2SPacket packet )
    {
    }

    @Override
    public void onQueryBlockNbt( @Nonnull QueryBlockNbtC2SPacket packet )
    {
    }

    @Override
    public void onPlayerMove( PlayerMoveC2SPacket packet )
    {
    }

    @Override
    public void onPlayerAction( PlayerActionC2SPacket packet )
    {
    }

    @Override
    public void onPlayerInteractBlock( PlayerInteractBlockC2SPacket packet )
    {
    }

    @Override
    public void onPlayerInteractItem( PlayerInteractItemC2SPacket packet )
    {
    }

    @Override
    public void onSpectatorTeleport( @Nonnull SpectatorTeleportC2SPacket packet )
    {
    }

    @Override
    public void onResourcePackStatus( ResourcePackStatusC2SPacket packet )
    {
    }

    @Override
    public void onBoatPaddleState( @Nonnull BoatPaddleStateC2SPacket packet )
    {
    }

    @Override
    public void onUpdateSelectedSlot( UpdateSelectedSlotC2SPacket packet )
    {
    }

    @Override
    public void onChatMessage( @Nonnull ChatMessageC2SPacket packet )
    {
    }

    @Override
    public void onHandSwing( HandSwingC2SPacket packet )
    {
    }

    @Override
    public void onClientCommand( ClientCommandC2SPacket packet )
    {
    }

    @Override
    public void onPlayerInteractEntity( PlayerInteractEntityC2SPacket packet )
    {
    }

    @Override
    public void onClientStatus( ClientStatusC2SPacket packet )
    {
    }

    @Override
    public void onGuiClose( @Nonnull GuiCloseC2SPacket packet )
    {
    }

    @Override
    public void onClickWindow( ClickWindowC2SPacket packet )
    {
    }

    @Override
    public void onCraftRequest( @Nonnull CraftRequestC2SPacket packet )
    {
    }

    @Override
    public void onButtonClick( ButtonClickC2SPacket packet )
    {
    }

    @Override
    public void onCreativeInventoryAction( @Nonnull CreativeInventoryActionC2SPacket packet )
    {
    }

    @Override
    public void onConfirmTransaction( ConfirmGuiActionC2SPacket packet )
    {
    }

    @Override
    public void onSignUpdate( UpdateSignC2SPacket packet )
    {
    }

    @Override
    public void onKeepAlive( @Nonnull KeepAliveC2SPacket packet )
    {
    }

    @Override
    public void onPlayerAbilities( UpdatePlayerAbilitiesC2SPacket packet )
    {
    }

    @Override
    public void onClientSettings( @Nonnull ClientSettingsC2SPacket packet )
    {
    }

    @Override
    public void onCustomPayload( CustomPayloadC2SPacket packet )
    {
    }

    @Override
    public void onUpdateDifficulty( @Nonnull UpdateDifficultyC2SPacket packet )
    {
    }

    @Override
    public void onUpdateDifficultyLock( @Nonnull UpdateDifficultyLockC2SPacket packet )
    {
    }

    private static class FakeNetworkManager extends ClientConnection
    {
        private PacketListener handler;
        private Text closeReason;

        FakeNetworkManager()
        {
            super( NetworkSide.CLIENTBOUND );
        }

        @Override
        public void channelActive( ChannelHandlerContext context )
        {
        }

        @Override
        public void setState( @Nonnull NetworkState state )
        {
        }

        @Override
        public void channelInactive( ChannelHandlerContext context )
        {
        }

        @Override
        public void exceptionCaught( ChannelHandlerContext context, @Nonnull Throwable err )
        {
        }

        @Override
        protected void channelRead0( ChannelHandlerContext context, @Nonnull Packet<?> packet )
        {
        }

        @Override
        public void setPacketListener( PacketListener handler )
        {
            this.handler = handler;
        }

        @Override
        public void send( @Nonnull Packet<?> packet )
        {
        }

        @Override
        public void send( @Nonnull Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> whenSent )
        {
        }

        @Override
        public void tick()
        {
        }

        @Override
        public void disconnect( @Nonnull Text message )
        {
            this.closeReason = message;
        }

        @Override
        public void setupEncryption( SecretKey key )
        {
        }

        @Nonnull
        @Override
        public PacketListener getPacketListener()
        {
            return handler;
        }

        @Nullable
        @Override
        public Text getDisconnectReason()
        {
            return closeReason;
        }

        @Override
        public void disableAutoRead()
        {
        }

        @Override
        public void setCompressionThreshold( int threshold )
        {
        }
    }
}
