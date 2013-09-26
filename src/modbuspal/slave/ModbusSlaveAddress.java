/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.slave;

import java.net.InetAddress;
import modbuspal.main.ModbusConst;

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
        
        if( (n<ModbusConst.FIRST_MODBUS_SLAVE) || (n>ModbusConst.LAST_MODBUS_SLAVE) )
        {
            n= -1;
        }
        rtuAddress = n;
    }
    
    public ModbusSlaveAddress(int n)
    {
        if( (n<ModbusConst.FIRST_MODBUS_SLAVE) || (n>ModbusConst.LAST_MODBUS_SLAVE) )
        {
            n= -1;
        }
        
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
        if( (n<ModbusConst.FIRST_MODBUS_SLAVE) || (n>ModbusConst.LAST_MODBUS_SLAVE) )
        {
            n= -1;
        }        
        rtuAddress = n;
    }

    public String toBaseString()
    {
        return String.format("{ rtuAddress : %d, ipAddress : %s }", 
                rtuAddress,
                (ipAddress==null)?"null":ipAddress.toString());
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
