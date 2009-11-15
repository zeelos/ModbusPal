/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.main.ModbusRequest;

/**
 *
 * @author nnovic
 */
public class ModbusTcpIpLink
implements ModbusLink, Runnable
{
    private ServerSocket serverSocket;
    private Thread serverThread;
    private boolean executeThread;
    private ModbusLinkListener listener = null;

    public ModbusTcpIpLink(int port)
    throws IOException
    {
        super();
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void start(ModbusLinkListener l)
    {
        executeThread = true;
        serverThread = new Thread(this,"tcp/ip link");
        listener = l;
        serverThread.start();
    }

    public void stop()
    {
        executeThread = false;
        serverThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ModbusTcpIpLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try
        {
            serverThread.join();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ModbusTcpIpLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        serverThread = null;
        ModbusTcpIpSlaveDispatcher.stopAll();
    }

    public void run()
    {
        System.out.println("Start ModbusTcpIpLink");

        while(executeThread == true)
        {
            // create client socket
            try
            {
                Socket socket = serverSocket.accept();
                ModbusTcpIpSlaveDispatcher slave = new ModbusTcpIpSlaveDispatcher(socket);
                slave.start();
            }
            catch (IOException ex)
            {
                if( Thread.interrupted() == false )
                {
                    Logger.getLogger(ModbusTcpIpLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
        }

        if( serverSocket.isClosed() == false )
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(ModbusTcpIpLink.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        serverSocket = null;
        
        System.out.println("Stop ModbusTcpIpLink");
        listener.linkBroken();
        listener = null;
    }

    @Override
    public void execute(ModbusRequest req)
    {
        //TODO: implement
    }
}
