/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.command.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;


/**
 * An alternative to {@link LiteralArgumentBuilder} which also provides a {@code /... help} command, and defaults
 * to that command when no arguments are given.
 */
public final class HelpingArgumentBuilder extends LiteralArgumentBuilder<ServerCommandSource>
{
    private final Collection<HelpingArgumentBuilder> children = new ArrayList<>();

    private HelpingArgumentBuilder( String literal )
    {
        super( literal );
    }

    public static HelpingArgumentBuilder choice( String literal )
    {
        return new HelpingArgumentBuilder( literal );
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> executes( final Command<ServerCommandSource> command )
    {
        throw new IllegalStateException( "Cannot use executes on a HelpingArgumentBuilder" );
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> then( final ArgumentBuilder<ServerCommandSource, ?> argument )
    {
        if( getRedirect() != null ) throw new IllegalStateException( "Cannot add children to a redirected node" );

        if( argument instanceof HelpingArgumentBuilder )
        {
            children.add( (HelpingArgumentBuilder) argument );
        }
        else if( argument instanceof LiteralArgumentBuilder )
        {
            super.then( argument );
        }
        else
        {
            throw new IllegalStateException( "HelpingArgumentBuilder can only accept literal children" );
        }

        return this;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> then( CommandNode<ServerCommandSource> argument )
    {
        if( !(argument instanceof LiteralCommandNode) )
        {
            throw new IllegalStateException( "HelpingArgumentBuilder can only accept literal children" );
        }
        return super.then( argument );
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> build()
    {
        return buildImpl( getLiteral().replace( '-', '_' ), getLiteral() );
    }

    private LiteralCommandNode<ServerCommandSource> build( @Nonnull String id, @Nonnull String command )
    {
        return buildImpl( id + "." + getLiteral().replace( '-', '_' ), command + " " + getLiteral() );
    }

    private LiteralCommandNode<ServerCommandSource> buildImpl( String id, String command )
    {
        HelpCommand helpCommand = new HelpCommand( id, command );
        LiteralCommandNode<ServerCommandSource> node = new LiteralCommandNode<>( getLiteral(), helpCommand, getRequirement(), getRedirect(), getRedirectModifier(), isFork() );
        helpCommand.node = node;

        // Set up a /... help command
        LiteralArgumentBuilder<ServerCommandSource> helpNode = LiteralArgumentBuilder.<ServerCommandSource>literal( "help" )
            .requires( x -> getArguments().stream().anyMatch( y -> y.getRequirement().test( x ) ) )
            .executes( helpCommand );

        // Add all normal command children to this and the help node
        for( CommandNode<ServerCommandSource> child : getArguments() )
        {
            node.addChild( child );

            helpNode.then( LiteralArgumentBuilder.<ServerCommandSource>literal( child.getName() )
                .requires( child.getRequirement() )
                .executes( helpForChild( child, id, command ) )
                .build()
            );
        }

        // And add alternative versions of which forward instead
        for( HelpingArgumentBuilder childBuilder : children )
        {
            LiteralCommandNode<ServerCommandSource> child = childBuilder.build( id, command );
            node.addChild( child );
            helpNode.then( LiteralArgumentBuilder.<ServerCommandSource>literal( child.getName() )
                .requires( child.getRequirement() )
                .executes( helpForChild( child, id, command ) )
                .redirect( child.getChild( "help" ) )
                .build()
            );
        }

        node.addChild( helpNode.build() );

        return node;
    }

    private static final Formatting HEADER = Formatting.LIGHT_PURPLE;
    private static final Formatting SYNOPSIS = Formatting.AQUA;
    private static final Formatting NAME = Formatting.GREEN;

    private static final class HelpCommand implements Command<ServerCommandSource>
    {
        private final String id;
        private final String command;
        LiteralCommandNode<ServerCommandSource> node;

        private HelpCommand( String id, String command )
        {
            this.id = id;
            this.command = command;
        }

        @Override
        public int run( CommandContext<ServerCommandSource> context )
        {
            context.getSource().sendFeedback( getHelp( context, node, id, command ), false );
            return 0;
        }
    }

    private static Command<ServerCommandSource> helpForChild( CommandNode<ServerCommandSource> node, String id, String command )
    {
        return context -> {
            context.getSource().sendFeedback( getHelp( context, node, id + "." + node.getName().replace( '-', '_' ), command + " " + node.getName() ), false );
            return 0;
        };
    }

    private static Text getHelp( CommandContext<ServerCommandSource> context, CommandNode<ServerCommandSource> node, String id, String command )
    {
        // An ugly hack to extract usage information from the dispatcher. We generate a temporary node, generate
        // the shorthand usage, and emit that.
        CommandDispatcher<ServerCommandSource> dispatcher = context.getSource().getMinecraftServer().getCommandManager().getDispatcher();
        CommandNode<ServerCommandSource> temp = new LiteralCommandNode<>( "_", null, x -> true, null, null, false );
        temp.addChild( node );
        String usage = dispatcher.getSmartUsage( temp, context.getSource() ).get( node ).substring( node.getName().length() );

        Text output = new LiteralText( "" )
            .append( coloured( "/" + command + usage, HEADER ) )
            .append( " " )
            .append( coloured( translate( "commands." + id + ".synopsis" ), SYNOPSIS ) )
            .append( "\n" )
            .append( translate( "commands." + id + ".desc" ) );

        for( CommandNode<ServerCommandSource> child : node.getChildren() )
        {
            if( !child.getRequirement().test( context.getSource() ) || !(child instanceof LiteralCommandNode) )
            {
                continue;
            }

            output.append( "\n" );

            Text component = coloured( child.getName(), NAME );
            component.getStyle().setClickEvent( new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND,
                "/" + command + " " + child.getName()
            ) );
            output.append( component );

            output.append( " - " ).append( translate( "commands." + id + "." + child.getName() + ".synopsis" ) );
        }

        return output;
    }
}
