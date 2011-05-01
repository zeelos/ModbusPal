/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.io.IOException;
import modbuspal.main.ModbusRequest;

/**
 * The interface that any link must implement
 * @author nnovic
 */
public interface ModbusLink
{
    /**
     * Starts the ModbusLink. Usually creates a thread.
     * @param l the modbus link listener that will receive notifications
     * for the events of this modbus link.
     * @throws IOException
     */
    public void start(ModbusLinkListener l) throws IOException;

    /**
     * Stops the ModbusLink. Usually stops the thread created by start().
     */
    public void stop();

    /**
     * For future use. When modbuspal will be able to operate as a MASTER.
     * @param req 
     */
    public void execute(ModbusRequest req);
}
