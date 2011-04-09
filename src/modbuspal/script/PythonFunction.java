/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import modbuspal.slave.ModbusSlavePduProcessor;

/**
 *
 * @author nnovic
 */
public class PythonFunction
implements ModbusSlavePduProcessor, PythonInstanciatorInterface
{
    private PythonRunner instanciator;

    
    public void install(PythonRunner inst)
    {
        instanciator = inst;
    }

    /**
     * the script should override this method and put
     * its initialization commands in here.
     */
    public void init()
    {
        return;
    }



    public String getClassName()
    {
        if(instanciator!=null)
        {
            return instanciator.getClassName();
        }
        else
        {
            return getClass().getSimpleName();
        }
    }


    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist) {
        return -1;
    }
}
