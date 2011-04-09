/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * \page jython
 * \section Jythools, a toolkit class
 * @author nnovic
 */
public class Jythools
{
    public static PyObject getFromFile(String fileName, String objectName)
    {
        System.out.println("jythools: get "+objectName+" from file "+fileName);
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(fileName);
        return interp.get(objectName);
    }
}
