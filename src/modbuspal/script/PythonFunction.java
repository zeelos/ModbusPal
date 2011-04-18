/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.slave.ModbusPduProcessor;
import org.w3c.dom.NodeList;

/**
 *
 * @author avincon
 */
public class PythonFunction
implements ModbusPduProcessor
{

    @Override
    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist) {
        return -1;
    }

    @Override
    public JPanel getPduPane()
    {
        return null;
    }

    public String getClassName() {
        return getClass().getSimpleName();
    }

    /**
     * the script should override this method and put
     * its initialization commands in here.
     */
    public void init()
    {
    }

    /**
     * the script should override this method and put
     * its reset commands in here.
     */
    public void reset()
    {
    }

    
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

    @Deprecated
    public void saveSettings(OutputStream out) throws IOException {
    }

    @Override
    public void loadPduProcessorSettings(NodeList list) {
        loadSettings(list);
    }


    @Deprecated
    public void loadSettings(NodeList list) {
    }


}
