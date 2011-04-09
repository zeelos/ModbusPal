/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.binding.Binding;
import modbuspal.generator.Generator;
import modbuspal.main.ModbusPalProject;
import modbuspal.slave.ModbusSlavePduProcessor;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * \page jython How to use Jython in ModbusPal
 *
 * \section API
 *
 * \subsection Pre-defined variables
 *
 * When ModbusPal starts executing a Jython scripts, a set of pre-defined
 * variables are pushed into the Python interpreter. The goal of those variables
 * is to provide access to some information that would be otherwise difficult
 * to obtain.
 *
 * The following variables are pre-defined when the Jython scripts is
 * executed:
 * - "mbp_script_path" is a String containing the full path of the current script file.
 * - "mbp_script_directory" is a String containing only the directory which contains
 *   the current script file.
 * - "mbp_script_file" is a File object pointing the current script's file.
 * 
 * @author nnovic
 */
public class PythonRunner
extends ScriptRunner
{
    private PyObject pythonClass = null;
    private final ModbusPalProject modbusPalProject;

    public PythonRunner(ModbusPalProject mpp, File file)
    {
        super(file);
        modbusPalProject = mpp;
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


    private void initEnvironment(PythonInterpreter pi)
    {
        // init vars for script file
        pi.set("mbp_script_path", scriptFile.getPath() );
        pi.set("mbp_script_directory", scriptFile.getParent() );
        pi.set("mbp_script_file", scriptFile );

        // init vars for modbuspal project
        pi.set("ModbusPal", modbusPalProject);
    }


    @Override
    public void execute()
    {
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(scriptFile);
            PythonInterpreter interp = new PythonInterpreter();
            initEnvironment(interp);
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

    @Override
    public ModbusSlavePduProcessor newFunction()
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
        PythonFunction pf = (PythonFunction)instance.__tojava__( PythonFunction.class );
        pf.install(this);
        pf.init();
        return pf;
    }


    @Override
    protected void interrupt()
    {
        //xxxx
    }
}

