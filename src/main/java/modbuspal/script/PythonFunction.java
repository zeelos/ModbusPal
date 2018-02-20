/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.master.ModbusMasterRequest;
import modbuspal.slave.ModbusPduProcessor;
import modbuspal.slave.ModbusSlaveAddress;
import org.w3c.dom.NodeList;

/**
 * the class that should be used in python script to create new ModbusPduProcessors
 * @author nnovic
 */
public class PythonFunction
implements ModbusPduProcessor
{

    @Override
    public int processPDU(byte functionCode, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist) {
        return -1;
    }

    @Override
    public int buildPDU(ModbusMasterRequest mmr, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist) {
        return -1;
    }
        
    @Override
    public boolean processPDU(ModbusMasterRequest mmr, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist) {
        return false;
    }

    @Override
    public JPanel getPduPane()
    {
        return null;
    }

    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

    /**
     * the script should override this method and put
     * its initialization commands in here.
     */
    @Override
    public void init()
    {
    }

    /**
     * the script should override this method and put
     * its reset commands in here.
     */
    @Override
    public void reset()
    {
    }

    
    @Override
    public ModbusPduProcessor newInstance()
    throws InstantiationException, IllegalAccessException
    {
        PythonFunction pf = getClass().newInstance();
        return pf;
    }

    @Override
    public void savePduProcessorSettings(OutputStream out) throws IOException {
        saveSettings(out);
    }

    /**
     * @see #savePduProcessorSettings(java.io.OutputStream) 
     * @param out the output stream where the settings must be saved
     * @throws IOException
     * @deprecated ambiguous name
     */
    @Deprecated
    public void saveSettings(OutputStream out) throws IOException {
    }

    @Override
    public void loadPduProcessorSettings(NodeList list) {
        loadSettings(list);
    }

  /**
     * @see #loadPduProcessorSettings(org.w3c.dom.NodeList) 
     * @param list the nodes containing the settings to load
     * @deprecated ambiguous name
     */
    @Deprecated
    public void loadSettings(NodeList list) {
    }

}
