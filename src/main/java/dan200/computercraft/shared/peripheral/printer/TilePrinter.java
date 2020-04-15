/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.printer;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.util.ColourUtils;
import dan200.computercraft.shared.util.DefaultSidedInventory;
import dan200.computercraft.shared.util.NamedTileEntityType;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.container.Container;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


import net.minecraft.util.math.Direction;

public final class TilePrinter extends TileGeneric implements DefaultSidedInventory, IPeripheralTile, Nameable, NameableContainerFactory
{
    public static final NamedTileEntityType<TilePrinter> FACTORY = NamedTileEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "printer" ),
        TilePrinter::new
    );

    private static final String NBT_NAME = "CustomName";
    private static final String NBT_PRINTING = "Printing";
    private static final String NBT_PAGE_TITLE = "PageTitle";

    static final int SLOTS = 13;

    private static final int[] BOTTOM_SLOTS = new int[] { 7, 8, 9, 10, 11, 12 };
    private static final int[] TOP_SLOTS = new int[] { 1, 2, 3, 4, 5, 6 };
    private static final int[] SIDE_SLOTS = new int[] { 0 };

    Text customName;

    private final DefaultedList<ItemStack> m_inventory = DefaultedList.ofSize( SLOTS, ItemStack.EMPTY );
    private LazyOptional<IItemHandlerModifiable>[] itemHandlerCaps;

    private final Terminal m_page = new Terminal( ItemPrintout.LINE_MAX_LENGTH, ItemPrintout.LINES_PER_PAGE );
    private String m_pageTitle = "";
    private boolean m_printing = false;

    private TilePrinter()
    {
        super( FACTORY );
    }

    @Override
    public void destroy()
    {
        ejectContents();
    }

    @Override
    protected void invalidateCaps()
    {
        super.invalidateCaps();

        if( itemHandlerCaps != null )
        {
            for( int i = 0; i < itemHandlerCaps.length; i++ )
            {
                if( itemHandlerCaps[i] == null ) continue;
                itemHandlerCaps[i].invalidate();
                itemHandlerCaps[i] = null;
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult onActivate( PlayerEntity player, Hand hand, BlockHitResult hit )
    {
        if( player.isInSneakingPose() ) return ActionResult.PASS;

        if( !getWorld().isClient ) NetworkHooks.openGui( (ServerPlayerEntity) player, this );
        return ActionResult.SUCCESS;
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );

        customName = nbt.contains( NBT_NAME ) ? Text.Serializer.fromJson( nbt.getString( NBT_NAME ) ) : null;

        // Read page
        synchronized( m_page )
        {
            m_printing = nbt.getBoolean( NBT_PRINTING );
            m_pageTitle = nbt.getString( NBT_PAGE_TITLE );
            m_page.readFromNBT( nbt );
        }

        // Read inventory
        Inventories.fromTag( nbt, m_inventory );
    }

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        if( customName != null ) nbt.putString( NBT_NAME, Text.Serializer.toJson( customName ) );

        // Write page
        synchronized( m_page )
        {
            nbt.putBoolean( NBT_PRINTING, m_printing );
            nbt.putString( NBT_PAGE_TITLE, m_pageTitle );
            m_page.writeToNBT( nbt );
        }

        // Write inventory
        Inventories.toTag( nbt, m_inventory );

        return super.toTag( nbt );
    }

    boolean isPrinting()
    {
        return m_printing;
    }

    // IInventory implementation
    @Override
    public int getInvSize()
    {
        return m_inventory.size();
    }

    @Override
    public boolean isInvEmpty()
    {
        for( ItemStack stack : m_inventory )
        {
            if( !stack.isEmpty() ) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getInvStack( int slot )
    {
        return m_inventory.get( slot );
    }

    @Nonnull
    @Override
    public ItemStack removeInvStack( int slot )
    {
        ItemStack result = m_inventory.get( slot );
        m_inventory.set( slot, ItemStack.EMPTY );
        markDirty();
        updateBlockState();
        return result;
    }

    @Nonnull
    @Override
    public ItemStack takeInvStack( int slot, int count )
    {
        ItemStack stack = m_inventory.get( slot );
        if( stack.isEmpty() ) return ItemStack.EMPTY;

        if( stack.getCount() <= count )
        {
            setInvStack( slot, ItemStack.EMPTY );
            return stack;
        }

        ItemStack part = stack.split( count );
        if( m_inventory.get( slot ).isEmpty() )
        {
            m_inventory.set( slot, ItemStack.EMPTY );
            updateBlockState();
        }
        markDirty();
        return part;
    }

    @Override
    public void setInvStack( int slot, @Nonnull ItemStack stack )
    {
        m_inventory.set( slot, stack );
        markDirty();
        updateBlockState();
    }

    @Override
    public void clear()
    {
        for( int i = 0; i < m_inventory.size(); i++ ) m_inventory.set( i, ItemStack.EMPTY );
        markDirty();
        updateBlockState();
    }

    @Override
    public boolean isValidInvStack( int slot, @Nonnull ItemStack stack )
    {
        if( slot == 0 )
        {
            return isInk( stack );
        }
        else if( slot >= TOP_SLOTS[0] && slot <= TOP_SLOTS[TOP_SLOTS.length - 1] )
        {
            return isPaper( stack );
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean canPlayerUseInv( @Nonnull PlayerEntity playerEntity )
    {
        return isUsable( playerEntity, false );
    }

    // ISidedInventory implementation

    @Nonnull
    @Override
    public int[] getInvAvailableSlots( @Nonnull Direction side )
    {
        switch( side )
        {
            case DOWN: // Bottom (Out tray)
                return BOTTOM_SLOTS;
            case UP: // Top (In tray)
                return TOP_SLOTS;
            default: // Sides (Ink)
                return SIDE_SLOTS;
        }
    }

    // IPeripheralTile implementation

    @Override
    public IPeripheral getPeripheral( @Nonnull Direction side )
    {
        return new PrinterPeripheral( this );
    }

    @Nullable
    Terminal getCurrentPage()
    {
        synchronized( m_page )
        {
            return m_printing ? m_page : null;
        }
    }

    boolean startNewPage()
    {
        synchronized( m_page )
        {
            if( !canInputPage() ) return false;
            if( m_printing && !outputPage() ) return false;
            return inputPage();
        }
    }

    boolean endCurrentPage()
    {
        synchronized( m_page )
        {
            return m_printing && outputPage();
        }
    }

    int getInkLevel()
    {
        ItemStack inkStack = m_inventory.get( 0 );
        return isInk( inkStack ) ? inkStack.getCount() : 0;
    }

    int getPaperLevel()
    {
        int count = 0;
        for( int i = 1; i < 7; i++ )
        {
            ItemStack paperStack = m_inventory.get( i );
            if( isPaper( paperStack ) ) count += paperStack.getCount();
        }
        return count;
    }

    void setPageTitle( String title )
    {
        synchronized( m_page )
        {
            if( m_printing ) m_pageTitle = title;
        }
    }

    private static boolean isInk( @Nonnull ItemStack stack )
    {
        return stack.getItem() instanceof DyeItem;
    }

    private static boolean isPaper( @Nonnull ItemStack stack )
    {
        Item item = stack.getItem();
        return item == Items.PAPER
            || (item instanceof ItemPrintout && ((ItemPrintout) item).getType() == ItemPrintout.Type.PAGE);
    }

    private boolean canInputPage()
    {
        ItemStack inkStack = m_inventory.get( 0 );
        return !inkStack.isEmpty() && isInk( inkStack ) && getPaperLevel() > 0;
    }

    private boolean inputPage()
    {
        ItemStack inkStack = m_inventory.get( 0 );
        if( !isInk( inkStack ) ) return false;

        for( int i = 1; i < 7; i++ )
        {
            ItemStack paperStack = m_inventory.get( i );
            if( paperStack.isEmpty() || !isPaper( paperStack ) ) continue;

            // Setup the new page
            DyeColor dye = ColourUtils.getStackColour( inkStack );
            m_page.setTextColour( dye != null ? dye.getId() : 15 );

            m_page.clear();
            if( paperStack.getItem() instanceof ItemPrintout )
            {
                m_pageTitle = ItemPrintout.getTitle( paperStack );
                String[] text = ItemPrintout.getText( paperStack );
                String[] textColour = ItemPrintout.getColours( paperStack );
                for( int y = 0; y < m_page.getHeight(); y++ )
                {
                    m_page.setLine( y, text[y], textColour[y], "" );
                }
            }
            else
            {
                m_pageTitle = "";
            }
            m_page.setCursorPos( 0, 0 );

            // Decrement ink
            inkStack.decrement( 1 );
            if( inkStack.isEmpty() ) m_inventory.set( 0, ItemStack.EMPTY );

            // Decrement paper
            paperStack.decrement( 1 );
            if( paperStack.isEmpty() )
            {
                m_inventory.set( i, ItemStack.EMPTY );
                updateBlockState();
            }

            markDirty();
            m_printing = true;
            return true;
        }
        return false;
    }

    private boolean outputPage()
    {
        int height = m_page.getHeight();
        String[] lines = new String[height];
        String[] colours = new String[height];
        for( int i = 0; i < height; i++ )
        {
            lines[i] = m_page.getLine( i ).toString();
            colours[i] = m_page.getTextColourLine( i ).toString();
        }

        ItemStack stack = ItemPrintout.createSingleFromTitleAndText( m_pageTitle, lines, colours );
        for( int slot : BOTTOM_SLOTS )
        {
            if( m_inventory.get( slot ).isEmpty() )
            {
                setInvStack( slot, stack );
                m_printing = false;
                return true;
            }
        }
        return false;
    }

    private void ejectContents()
    {
        for( int i = 0; i < 13; i++ )
        {
            ItemStack stack = m_inventory.get( i );
            if( !stack.isEmpty() )
            {
                // Remove the stack from the inventory
                setInvStack( i, ItemStack.EMPTY );

                // Spawn the item in the world
                WorldUtil.dropItemStack( stack, getWorld(), new Vec3d( getPos() ).add( 0.5, 0.75, 0.5 ) );
            }
        }
    }

    private void updateBlockState()
    {
        boolean top = false, bottom = false;
        for( int i = 1; i < 7; i++ )
        {
            ItemStack stack = m_inventory.get( i );
            if( !stack.isEmpty() && isPaper( stack ) )
            {
                top = true;
                break;
            }
        }
        for( int i = 7; i < 13; i++ )
        {
            ItemStack stack = m_inventory.get( i );
            if( !stack.isEmpty() && isPaper( stack ) )
            {
                bottom = true;
                break;
            }
        }

        updateBlockState( top, bottom );
    }

    private void updateBlockState( boolean top, boolean bottom )
    {
        if( removed ) return;

        BlockState state = getCachedState();
        if( state.get( BlockPrinter.TOP ) == top & state.get( BlockPrinter.BOTTOM ) == bottom ) return;

        getWorld().setBlockState( getPos(), state.with( BlockPrinter.TOP, top ).with( BlockPrinter.BOTTOM, bottom ) );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability( @Nonnull Capability<T> capability, @Nullable Direction facing )
    {
        if( capability == ITEM_HANDLER_CAPABILITY )
        {
            LazyOptional<IItemHandlerModifiable>[] handlers = itemHandlerCaps;
            if( handlers == null ) handlers = itemHandlerCaps = new LazyOptional[7];

            int index = facing == null ? 0 : 1 + facing.getId();
            LazyOptional<IItemHandlerModifiable> handler = handlers[index];
            if( handler == null )
            {
                handler = handlers[index] = facing == null
                    ? LazyOptional.of( () -> new InvWrapper( this ) )
                    : LazyOptional.of( () -> new SidedInvWrapper( this, facing ) );
            }

            return handler.cast();
        }

        return super.getCapability( capability, facing );
    }

    @Override
    public boolean hasCustomName()
    {
        return customName != null;
    }

    @Nullable
    @Override
    public Text getCustomName()
    {
        return customName;
    }

    @Nonnull
    @Override
    public Text getName()
    {
        return customName != null ? customName : new TranslatableText( getCachedState().getBlock().getTranslationKey() );
    }

    @Override
    public Text getDisplayName()
    {
        return Nameable.super.getDisplayName();
    }

    @Nonnull
    @Override
    public Container createMenu( int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player )
    {
        return new ContainerPrinter( id, inventory, this );
    }
}
