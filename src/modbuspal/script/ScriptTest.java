/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author avincon
 */
public class ScriptTest
{
    public static void test()
    {
        PythonInterpreter interp = new PythonInterpreter();
                interp.exec("import sys");
                interp.exec("print sys");
                interp.set("a", new PyInteger(42));
                interp.exec("print a");
                interp.exec("x = 2+2");
                PyObject x = interp.get("x");
                System.out.println("x: " + x);
    }
}
