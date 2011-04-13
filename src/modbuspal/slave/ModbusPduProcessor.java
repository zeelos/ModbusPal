/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import javax.swing.JPanel;
import modbuspal.instanciator.Instantiable;

/**
 *
 * @author nnovic
 */
public interface ModbusPduProcessor
extends Instantiable<ModbusPduProcessor>
{
    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist);

    public JPanel getPduPane();

    //public void save(OutputStream out, boolean withBindings)
    //throws IOException;
}
