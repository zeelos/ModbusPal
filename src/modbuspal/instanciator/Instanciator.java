/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import modbuspal.binding.Binding;
import modbuspal.generator.Generator;
import modbuspal.slave.ModbusSlavePduProcessor;

/**
 * A Instanciator is an object that is able to create object instances of a given class.
 * This interface defines the methods that a class must implement in order to
 * instanciate objects.
 * @author nnovic
 */
public interface Instanciator
{
    /**
     * Get the classname of the instanciated object. This function
     * is useful because some instanciators will create java classes that do not have
     * a good-looking classname.
     * @return the classname of the objects that are instanciated.
     */
    public String getClassName();

    /**
     * writes the instanciator's description into the output stream, making
     * all paths relative to projectFile whenever possible.
     * @param out
     * @param projectFile
     */
    public void save(OutputStream out, File projectFile) throws IOException;

    /**
     * Creates a new object instance.
     * @return an instance of the object as defined by this instanciator.
     */
    public Generator newGenerator();

    /**
     * Creates a new object instance.
     * @return an instance of the object as defined by this instanciator.
     */
    public Binding newBinding();

    /**
     * Creates a new object instance.
     * @return an instance of the object as defined by this instanciator.
     */
    public ModbusSlavePduProcessor newFunction();

    /**
     * Returns the complete path of the script file.
     * @return complete path of the script file.
     */
    public String getPath();
}
