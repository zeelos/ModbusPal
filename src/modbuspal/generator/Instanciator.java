/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A Instanciator is an object that is able to create Generator instances.
 * This interface defines the methods that a class must implement in order to
 * instanciate generators. There are two families of instanciators:
 * - the ClassInstanciator family instanciates the predefined generators (like
 * LinearGenerator and RandomGenerator).
 * - the ScriptInstanciator family instanciates generators from scripts. 
 * @author nnovic
 */
public interface Instanciator
{
    /**
     * Get the classname of the instanciated generators. This function
     * is useful because some instanciators will create java classes that do not have
     * a good-looking classname.
     * @return the classname of the generators that are instanciated.
     */
    public String getClassName();

    /**
     * Creates a new generator instance.
     * @return an instance of the generator as defined by this instanciator.
     */
    public Generator newInstance();

    /**
     * writes the instanciator's description into the output stream, making
     * all paths relative to projectFile whenever possible.
     * @param out
     * @param projectFile
     */
    public void save(OutputStream out, File projectFile) throws IOException;
}
