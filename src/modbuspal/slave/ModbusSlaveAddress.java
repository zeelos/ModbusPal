/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.slave;

import java.net.InetAddress;

/**
 *
 * @author JMC15
 */
public class ModbusSlaveAddress
{

    public static ModbusSlaveAddress parse(String slaveAddress) 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    private int rtuAddress = -1;
    private InetAddress ipAddress = null;
    
    public ModbusSlaveAddress(InetAddress a, int n)
    {
        ipAddress = a;
        rtuAddress = n;
    }
    
    public ModbusSlaveAddress(int n)
    {
        rtuAddress = n;
        ipAddress = null;
    }
    
    public ModbusSlaveAddress(InetAddress a)
    {
        ipAddress = a;
        rtuAddress = -1;        
    }
    
    public InetAddress getIpAddress()
    {
        return ipAddress;
    }
    
    public void setIpAddress(InetAddress ip)
    {
        ipAddress = ip;
    }
    
    public int getRtuAddress()
    {
        return rtuAddress;
    }
    
    public void setRtuAddress(int n)
    {
        rtuAddress = n;
    }

    @Override
    public String toString() 
    {
        if( ipAddress!=null )
        {
            if( rtuAddress != -1 )
            {
                return String.format("%s(%d)", ipAddress.getHostAddress(), rtuAddress);
            }
            else
            {
                return String.format("%s", ipAddress.getHostAddress());
            }
        }
        else if( rtuAddress != -1 )
        {
            return String.format("%d", rtuAddress);
        }
        else
        {
            return super.toString();
        }
    }

    @Override
    public int hashCode() 
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) 
    {
        if(o instanceof ModbusSlaveAddress)
        {
            ModbusSlaveAddress other = (ModbusSlaveAddress)o;
            return toString().compareTo(other.toString())==0;
        }
        return false;
    }
    
    
    
    
}
