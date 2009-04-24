/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import modbuspal.script.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.binding.Binding;
import modbuspal.generator.Generator;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author nnovic
 */
public class PythonRunner
extends ScriptRunner
{
    private PyObject pythonClass = null;

    public PythonRunner(File file)
    {
        super(file);
    }



    @Override
    public String getFileExtension()
    {
        return "py";
    }



    private PyObject executeInstanciator()
    throws FileNotFoundException, IOException
    {
        FileInputStream in = new FileInputStream(scriptFile);
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile(in);
        in.close();
        return interp.get( getClassName() );
    }


    @Override
    public void execute()
    {
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(scriptFile);
            PythonInterpreter interp = new PythonInterpreter();
            interp.execfile(in);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        if( in != null )
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    @Override
    public Generator newGenerator()
    {
        if( pythonClass==null )
        {
            try
            {
                pythonClass = executeInstanciator();
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            catch (IOException ex)
            {
                Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        PyObject instance = pythonClass.__call__();
        PythonGenerator gen = (PythonGenerator)instance.__tojava__( PythonGenerator.class );
        gen.install(this);
        gen.init();
        return gen;
    }

    @Override
    public Binding newBinding()
    {
        if( pythonClass==null )
        {
            try
            {
                pythonClass = executeInstanciator();
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            catch (IOException ex)
            {
                Logger.getLogger(PythonRunner.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        PyObject instance = pythonClass.__call__();
        PythonBinding bd = (PythonBinding)instance.__tojava__( PythonBinding.class );
        bd.install(this);
        bd.init();
        return bd;
    }
}

