/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.slave.ModbusSlave;
import modbuspal.slave.ModbusSlaveAddress;
import modbuspal.toolkit.NetworkTools;

/**
 * takes care of some particularies of the management of the modbus slaves
 * in the project
 * @author nnovic
 */
public abstract class ModbusPalProject2
{
    final private HashMap<ModbusSlaveAddress, ModbusSlave> knownSlaves = new HashMap<ModbusSlaveAddress, ModbusSlave>();

    // The MODBUS ADDRESS we are trying to match has an InetAddress.
    // 1/ A perfect match would be a MODBUS ADDRESS with the same InetAddress
    // and also the same Rtu address.
    // 2/ A very good match would be a MODBUS ADDRESS with no InetAddress but
    // with a matching Rtu address.
    // 3/ An acceptable match would be a MODBUS address with an
    // InetAddress corresponding to a local network interface and a
    // matching Rtu address.
    // 4/ As a last resort, a MODBUS address with an InetAddress corresponding
    // to a local network interface and no specified Rtu address would be ok.    
    private ModbusSlaveAddress getMatchingTcpAddress(ModbusSlaveAddress id)
    {
        ModbusSlaveAddress bestMatch = null;
        int matchRate = Integer.MAX_VALUE;
        
        Set<ModbusSlaveAddress> addresses = knownSlaves.keySet();
        for(ModbusSlaveAddress address : addresses )
        {
            if(address.getIpAddress()==null)
            {
                if(address.getRtuAddress()==id.getRtuAddress())
                {
                    if( matchRate > 2 )
                    {
                        bestMatch = address;
                        matchRate = 2;
                    }
                }
            }
            
            else  if( address.getIpAddress().equals(id.getIpAddress()) == true )
            {
                // perfect IP address match. But now have to check the
                // optional slave identifier.
                if( address.getRtuAddress() == id.getRtuAddress() )
                {
                    if(matchRate > 1 )
                    {
                        bestMatch = address;
                        matchRate = 1;
                    }
                }  
                else if( address.getRtuAddress() == -1 )
                {
                    if(matchRate > 3)
                    {
                        bestMatch = address;
                        matchRate = 3;
                    }
                }
            }
            
            else
            {
                try 
                {
                    if( NetworkTools.isLocalAddress(address.getIpAddress())==true )
                    {
                        if( address.getRtuAddress() == id.getRtuAddress() )
                        {
                            if(matchRate > 4)
                            {
                                bestMatch=address;
                                matchRate = 4;
                            }
                        }
                        else if( address.getRtuAddress() == -1 )
                        {
                            if(matchRate > 5)
                            {
                                bestMatch = address;
                                matchRate = 5;
                            }
                        }
                    }
                }
                catch (SocketException ex) 
                {
                    Logger.getLogger(ModbusPalProject2.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
        return bestMatch;

    }
    
    
    
    private ModbusSlaveAddress getMatchingRtuAddress(ModbusSlaveAddress id)
    {
        ModbusSlaveAddress bestMatch = null;
        int matchRate = Integer.MAX_VALUE;
        
        Set<ModbusSlaveAddress> addresses = knownSlaves.keySet();
        for(ModbusSlaveAddress address : addresses )
        {
            if(address.getIpAddress()==null)
            {
                if(address.getRtuAddress()==id.getRtuAddress())
                {
                    if( matchRate > 1 )
                    {
                        bestMatch = address;
                        matchRate = 1;
                    }
                }
            }
            
            else
            {
                try 
                {
                    if( NetworkTools.isLocalAddress(address.getIpAddress())==true )
                    {
                        if( address.getRtuAddress() == id.getRtuAddress() )
                        {
                            if(matchRate > 2)
                            {
                                bestMatch=address;
                                matchRate = 2;
                            }
                        }
                        else if( address.getRtuAddress() == -1 )
                        {
                            if(matchRate > 3)
                            {
                                bestMatch = address;
                                matchRate = 3;
                            }
                        }
                    }
                }
                catch (SocketException ex) 
                {
                    Logger.getLogger(ModbusPalProject2.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
        return bestMatch;        
    }
    
    /**
     * tries to retrieve an already existing ModbusSlaveAddress instance that
     * might match the one in argument.
     * @param id
     * @return tries 
     */
    private ModbusSlaveAddress getMatchingSlaveAddress(ModbusSlaveAddress id)
    {
        // if the slave address in argument has no ip address
        // (MODBUS RTU flavor)...
        if( id.getIpAddress()==null )
        {
            return getMatchingRtuAddress(id);
        }

        // MODBUS TCP flavor...
        else
        {                
            return getMatchingTcpAddress(id);
        }
    }
    
     /**
     * Returns the MODBUS slave identified by its slave number. If the slave
     * does not exist, and if createIfNotExist is true, then the slave is created
     * on-the-fly.
     * @param id the slave number
     * @param createIfNotExist if true, and if there is no defined MODBUS slave
     * for the specified slave number, then it is created.
     * @return the instance of ModbusSlave associated with the slave number
     * or null if no slave is associated with this number.
     */
    public ModbusSlave getModbusSlave(ModbusSlaveAddress id, boolean createIfNotExist)
    {
        synchronized(knownSlaves)
        {
            ModbusSlaveAddress matchedId = getMatchingSlaveAddress(id);
            if( (matchedId!=null) && (knownSlaves.get(matchedId)!=null) )
            {
                return knownSlaves.get(matchedId);
            }
            else if( createIfNotExist==true )
            {
                setModbusSlave( id, new ModbusSlave(id) );
                return knownSlaves.get(id);
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Defines the specified modbus slave as having the specified slave number
     * @param id the slave number
     * @param s the modbus slave definition
     * @return the modbus slave was defined for this number before being replace,
     * or null if there was no modbus slave previously defined for this slave
     * number.
     */
    protected ModbusSlave setModbusSlave(ModbusSlaveAddress id, ModbusSlave s)
    {
        synchronized(knownSlaves)
        {
            ModbusSlave old = knownSlaves.get(id);
            knownSlaves.put(id, s);
            if(s!=null)
            {
                notifySlaveAdded(s);
            }
            else if(old!=null)
            {
                notifySlaveRemoved(old);
            }

            return old;
        }
    }

    /**
     * Triggers the listeners to notify them of the removal of
     * a MODBUS slave from the project.
     * @param slave the slave that has been removed from the project.
     */
    protected abstract void notifySlaveRemoved(ModbusSlave slave);
    
    /**
     * Triggers the listeners to notify them of the addition of a
     * new MODBUS slave in the project.
     * @param slave the slave that has been added in the project
     */
    protected abstract void notifySlaveAdded(ModbusSlave slave);
    
    /**
     * Returns the MODBUS slave identified by its slave number.
     * @param id the slave number
     * @return the instance of ModbusSlave associated with the slave number
     * or null if no slave is associated with this number.
     */
    public ModbusSlave getModbusSlave(ModbusSlaveAddress id)
    {
        return getModbusSlave(id, false);
    }

    /**
     * Get all the slaves indexed by their modbus address.
     * @return an array containing the modbus slaves currently defined in the
     * application.
     */
    public ModbusSlave[] getModbusSlaves()
    {
        synchronized(knownSlaves)
        {
            ArrayList<ModbusSlave> list = new ArrayList<ModbusSlave>();
            for(ModbusSlave slave : knownSlaves.values())
            {
                if( slave != null )
                {
                    list.add(slave);
                }
            }

            ModbusSlave[] output = new ModbusSlave[0];
            return list.toArray(output);
        }
    }

    /**
     * Count how many modbus slaves are defined in the current project.
     * @return the number of modbus slaves defined in the project.
     */
    public int getModbusSlaveCount()
    {
        synchronized(knownSlaves)
        {
            return knownSlaves.size();
        }
    }

}
