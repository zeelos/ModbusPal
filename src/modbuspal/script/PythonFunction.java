/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import javax.swing.JPanel;
import modbuspal.slave.ModbusPduProcessor;

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


}
