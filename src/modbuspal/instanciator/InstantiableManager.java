/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


/**
 *
 * @author nnovic
 */
public class InstantiableManager<T extends Instantiable<T>>
{
    /**
     * List of listeners that are interested in receiving events from this
     * manager.
     */
    protected ArrayList<InstantiableManagerListener> listeners = new ArrayList<InstantiableManagerListener>();

    protected ArrayList<String> instanciatorNames = new ArrayList<String>();
    protected HashMap<String,T> instanciators = new HashMap<String,T>();





    /**
     * Check if the specified instanciator exists in the factory.
     * @param className name of the instanciator to find.
     * @return true if the instanciator exists, false otherwise.
     */
    public boolean exists(String name)
    {
        return instanciatorNames.contains(name);
    }


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


    public void clear()
    {
        String list[] = getList();
        for( int i=0; i<list.length; i++ )
        {
            remove( list[i] );
        }
    }

    public String nameOf(T gi)
    {
        Set<Entry<String,T>> set = instanciators.entrySet();
        for( Entry<String,T> entry:set )
        {
            if( entry.getValue()==gi )
            {
                return entry.getKey();
            }
        }
        return null;
    }

    public void addInstanciatorListener(InstantiableManagerListener l)
    {
        if( listeners.contains(l)==false )
            listeners.add(l);
    }

    public void removeInstanciatorListener(InstantiableManagerListener l)
    {
        if( listeners.contains(l)==true )
            listeners.remove(l);
    }    



    public Instantiable<T> getInstantiator(String name)
    {
        return instanciators.get(name);
    }

    public T newInstance(String name)
    throws InstantiationException, IllegalAccessException
    {
        Instantiable<T> i = getInstantiator(name);
        if( i==null ) return null;
        return i.newInstance();
    }


    public String[] getList()
    {
        String list[] = new String[0];
        list=instanciatorNames.toArray(list);
        return list;
    }



    protected void notifyInstanciatorAdded(T i)
    {
        for(InstantiableManagerListener l:listeners)
            l.instanciatorAdded(this, i);
    }

    protected void notifyInstanciatorRemoved(T i)
    {
        for(InstantiableManagerListener l:listeners)
            l.instanciatorRemoved(this, i);
    }

    public static String makeInstanceName(Instantiable i)
    {
        String hash = Integer.toHexString(i.hashCode());
        return i.getClassName()+"@"+hash;
    }
}
