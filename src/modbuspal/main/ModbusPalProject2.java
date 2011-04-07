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
            notifySlaveRemoved(null);
        }

        return old;
    }

    protected abstract void notifySlaveRemoved(ModbusSlave slave);
    protected abstract void notifySlaveAdded(ModbusSlave slave);
    
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
