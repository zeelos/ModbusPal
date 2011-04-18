/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 *
 * @author nnovic
 */
public interface ModbusSlaveListener
{

    public void modbusSlaveEnabled(ModbusSlave slave, boolean enabled);

    public void modbusSlaveImplChanged(ModbusSlave slave, int impl);

    public void modbusSlaveNameChanged(ModbusSlave slave, String newName);

    public void modbusSlavePduProcessorChanged(ModbusSlave slave, byte functionCode, ModbusPduProcessor old, ModbusPduProcessor mspp);

    public void modbusSlaveReplyDelayChanged(ModbusSlave slave, long min, long max);

}
