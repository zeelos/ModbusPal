/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 *
 * @author nnovic
 */
public interface ModbusSlavePduProcessor
{

    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist);
    public String getClassName();
}
