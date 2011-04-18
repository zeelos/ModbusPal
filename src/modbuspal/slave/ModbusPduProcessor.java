/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.instanciator.Instantiable;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
public interface ModbusPduProcessor
extends Instantiable<ModbusPduProcessor>
{
    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist);

    public JPanel getPduPane();

    /**
     * saves the parameter of the instance into the provided output stream,
     * in XML format.
     * @param out
     * @throws IOException
     */
    public void savePduProcessorSettings(OutputStream out) throws IOException;

    /**
     * XML nodes that may contain settings that were previously saved
     * with saveSettings()
     * @param list
     */
    public void loadPduProcessorSettings(NodeList list);

}
