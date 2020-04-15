/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.computer.core;

import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.network.NetworkHandler;
import dan200.computercraft.shared.network.server.*;
import net.minecraft.nbt.CompoundTag;

public class ClientComputer extends ClientTerminal implements IComputer
{
    private final int m_instanceID;

    private boolean m_on = false;
    private boolean m_blinking = false;
    private CompoundTag m_userData = null;


    public ClientComputer( int instanceID )
    {
        super( false );
        m_instanceID = instanceID;
    }

    public CompoundTag getUserData()
    {
        return m_userData;
    }

    public void requestState()
    {
        // Request state from server
        NetworkHandler.sendToServer( new RequestComputerMessage( getInstanceID() ) );
    }

    // IComputer

    @Override
    public int getInstanceID()
    {
        return m_instanceID;
    }

    @Override
    public boolean isOn()
    {
        return m_on;
    }

    @Override
    public boolean isCursorDisplayed()
    {
        return m_on && m_blinking;
    }

    @Override
    public void turnOn()
    {
        // Send turnOn to server
        NetworkHandler.sendToServer( new ComputerActionServerMessage( m_instanceID, ComputerActionServerMessage.Action.TURN_ON ) );
    }

    @Override
    public void shutdown()
    {
        // Send shutdown to server
        NetworkHandler.sendToServer( new ComputerActionServerMessage( m_instanceID, ComputerActionServerMessage.Action.SHUTDOWN ) );
    }

    @Override
    public void reboot()
    {
        // Send reboot to server
        NetworkHandler.sendToServer( new ComputerActionServerMessage( m_instanceID, ComputerActionServerMessage.Action.REBOOT ) );
    }

    @Override
    public void queueEvent( String event, Object[] arguments )
    {
        // Send event to server
        NetworkHandler.sendToServer( new QueueEventServerMessage( m_instanceID, event, arguments ) );
    }

    @Override
    public void keyDown( int key, boolean repeat )
    {
        NetworkHandler.sendToServer( new KeyEventServerMessage( m_instanceID, repeat ? KeyEventServerMessage.TYPE_REPEAT : KeyEventServerMessage.TYPE_DOWN, key ) );
    }

    @Override
    public void keyUp( int key )
    {
        NetworkHandler.sendToServer( new KeyEventServerMessage( m_instanceID, KeyEventServerMessage.TYPE_UP, key ) );
    }

    @Override
    public void mouseClick( int button, int x, int y )
    {
        NetworkHandler.sendToServer( new MouseEventServerMessage( m_instanceID, MouseEventServerMessage.TYPE_CLICK, button, x, y ) );
    }

    @Override
    public void mouseUp( int button, int x, int y )
    {
        NetworkHandler.sendToServer( new MouseEventServerMessage( m_instanceID, MouseEventServerMessage.TYPE_UP, button, x, y ) );
    }

    @Override
    public void mouseDrag( int button, int x, int y )
    {
        NetworkHandler.sendToServer( new MouseEventServerMessage( m_instanceID, MouseEventServerMessage.TYPE_DRAG, button, x, y ) );
    }

    @Override
    public void mouseScroll( int direction, int x, int y )
    {
        NetworkHandler.sendToServer( new MouseEventServerMessage( m_instanceID, MouseEventServerMessage.TYPE_SCROLL, direction, x, y ) );
    }

    public void setState( ComputerState state, CompoundTag userData )
    {
        m_on = state != ComputerState.OFF;
        m_blinking = state == ComputerState.BLINKING;
        m_userData = userData;
    }
}
