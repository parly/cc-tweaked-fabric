/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.printer;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.network.Containers;
import dan200.computercraft.shared.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TilePrinter extends TileGeneric implements DefaultSidedInventory, IPeripheralTile, DefaultPropertyDelegate, Nameable
{
    public static final NamedBlockEntityType<TilePrinter> FACTORY = NamedBlockEntityType.create(
        new Identifier( ComputerCraft.MOD_ID, "printer" ),
        TilePrinter::new
    );

    private static final String NBT_NAME = "CustomName";
    private static final String NBT_PRINTING = "Printing";
    private static final String NBT_PAGE_TITLE = "PageTitle";

    public static final int PROPERTY_SIZE = 1;
    public static final int PROPERTY_PRINTING = 0;

    public static final int INVENTORY_SIZE = 13;

    private static final int[] BOTTOM_SLOTS = new int[] { 7, 8, 9, 10, 11, 12 };
    private static final int[] TOP_SLOTS = new int[] { 1, 2, 3, 4, 5, 6 };
    private static final int[] SIDE_SLOTS = new int[] { 0 };

    Text customName;

    private final DefaultedList<ItemStack> m_inventory = DefaultedList.ofSize( INVENTORY_SIZE, ItemStack.EMPTY );
    private final ItemStorage m_itemHandlerAll = ItemStorage.wrap( this );

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
    public boolean onActivate( PlayerEntity player, Hand hand, BlockHitResult hit )
    {
        if( player.isSneaking() ) return false;

        if( !getWorld().isClient ) Containers.openPrinterGUI( player, this );
        return true;
    }

    @Override
    public void fromTag( CompoundTag nbt )
    {
        super.fromTag( nbt );

        customName = nbt.contains( NBT_NAME ) ? LiteralText.Serializer.fromJson( nbt.getString( NBT_NAME ) ) : null;

        // Read page
        synchronized( m_page )
        {
            m_printing = nbt.getBoolean( NBT_PRINTING );
            m_pageTitle = nbt.getString( NBT_PAGE_TITLE );
            m_page.readFromNBT( nbt );
        }

        // Read inventory
        synchronized( m_inventory )
        {
            Inventories.fromTag( nbt, m_inventory );
        }
    }

    @Nonnull
    @Override
    public CompoundTag toTag( CompoundTag nbt )
    {
        if( customName != null ) nbt.putString( NBT_NAME, LiteralText.Serializer.toJson( customName ) );

        // Write page
        synchronized( m_page )
        {
            nbt.putBoolean( NBT_PRINTING, m_printing );
            nbt.putString( NBT_PAGE_TITLE, m_pageTitle );
            m_page.writeToNBT( nbt );
        }

        // Write inventory
        synchronized( m_inventory )
        {
            Inventories.toTag( nbt, m_inventory );
        }

        return super.toTag( nbt );
    }

    @Override
    protected void writeDescription( @Nonnull CompoundTag nbt )
    {
        super.writeDescription( nbt );
        if( customName != null ) nbt.putString( NBT_NAME, LiteralText.Serializer.toJson( customName ) );
    }

    @Override
    public void readDescription( @Nonnull CompoundTag nbt )
    {
        super.readDescription( nbt );
        customName = nbt.contains( NBT_NAME ) ? LiteralText.Serializer.fromJson( nbt.getString( NBT_NAME ) ) : null;
        updateBlock();
    }

    public boolean isPrinting()
    {
        return m_printing;
    }

    // Inventory implementation
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
    public ItemStack getInvStack( int i )
    {
        return m_inventory.get( i );
    }

    @Nonnull
    @Override
    public ItemStack removeInvStack( int i )
    {
        synchronized( m_inventory )
        {
            ItemStack result = m_inventory.get( i );
            m_inventory.set( i, ItemStack.EMPTY );
            markDirty();
            updateBlockState();
            return result;
        }
    }

    @Nonnull
    @Override
    public ItemStack takeInvStack( int i, int j )
    {
        synchronized( m_inventory )
        {
            if( m_inventory.get( i ).isEmpty() ) return ItemStack.EMPTY;

            if( m_inventory.get( i ).getCount() <= j )
            {
                ItemStack itemstack = m_inventory.get( i );
                m_inventory.set( i, ItemStack.EMPTY );
                markDirty();
                updateBlockState();
                return itemstack;
            }

            ItemStack part = m_inventory.get( i ).split( j );
            if( m_inventory.get( i ).isEmpty() )
            {
                m_inventory.set( i, ItemStack.EMPTY );
                updateBlockState();
            }
            markDirty();
            return part;
        }
    }

    @Override
    public void setInvStack( int i, @Nonnull ItemStack stack )
    {
        synchronized( m_inventory )
        {
            m_inventory.set( i, stack );
            markDirty();
            updateBlockState();
        }
    }

    @Override
    public void clear()
    {
        synchronized( m_inventory )
        {
            for( int i = 0; i < m_inventory.size(); i++ ) m_inventory.set( i, ItemStack.EMPTY );
            markDirty();
            updateBlockState();
        }
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
    public boolean canPlayerUseInv( PlayerEntity playerEntity )
    {
        return isUsable( playerEntity, false );
    }

    // ISidedInventory implementation

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

    public Terminal getCurrentPage()
    {
        return m_printing ? m_page : null;
    }

    public boolean startNewPage()
    {
        synchronized( m_inventory )
        {
            if( !canInputPage() ) return false;
            if( m_printing && !outputPage() ) return false;
            return inputPage();
        }
    }

    public boolean endCurrentPage()
    {
        synchronized( m_inventory )
        {
            if( m_printing && outputPage() )
            {
                return true;
            }
        }
        return false;
    }

    public int getInkLevel()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            return isInk( inkStack ) ? inkStack.getCount() : 0;
        }
    }

    public int getPaperLevel()
    {
        int count = 0;
        synchronized( m_inventory )
        {
            for( int i = 1; i < 7; i++ )
            {
                ItemStack paperStack = m_inventory.get( i );
                if( !paperStack.isEmpty() && isPaper( paperStack ) )
                {
                    count += paperStack.getCount();
                }
            }
        }
        return count;
    }

    public void setPageTitle( String title )
    {
        if( m_printing )
        {
            m_pageTitle = title;
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
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            return !inkStack.isEmpty() && isInk( inkStack ) && getPaperLevel() > 0;
        }
    }

    private boolean inputPage()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory.get( 0 );
            if( !isInk( inkStack ) ) return false;

            for( int i = 1; i < 7; i++ )
            {
                ItemStack paperStack = m_inventory.get( i );
                if( !paperStack.isEmpty() && isPaper( paperStack ) )
                {
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
            }
            return false;
        }
    }

    private boolean outputPage()
    {
        synchronized( m_page )
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
            synchronized( m_inventory )
            {
                for( int slot : BOTTOM_SLOTS )
                {
                    if( m_inventory.get( slot ).isEmpty() )
                    {
                        setInvStack( slot, stack );
                        m_printing = false;
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void ejectContents()
    {
        synchronized( m_inventory )
        {
            for( int i = 0; i < 13; i++ )
            {
                ItemStack stack = m_inventory.get( i );
                if( !stack.isEmpty() )
                {
                    // Remove the stack from the inventory
                    setInvStack( i, ItemStack.EMPTY );

                    // Spawn the item in the world
                    BlockPos pos = getPos();
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.75;
                    double z = pos.getZ() + 0.5;
                    WorldUtil.dropItemStack( stack, getWorld(), x, y, z );
                }
            }
        }
    }

    private void updateBlockState()
    {
        boolean top = false, bottom = false;
        synchronized( m_inventory )
        {
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

    @Override
    public int get( int property )
    {
        if( property == PROPERTY_PRINTING ) return isPrinting() ? 1 : 0;
        return 0;
    }

    @Override
    public int size()
    {
        return PROPERTY_SIZE;
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
        return customName != null ? customName : getCachedState().getBlock().getName();
    }
}
