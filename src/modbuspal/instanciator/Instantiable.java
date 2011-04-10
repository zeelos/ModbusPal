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
import modbuspal.slave.ModbusPduProcessor;

/**
 * A Instanciator is an object that is able to create object instances of a given class.
 * This interface defines the methods that a class must implement in order to
 * instanciate objects.
 * @author nnovic
 */
public interface Instantiable<T>
{
    /**
     * Get the classname of the instanciated object. This function
     * is useful because some instanciators will create java classes that do not have
     * a good-looking classname.
     * @return the classname of the objects that are instanciated.
     */
    public String getClassName();

    /**
     * Creates a new object instance.
     * @return an instance of the object as defined by this instanciator.
     */
    public T newInstance() throws InstantiationException, IllegalAccessException;

}
