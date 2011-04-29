/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import modbuspal.slave.ModbusSlave;

/**
 *
 * @author nnovic
 */
public abstract class ModbusPalProject2
{
    final private ModbusSlave[] knownSlaves = new ModbusSlave[ModbusConst.MAX_MODBUS_SLAVE];

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
    protected ModbusSlave getModbusSlave(int id, boolean createIfNotExist)
    {
        if( knownSlaves[id]!=null )
        {
            return knownSlaves[id];
        }
        else if( createIfNotExist==true )
        {
            setModbusSlave( id, new ModbusSlave(id) );
            return knownSlaves[id];
        }
        else
        {
            return null;
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
    protected ModbusSlave setModbusSlave(int id, ModbusSlave s)
    {
        ModbusSlave old = knownSlaves[id];
        knownSlaves[id]=s;
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
    public ModbusSlave getModbusSlave(int id)
    {
        return getModbusSlave(id,false);
    }

    /**
     * Get all the slaves indexed by their modbus address.
     * @return an array containing the modbus slaves currently defined in the
     * application.
     */
    public ModbusSlave[] getModbusSlaves()
    {
        return knownSlaves.clone();
    }

    /**
     * Count how many modbus slaves are defined in the current project.
     * @return the number of modbus slaves defined in the project.
     */
    public int getModbusSlaveCount()
    {
        int count = 0;
        for(int i=0; i<knownSlaves.length; i++)
        {
            if( knownSlaves[i]!=null )
            {
                count++;
            }
        }
        return count;
    }

}
