/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.util;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;


public final class NBTUtil
{
    public static final int TAG_END = 0;
    public static final int TAG_BYTE = 1;
    public static final int TAG_SHORT = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTE_ARRAY = 7;
    public static final int TAG_STRING = 8;
    public static final int TAG_LIST = 9;
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT_ARRAY = 11;
    public static final int TAG_LONG_ARRAY = 12;
    public static final int TAG_ANY_NUMERIC = 99;

    private NBTUtil() {}

    private static Tag toNBTTag( Object object )
    {
        if( object == null ) return null;
        if( object instanceof Boolean ) return ByteTag.of( (byte) ((boolean) (Boolean) object ? 1 : 0) );
        if( object instanceof Number ) return DoubleTag.of( ((Number) object).doubleValue() );
        if( object instanceof String ) return StringTag.of( object.toString() );
        if( object instanceof Map )
        {
            Map<?, ?> m = (Map<?, ?>) object;
            CompoundTag nbt = new CompoundTag();
            int i = 0;
            for( Map.Entry<?, ?> entry : m.entrySet() )
            {
                Tag key = toNBTTag( entry.getKey() );
                Tag value = toNBTTag( entry.getKey() );
                if( key != null && value != null )
                {
                    nbt.put( "k" + i, key );
                    nbt.put( "v" + i, value );
                    i++;
                }
            }
            nbt.putInt( "len", m.size() );
            return nbt;
        }

        return null;
    }

    public static CompoundTag encodeObjects( Object[] objects )
    {
        if( objects == null || objects.length <= 0 ) return null;

        CompoundTag nbt = new CompoundTag();
        nbt.putInt( "len", objects.length );
        for( int i = 0; i < objects.length; i++ )
        {
            Tag child = toNBTTag( objects[i] );
            if( child != null ) nbt.put( Integer.toString( i ), child );
        }
        return nbt;
    }

    private static Object fromNBTTag( Tag tag )
    {
        if( tag == null ) return null;
        switch( tag.getType() )
        {
            case TAG_BYTE:
                return ((ByteTag) tag).getByte() > 0;
            case TAG_DOUBLE:
                return ((DoubleTag) tag).getDouble();
            default:
            case TAG_STRING:
                return tag.asString();
            case TAG_COMPOUND:
            {
                CompoundTag c = (CompoundTag) tag;
                int len = c.getInt( "len" );
                Map<Object, Object> map = new HashMap<>( len );
                for( int i = 0; i < len; i++ )
                {
                    Object key = fromNBTTag( c.get( "k" + i ) );
                    Object value = fromNBTTag( c.get( "v" + i ) );
                    if( key != null && value != null ) map.put( key, value );
                }
                return map;
            }
        }
    }

    public static Object toLua( Tag tag )
    {
        if( tag == null ) return null;

        byte typeID = tag.getType();
        switch( typeID )
        {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
                return ((AbstractNumberTag) tag).getLong();
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
                return ((AbstractNumberTag) tag).getDouble();
            case Constants.NBT.TAG_STRING: // String
                return tag.asString();
            case Constants.NBT.TAG_COMPOUND: // Compound
            {
                CompoundTag compound = (CompoundTag) tag;
                Map<String, Object> map = new HashMap<>( compound.getSize() );
                for( String key : compound.getKeys() )
                {
                    Object value = toLua( compound.get( key ) );
                    if( value != null ) map.put( key, value );
                }
                return map;
            }
            case Constants.NBT.TAG_LIST:
            {
                ListTag list = (ListTag) tag;
                Map<Integer, Object> map = new HashMap<>( list.size() );
                for( int i = 0; i < list.size(); i++ ) map.put( i, toLua( list.get( i ) ) );
                return map;
            }
            case Constants.NBT.TAG_BYTE_ARRAY:
            {
                byte[] array = ((ByteArrayTag) tag).getByteArray();
                Map<Integer, Byte> map = new HashMap<>( array.length );
                for( int i = 0; i < array.length; i++ ) map.put( i + 1, array[i] );
                return map;
            }
            case Constants.NBT.TAG_INT_ARRAY:
            {
                int[] array = ((IntArrayTag) tag).getIntArray();
                Map<Integer, Integer> map = new HashMap<>( array.length );
                for( int i = 0; i < array.length; i++ ) map.put( i + 1, array[i] );
                return map;
            }

            default:
                return null;
        }
    }

    public static Object[] decodeObjects( CompoundTag tag )
    {
        int len = tag.getInt( "len" );
        if( len <= 0 ) return null;

        Object[] objects = new Object[len];
        for( int i = 0; i < len; i++ )
        {
            String key = Integer.toString( i );
            if( tag.contains( key ) )
            {
                objects[i] = fromNBTTag( tag.get( key ) );
            }
        }
        return objects;
    }
}
