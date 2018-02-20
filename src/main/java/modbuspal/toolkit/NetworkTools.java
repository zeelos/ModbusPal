/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.toolkit;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author JMC15
 */
public class NetworkTools 
{
    public static boolean isLocalAddress(InetAddress a) 
    throws SocketException
    {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while( interfaces.hasMoreElements() )
        {
            NetworkInterface itf = interfaces.nextElement();
            Enumeration<InetAddress> addresses = itf.getInetAddresses();
            while(addresses.hasMoreElements())
            {
                InetAddress address = addresses.nextElement();
                if(a.equals(address)==true)
                {
                    return true;
                }
            }
        }
        return false;
    }
}
