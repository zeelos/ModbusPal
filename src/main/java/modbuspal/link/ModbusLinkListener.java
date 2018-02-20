/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

/**
 * objects interested in events from a ModbusLink must implement this interface
 * @author nnovic
 */
public interface ModbusLinkListener
{
    /**
     * This method will be triggered when the ModbusLink gets broken. For example,
     * when the TCP/IP socket is unexpectedly closed.
     */
    public void linkBroken();
}
