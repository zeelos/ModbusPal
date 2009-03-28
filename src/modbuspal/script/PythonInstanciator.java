/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import modbuspal.automation.Generator;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author avincon
 */
public class PythonInstanciator
extends ScriptInstanciator
{
    private PyObject pythonClass = null;

    public PythonInstanciator(File file)
    throws FileNotFoundException, IOException
    {
        super(file);
        pythonClass = executeScript();
    }



    @Override
    public String getFileExtension()
    {
        return "py";
    }



    private PyObject executeScript()
    throws FileNotFoundException, IOException
    {
        FileInputStream in = new FileInputStream(scriptFile);
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(in);
        in.close();
        return interp.get( getClassName() );
    }


    
    @Override
    public Generator newInstance() 
    {
        PyObject instance = pythonClass.__call__();
        //PyObject instance =  new PyObject(pythonType);
        PythonGenerator gen = (PythonGenerator)instance.__tojava__(PythonGenerator.class);
        gen.install(this);
        return gen;
    }
}

