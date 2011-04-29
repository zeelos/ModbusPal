/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

/**
 * A Instanciator is an object that is able to create object instances of a given class.
 * This interface defines the methods that a class must implement in order to
 * instanciate objects.
 * @param <T> the class of object the will be instantiated by this Instantiable
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
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public T newInstance() throws InstantiationException, IllegalAccessException;

    /**
     * This function will be called by ModbusPal after the instantiation,
     * so that the subclasses can initialize themselves without relying on
     * the constructor. For some subclasses, like the one written in Python
     * for example, cannot rely on the calling of the constructor in order
     * to initialize their members.
     */
    public void init();

    /**
     * This function will be called by ModbusPal when the instance is no longer
     * needed, so that the subclasses can perform the necessary operation to
     * terminate cleanly. The subclasses cannot rely on the calling of the
     * Java destructor, because there no garantee as to if and when it will
     * be called.
     */
    public void reset();
}
