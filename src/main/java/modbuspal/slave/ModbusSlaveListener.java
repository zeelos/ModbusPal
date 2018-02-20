/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 * objects interesting in notifications about a modbus slave must implement
 * this interface
 * @author nnovic
 */
public interface ModbusSlaveListener
{

    
    /**
     * This method will be triggered to notify the listener that the specified
     * modbus slave has been enabled or disabled
     * @param slave the modbus slave 
     * @param enabled true if enabled, false if disabled
     */
    public void modbusSlaveEnabled(ModbusSlave slave, boolean enabled);

    /**
     * This method will be triggered to notify the listener that the implementation
     * has been modified for the specified modbus slave
     * @param slave the modbus slave
     * @param impl the new implementation, one of IMPLEMENTATION_MODBUS or
     * IMPLEMENTATION_JBUS
     */
    public void modbusSlaveImplChanged(ModbusSlave slave, int impl);

    /**
     * This method will be triggered to notify the listener that the name of 
     * the specified modbus slave has been modified
     * @param slave the modbus slave
     * @param newName the new name of the slave
     */
    public void modbusSlaveNameChanged(ModbusSlave slave, String newName);

    /**
     * This method will be triggered to notify the listener that the specified
     * modbus slave has been assigned a new ModbusPduProcessor
     * @param slave the modbus slave
     * @param functionCode the function code to which the new pdu processor is assigned
     * @param old the old pdu processor that was assigned to the function code
     * @param mspp the new pdu processor that is now assigned to the function code
     */
    public void modbusSlavePduProcessorChanged(ModbusSlave slave, byte functionCode, ModbusPduProcessor old, ModbusPduProcessor mspp);

    /**
     * This method will be triggered to notify the listener that the reply
     * delays for the specified modbus slave have been changed
     * @param slave the modbus slave
     * @param min the minimum reply delay, in milliseconds
     * @param max the maximum reply delay, in milliseconds
     */
    public void modbusSlaveReplyDelayChanged(ModbusSlave slave, long min, long max);

    /**
     * This method will be triggered to notify the listener that the 
     * error rates for the specified modbus slave have been modified
     * @param slave the modbus slave
     * @param noReplyRate the "no reply" error rate, float value between 0f and 1f.
     */
    public void modbusSlaveErrorRatesChanged(ModbusSlave slave, float noReplyRate);

}
