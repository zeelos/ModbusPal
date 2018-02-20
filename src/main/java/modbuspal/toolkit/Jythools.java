/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * various tools for Jython
 * @author nnovic
 */
public class Jythools
{
    /**
     * Execute the specified python script and gets the named object from the
     * interpreter
     * @param fileName the script file
     * @param objectName the name of the object to obtain
     * @return the designated object, or null
     */
    public static PyObject getFromFile(String fileName, String objectName)
    {
        System.out.println("jythools: get "+objectName+" from file "+fileName);
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(fileName);
        return interp.get(objectName);
    }
}
