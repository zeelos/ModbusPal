/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This object contains a list of objects that can be instantiated.
 * @param <T> the class of Instantiable that this manager will hold
 * @author nnovic
 */
public class InstantiableManager<T extends Instantiable<T>>
{
    /**
     * List of listeners that are interested in receiving events from this
     * manager.
     */
    protected ArrayList<InstantiableManagerListener> listeners = new ArrayList<InstantiableManagerListener>();

    /**
     * list of the names given to the instantiables managed by this object.
     */
    protected ArrayList<String> instanciatorNames = new ArrayList<String>();
    
    /**
     * map storing the instantiables, using their names as keys.
     */
    protected HashMap<String,T> instanciators = new HashMap<String,T>();





    /**
     * Check if the specified instanciator exists in the factory.
     * @param name name of the instanciator to find.
     * @return true if the instanciator exists, false otherwise.
     */
    public boolean exists(String name)
    {
        return instanciatorNames.contains(name);
    }


    /**
     * Adds a new instantiable into the manager.
     * @param gi the instantiable to add
     * @return true is added properly
     */
    public boolean add(T gi)
    {
        return add(gi.getClassName(), gi);
    }

    private boolean add(String name, T gi)
    {
        if( exists(name)==true )
        {
            return false;
        }

        instanciatorNames.add(name);
        instanciators.put(name, gi);
        notifyInstanciatorAdded(gi);
        return true;
    }

    /**
     * removes the named instantiable from this manager
     * @param name name of the instantiable to remove
     * @return true if the instantiable has been removed properly
     */
    public boolean remove(String name)
    {
        if( instanciatorNames.contains(name)==false )
        {
            return false;
        }

        instanciatorNames.remove(name);
        instanciators.put(name, null);
        return true;
    }

    /**
     * removes all instantiables in this manager.
     */
    public void clear()
    {
        String list[] = getList();
        for( int i=0; i<list.length; i++ )
        {
            remove( list[i] );
        }
    }

    
    /**
     * Adds an InstantiableManagerListener to the list of listeners
     * @param l the listener to add
     */
    public void addInstanciatorListener(InstantiableManagerListener l)
    {
        if( listeners.contains(l)==false )
            listeners.add(l);
    }

    /**
     * Removes an InstantiableManagerListener from the list of listeners
     * @param l the listener to remove
     */
    public void removeInstanciatorListener(InstantiableManagerListener l)
    {
        if( listeners.contains(l)==true )
            listeners.remove(l);
    }    


    /**
     * Returns the instantiable identified by the specified name
     * @param name the name of the instantiable to return
     * @return the instantiable identified by the specified name
     */
    public Instantiable<T> getInstantiator(String name)
    {
        return instanciators.get(name);
    }

    /**
     * Creates a new instance of an object. This method will find the Instantiable
     * that is identified by "name", and call its "newInstance()" method and 
     * return the resulting object.
     * @param name the name of the instantiable to use in order to create a new object
     * @return a new object of type <T>
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public T newInstance(String name)
    throws InstantiationException, IllegalAccessException
    {
        Instantiable<T> i = getInstantiator(name);
        if( i==null ) return null;
        return i.newInstance();
    }

    /**
     * Returns a list of all the names of the instantiables held by this manager
     * @return array of string containing the names of the instantiables  in this manager
     */
    public String[] getList()
    {
        String list[] = new String[0];
        list=instanciatorNames.toArray(list);
        return list;
    }



    private void notifyInstanciatorAdded(T i)
    {
        for(InstantiableManagerListener l:listeners)
            l.instanciatorAdded(this, i);
    }

    private void notifyInstanciatorRemoved(T i)
    {
        for(InstantiableManagerListener l:listeners)
            l.instanciatorRemoved(this, i);
    }

    /**
     * Create a standardized name for the specified object that implements
     * the Instantiable interface
     * @param i the object for which a standardized name must be returned
     * @return a standardiez name for the instance, made of the class name and the hash code of the object
     */
    public static String makeInstanceName(Instantiable i)
    {
        String hash = Integer.toHexString(i.hashCode());
        return i.getClassName()+"@"+hash;
    }
}
