/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dan200.computercraft.ComputerCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.fabricmc.api.EnvType;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber( modid = ComputerCraft.MOD_ID, value = EnvType.CLIENT )
public final class CommandCopy
{
    private static final String PREFIX = "/computercraft copy ";

    private CommandCopy()
    {
    }

    public static void register( CommandDispatcher<ServerCommandSource> registry )
    {
        registry.register( literal( "computercraft" )
            .then( literal( "copy" ) )
            .then( argument( "message", StringArgumentType.greedyString() ) )
            .executes( context -> {
                MinecraftClient.getInstance().keyboard.setClipboard( context.getArgument( "message", String.class ) );
                return 1;
            } )
        );
    }

    @SubscribeEvent
    public static void onClientSendMessage( ClientChatEvent event )
    {
        // Emulate the command on the client side
        if( event.getMessage().startsWith( PREFIX ) )
        {
            MinecraftClient.getInstance().keyboard.setClipboard( event.getMessage().substring( PREFIX.length() ) );
            event.setCanceled( true );
        }
    }

    public static Text createCopyText( String text )
    {
        LiteralText name = new LiteralText( text );
        name.getStyle()
            .setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, PREFIX + text ) )
            .setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new TranslatableText( "gui.computercraft.tooltip.copy" ) ) );
        return name;
    }
}
