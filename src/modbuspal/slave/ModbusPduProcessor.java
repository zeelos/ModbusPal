/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import modbuspal.instanciator.Instantiable;

/**
 *
 * @author nnovic
 */
public interface ModbusPduProcessor
extends Instantiable<ModbusPduProcessor>
{
    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist);
}
