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
{

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

    
    @Override
    public Generator newInstance()
    throws InstantiationException, IllegalAccessException
    {
        PythonGenerator pg = (PythonGenerator)super.newInstance();
        pg.init();
        return pg;
    }

    @Override
    public void saveGeneratorSettings(OutputStream out) throws IOException {
        saveSettings(out);
    }

    /**
     * @see #saveGeneratorSettings(java.io.OutputStream) 
     * @param out the output stream where the settings must be saved
     * @throws IOException
     * @deprecated ambiguous name
     */
    @Deprecated
    public void saveSettings(OutputStream out) throws IOException {
    }

    @Override
    public void loadGeneratorSettings(NodeList list) {
        loadSettings(list);
    }

    /**
     * @see #loadGeneratorSettings(org.w3c.dom.NodeList) 
     * @param list the nodes containing the settings to load
     * @deprecated ambiguous name
     */
    @Deprecated
    public void loadSettings(NodeList list) {
    }
}
