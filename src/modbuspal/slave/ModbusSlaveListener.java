/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 *
 * @author avincon
 */
public interface ModbusSlaveListener
{

    public void modbusSlaveEnabled(ModbusSlave slave, boolean enabled);

    public void modbusSlaveImplChanged(ModbusSlave slave, int impl);

    public void modbusSlaveNameChanged(ModbusSlave slave, String newName);

}
