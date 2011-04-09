/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.IOException;
import java.io.OutputStream;
import modbuspal.generator.Generator;
import org.w3c.dom.NodeList;

/**
 * If you create a generator with Python, you have to subclass PythonGenerator
 * instead of Generator. PythonGenerator slightly modifies some behaviors of Generator
 * in order to ease integration of Python scripts into the Java API.
 * - The init() method will hold the initialization code
 * - getClassName() is modified because Java classes created from Python scripts have
 *   strange names.
 * - getValue() is no more abstract (the default implementation always returns 0)
 * - setIcon() becomes public because Python doesn't handle protected methods, and
 *   adds the ability to search for image files into the directory of the script.
 * @author nnovic
 */
public class PythonGenerator
extends Generator
implements PythonInstanciatorInterface
{
    private PythonRunner instanciator;
    
    /**
     * If your Python generator needs to make some initialization, you have to
     * override this method and put your initialization code into it. Because of the
     * complexity of Java/Python integration, the Java constructor nor the __init__()
     * function are garanteed to work.
     * The default implementation does nothing.
     */
    public void init()
    {
        return;
    }

    /**
     * Returns the "human readable" class name for this Python generator.
     * The Java class resulting from the interpretation of the Python generator
     * script would not have a comprehensive class name.
     * @return the "human readable" class name of this generator.
     */
    @Override
    public String getClassName()
    {
        return instanciator.getClassName();
    }



    /**
     * Subclasses have to override this method in order to generate a dynamic
     * value. The current time is passed in argument so that you can easily create
     * functions depending on the time. The time is provided in seconds, starting from
     * the moment when the automation has been started.
     * The default implementation always returns zero.
     * @param time current time, in seconds.
     * @return dynamic value created by this generator.
     */
    @Override
    public double getValue(double time)
    {
        return 0.0;
    }

    
    /**
     * The setIcon method has to be exposed with the "public" modifier,
     * because Jython doesn't handle protected methods.
     * @param iconUrl
     */
    @Override
    public boolean setIcon(String iconUrl)
    {
        // try the standard method:
        if( super.setIcon(iconUrl)==false )
        {
            // if standard method failed, try this
            String fullpath = instanciator.scriptFile.getAbsolutePath();
            String filename = instanciator.scriptFile.getName();
            iconUrl = fullpath.replace(filename, iconUrl);
            return super.setIcon(iconUrl);
        }
        return false;
    }


    public void install(PythonRunner inst)
    {
        instanciator = inst;
    }


    /**
     * If the generator should save parameters, for example when a project file
     * is saved, you have to override this method in order to write those
     * parameters into the provided output stream, formatted in XML.
     * This method is overriden in order to make it public, because Python doesn't
     * handle protected methods. It doesn't do anything.
     * @param out the output stream to write into.
     */
    @Override
    public void saveSettings(OutputStream out)
    throws IOException
    {
        return;
    }


    /**
     * If the generator should load parameters, for example when a project file
     * is loaded, you have to override this method in order to process those
     * parameters. Each item in the provided node list is a node that defines the
     * parameters of this generator.
     * This method is overriden in order to make it public, because Python doesn't
     * handle protected methods. It doesn't do anything.
     * @param childNodes list of nodes describing the parameters of this generator.
     */
    @Override
    public void loadSettings(NodeList childNodes)
    {
        return;
    }
}
