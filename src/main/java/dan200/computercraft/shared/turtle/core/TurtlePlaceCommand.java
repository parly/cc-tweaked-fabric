/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.event.TurtleBlockEvent;
import dan200.computercraft.shared.TurtlePermissions;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.DropConsumer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public class TurtlePlaceCommand implements ITurtleCommand
{
    private final InteractDirection m_direction;
    private final Object[] m_extraArguments;

    public TurtlePlaceCommand( InteractDirection direction, Object[] arguments )
    {
        m_direction = direction;
        m_extraArguments = arguments;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Get thing to place
        ItemStack stack = turtle.getInventory().getInvStack( turtle.getSelectedSlot() );
        if( stack.isEmpty() )
        {
            return TurtleCommandResult.failure( "No items to place" );
        }

        // Remember old block
        Direction direction = m_direction.toWorldDir( turtle );
        BlockPos coordinates = turtle.getPosition().offset( direction );

        // Create a fake player, and orient it appropriately
        BlockPos playerPosition = turtle.getPosition().offset( direction );
        TurtlePlayer turtlePlayer = createPlayer( turtle, playerPosition, direction );

        TurtleBlockEvent.Place place = new TurtleBlockEvent.Place( turtle, turtlePlayer, turtle.getWorld(), coordinates, stack );
        if( MinecraftForge.EVENT_BUS.post( place ) )
        {
            return TurtleCommandResult.failure( place.getFailureMessage() );
        }

        // Do the deploying
        String[] errorMessage = new String[1];
        ItemStack remainder = deploy( stack, turtle, turtlePlayer, direction, m_extraArguments, errorMessage );
        if( remainder != stack )
        {
            // Put the remaining items back
            turtle.getInventory().setInvStack( turtle.getSelectedSlot(), remainder );
            turtle.getInventory().markDirty();

            // Animate and return success
            turtle.playAnimation( TurtleAnimation.WAIT );
            return TurtleCommandResult.success();
        }
        else
        {
            if( errorMessage[0] != null )
            {
                return TurtleCommandResult.failure( errorMessage[0] );
            }
            else if( stack.getItem() instanceof BlockItem )
            {
                return TurtleCommandResult.failure( "Cannot place block here" );
            }
            else
            {
                return TurtleCommandResult.failure( "Cannot place item here" );
            }
        }
    }

    public static ItemStack deploy( @Nonnull ItemStack stack, ITurtleAccess turtle, Direction direction, Object[] extraArguments, String[] outErrorMessage )
    {
        // Create a fake player, and orient it appropriately
        BlockPos playerPosition = turtle.getPosition().offset( direction );
        TurtlePlayer turtlePlayer = createPlayer( turtle, playerPosition, direction );

        return deploy( stack, turtle, turtlePlayer, direction, extraArguments, outErrorMessage );
    }

    public static ItemStack deploy( @Nonnull ItemStack stack, ITurtleAccess turtle, TurtlePlayer turtlePlayer, Direction direction, Object[] extraArguments, String[] outErrorMessage )
    {
        // Deploy on an entity
        ItemStack remainder = deployOnEntity( stack, turtle, turtlePlayer, direction, extraArguments, outErrorMessage );
        if( remainder != stack )
        {
            return remainder;
        }

        // Deploy on the block immediately in front
        BlockPos position = turtle.getPosition();
        BlockPos newPosition = position.offset( direction );
        remainder = deployOnBlock( stack, turtle, turtlePlayer, newPosition, direction.getOpposite(), extraArguments, true, outErrorMessage );
        if( remainder != stack )
        {
            return remainder;
        }

        // Deploy on the block one block away
        remainder = deployOnBlock( stack, turtle, turtlePlayer, newPosition.offset( direction ), direction.getOpposite(), extraArguments, false, outErrorMessage );
        if( remainder != stack )
        {
            return remainder;
        }

        if( direction.getAxis() != Direction.Axis.Y )
        {
            // Deploy down on the block in front
            remainder = deployOnBlock( stack, turtle, turtlePlayer, newPosition.down(), Direction.UP, extraArguments, false, outErrorMessage );
            if( remainder != stack )
            {
                return remainder;
            }
        }

        // Deploy back onto the turtle
        remainder = deployOnBlock( stack, turtle, turtlePlayer, position, direction, extraArguments, false, outErrorMessage );
        if( remainder != stack )
        {
            return remainder;
        }

        // If nothing worked, return the original stack unchanged
        return stack;
    }

    public static TurtlePlayer createPlayer( ITurtleAccess turtle, BlockPos position, Direction direction )
    {
        TurtlePlayer turtlePlayer = TurtlePlayer.get( turtle );
        orientPlayer( turtle, turtlePlayer, position, direction );
        return turtlePlayer;
    }

    private static void orientPlayer( ITurtleAccess turtle, TurtlePlayer turtlePlayer, BlockPos position, Direction direction )
    {
        double posX = position.getX() + 0.5;
        double posY = position.getY() + 0.5;
        double posZ = position.getZ() + 0.5;

        // Stop intersection with the turtle itself
        if( turtle.getPosition().equals( position ) )
        {
            posX += 0.48 * direction.getOffsetX();
            posY += 0.48 * direction.getOffsetY();
            posZ += 0.48 * direction.getOffsetZ();
        }

        if( direction.getAxis() != Direction.Axis.Y )
        {
            turtlePlayer.yaw = direction.asRotation();
            turtlePlayer.pitch = 0.0f;
        }
        else
        {
            turtlePlayer.yaw = turtle.getDirection().asRotation();
            turtlePlayer.pitch = DirectionUtil.toPitchAngle( direction );
        }

        turtlePlayer.setPos( posX, posY, posZ );
        turtlePlayer.prevX = posX;
        turtlePlayer.prevY = posY;
        turtlePlayer.prevZ = posZ;
        turtlePlayer.prevPitch = turtlePlayer.pitch;
        turtlePlayer.prevYaw = turtlePlayer.yaw;

        turtlePlayer.headYaw = turtlePlayer.yaw;
        turtlePlayer.prevHeadYaw = turtlePlayer.headYaw;
    }

    @Nonnull
    private static ItemStack deployOnEntity( @Nonnull ItemStack stack, final ITurtleAccess turtle, TurtlePlayer turtlePlayer, Direction direction, Object[] extraArguments, String[] outErrorMessage )
    {
        // See if there is an entity present
        final World world = turtle.getWorld();
        final BlockPos position = turtle.getPosition();
        Vec3d turtlePos = turtlePlayer.getPos();
        Vec3d rayDir = turtlePlayer.getRotationVec( 1.0f );
        Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities( world, turtlePos, rayDir, 1.5 );
        if( hit == null )
        {
            return stack;
        }

        // Load up the turtle's inventory
        ItemStack stackCopy = stack.copy();
        turtlePlayer.loadInventory( stackCopy );

        // Start claiming entity drops
        Entity hitEntity = hit.getKey();
        Vec3d hitPos = hit.getValue();
        DropConsumer.set(
            hitEntity,
            drop -> InventoryUtil.storeItems( drop, turtle.getItemHandler(), turtle.getSelectedSlot() )
        );

        // Place on the entity
        boolean placed = false;
        ActionResult cancelResult = ForgeHooks.onInteractEntityAt( turtlePlayer, hitEntity, hitPos, Hand.MAIN_HAND );
        if( cancelResult == null )
        {
            cancelResult = hitEntity.interactAt( turtlePlayer, hitPos, Hand.MAIN_HAND );
        }

        if( cancelResult == ActionResult.SUCCESS )
        {
            placed = true;
        }
        else
        {
            // See EntityPlayer.interactOn
            cancelResult = ForgeHooks.onInteractEntity( turtlePlayer, hitEntity, Hand.MAIN_HAND );
            if( cancelResult == ActionResult.SUCCESS )
            {
                placed = true;
            }
            else if( cancelResult == null )
            {
                if( hitEntity.interact( turtlePlayer, Hand.MAIN_HAND ) )
                {
                    placed = true;
                }
                else if( hitEntity instanceof LivingEntity )
                {
                    placed = stackCopy.useOnEntity( turtlePlayer, (LivingEntity) hitEntity, Hand.MAIN_HAND );
                    if( placed ) turtlePlayer.loadInventory( stackCopy );
                }
            }
        }

        // Stop claiming drops
        List<ItemStack> remainingDrops = DropConsumer.clear();
        for( ItemStack remaining : remainingDrops )
        {
            WorldUtil.dropItemStack( remaining, world, position, turtle.getDirection().getOpposite() );
        }

        // Put everything we collected into the turtles inventory, then return
        ItemStack remainder = turtlePlayer.unloadInventory( turtle );
        if( !placed && ItemStack.areEqualIgnoreDamage( stack, remainder ) )
        {
            return stack;
        }
        else if( !remainder.isEmpty() )
        {
            return remainder;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    private static boolean canDeployOnBlock( @Nonnull ItemPlacementContext context, ITurtleAccess turtle, TurtlePlayer player, BlockPos position, Direction side, boolean allowReplaceable, String[] outErrorMessage )
    {
        World world = turtle.getWorld();
        if( !World.isValid( position ) || world.isAir( position ) ||
            (context.getStack().getItem() instanceof BlockItem && WorldUtil.isLiquidBlock( world, position )) )
        {
            return false;
        }

        BlockState state = world.getBlockState( position );

        boolean replaceable = state.canReplace( context );
        if( !allowReplaceable && replaceable ) return false;

        if( ComputerCraft.turtlesObeyBlockProtection )
        {
            // Check spawn protection
            boolean editable = replaceable
                ? TurtlePermissions.isBlockEditable( world, position, player )
                : TurtlePermissions.isBlockEditable( world, position.offset( side ), player );
            if( !editable )
            {
                if( outErrorMessage != null ) outErrorMessage[0] = "Cannot place in protected area";
                return false;
            }
        }

        return true;
    }

    @Nonnull
    private static ItemStack deployOnBlock( @Nonnull ItemStack stack, ITurtleAccess turtle, TurtlePlayer turtlePlayer, BlockPos position, Direction side, Object[] extraArguments, boolean allowReplace, String[] outErrorMessage )
    {
        // Re-orient the fake player
        Direction playerDir = side.getOpposite();
        BlockPos playerPosition = position.offset( side );
        orientPlayer( turtle, turtlePlayer, playerPosition, playerDir );

        ItemStack stackCopy = stack.copy();
        turtlePlayer.loadInventory( stackCopy );

        // Calculate where the turtle would hit the block
        float hitX = 0.5f + side.getOffsetX() * 0.5f;
        float hitY = 0.5f + side.getOffsetY() * 0.5f;
        float hitZ = 0.5f + side.getOffsetZ() * 0.5f;
        if( Math.abs( hitY - 0.5f ) < 0.01f )
        {
            hitY = 0.45f;
        }

        // Check if there's something suitable to place onto
        BlockHitResult hit = new BlockHitResult( new Vec3d( hitX, hitY, hitZ ), side, position, false );
        ItemUsageContext context = new ItemUsageContext( turtlePlayer, Hand.MAIN_HAND, hit );
        if( !canDeployOnBlock( new ItemPlacementContext( context ), turtle, turtlePlayer, position, side, allowReplace, outErrorMessage ) )
        {
            return stack;
        }

        // Load up the turtle's inventory
        Item item = stack.getItem();

        // Do the deploying (put everything in the players inventory)
        boolean placed = false;
        BlockEntity existingTile = turtle.getWorld().getBlockEntity( position );

        // See PlayerInteractionManager.processRightClickBlock
        // TODO: ^ Check we're still consistent.
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock( turtlePlayer, Hand.MAIN_HAND, position, side );
        if( !event.isCanceled() )
        {
            if( item.onItemUseFirst( stack, context ) == ActionResult.SUCCESS )
            {
                placed = true;
                turtlePlayer.loadInventory( stackCopy );
            }
            else if( event.getUseItem() != Event.Result.DENY &&
                stackCopy.useOnBlock( context ) == ActionResult.SUCCESS )
            {
                placed = true;
                turtlePlayer.loadInventory( stackCopy );
            }
        }

        if( !placed && (item instanceof BucketItem || item instanceof BoatItem || item instanceof LilyPadItem || item instanceof GlassBottleItem) )
        {
            ActionResult actionResult = ForgeHooks.onItemRightClick( turtlePlayer, Hand.MAIN_HAND );
            if( actionResult == ActionResult.SUCCESS )
            {
                placed = true;
            }
            else if( actionResult == null )
            {
                TypedActionResult<ItemStack> result = stackCopy.use( turtle.getWorld(), turtlePlayer, Hand.MAIN_HAND );
                if( result.getResult() == ActionResult.SUCCESS && !ItemStack.areEqualIgnoreDamage( stack, result.getValue() ) )
                {
                    placed = true;
                    turtlePlayer.loadInventory( result.getValue() );
                }
            }
        }

        // Set text on signs
        if( placed && item instanceof SignItem )
        {
            if( extraArguments != null && extraArguments.length >= 1 && extraArguments[0] instanceof String )
            {
                World world = turtle.getWorld();
                BlockEntity tile = world.getBlockEntity( position );
                if( tile == null || tile == existingTile )
                {
                    tile = world.getBlockEntity( position.offset( side ) );
                }
                if( tile instanceof SignBlockEntity )
                {
                    SignBlockEntity signTile = (SignBlockEntity) tile;
                    String s = (String) extraArguments[0];
                    String[] split = s.split( "\n" );
                    int firstLine = split.length <= 2 ? 1 : 0;
                    for( int i = 0; i < signTile.text.length; i++ )
                    {
                        if( i >= firstLine && i < firstLine + split.length )
                        {
                            if( split[i - firstLine].length() > 15 )
                            {
                                signTile.text[i] = new LiteralText( split[i - firstLine].substring( 0, 15 ) );
                            }
                            else
                            {
                                signTile.text[i] = new LiteralText( split[i - firstLine] );
                            }
                        }
                        else
                        {
                            signTile.text[i] = new LiteralText( "" );
                        }
                    }
                    signTile.markDirty();
                    world.updateListeners( tile.getPos(), tile.getCachedState(), tile.getCachedState(), 3 );
                }
            }
        }

        // Put everything we collected into the turtles inventory, then return
        ItemStack remainder = turtlePlayer.unloadInventory( turtle );
        if( !placed && ItemStack.areEqualIgnoreDamage( stack, remainder ) )
        {
            return stack;
        }
        else if( !remainder.isEmpty() )
        {
            return remainder;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }
}
