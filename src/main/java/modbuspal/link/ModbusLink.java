/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.io.IOException;
import modbuspal.master.ModbusMasterRequest;
import modbuspal.slave.ModbusSlaveAddress;

/**
 * The interface that any link must implement
 * @author nnovic
 */
public interface ModbusLink
{
    /**
     * Starts the ModbusLink in order to operate as a slave simulator. 
     * Usually creates a thread that is waiting for incoming requests.
     * @param l the modbus link listener that will receive notifications
     * for the events of this modbus link.
     * @throws IOException
     */
    public void start(ModbusLinkListener l) throws IOException;
    
    /**
     * Starts the ModbusLink in order to operate as a MODBUS master.
     * Usually opens a client connection. Requests are then managed
     * by the execute method.
     * @param l
     * @throws IOException 
     */
    public void startMaster(ModbusLinkListener l) throws IOException;

    /**
     * Stops the ModbusLink. Usually stops the thread created by start().
     */
    public void stop();

    /**
     * Stops the ModbusLink. Usually stops the thread created by startMaster().
     */
    public void stopMaster();
    
    /**
     * For future use. When modbuspal will be able to operate as a MASTER.
     * @param req 
     */
    public void execute(ModbusSlaveAddress dst, ModbusMasterRequest req, int timeout) throws IOException;
}
