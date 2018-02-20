/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * keeps track of how many times the same instance of an object gets
 * added or removed. useful for listing listeners.
 * @param <T> the class for which instances must be counted
 * @author nnovic
 */
public class InstanceCounter<T>
implements Iterable<T>
{

    private HashMap<T,Integer> instances = new HashMap<T,Integer>();

    /**
     * Adds an instance into this instance counter.
     * @param obj the instance to add
     * @return true if the operation is successful.
     */
    public boolean addInstance(T obj)
    {
        Integer count = instances.get(obj);
        if( count!=null )
        {
            count++;
        }
        else
        {
            count = new Integer(1);
        }
        instances.put(obj, count);
        return true;
    }

    /**
     * removes an instance from this instance counter.
     * @param obj the instance to remove
     * @return true if operation successful
     */
    public boolean removeInstance(T obj)
    {
        Integer count = instances.get(obj);
        if( count!=null)
        {
            count--;
            if( count<=0 )
            {
                instances.remove(obj);
            }
            else
            {
                instances.put(obj, count);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Resets the counter associated with the specified instance
     * @param obj the instance for which the counter must resetted
     */
    public void removeAllInstances(T obj)
    {
        instances.remove(obj);
    }


    /**
     * Returns a set containing the instances
     * @return a set containing the instances
     */
    public Set<T> getInstanceSet()
    {
        return instances.keySet();
    }

    @Override
    public Iterator<T> iterator() {
        return getInstanceSet().iterator();
    }

    /**
     * checks if the specified instance is known to this list
     * @param obj the instance to check
     * @return true if the instance is contained in the list, false otherwise
     */
    public boolean contains(T obj)
    {
        return instances.containsKey(obj);
    }


    /**
     * Resets all counters for all instances.
     */
    public void clear()
    {
        instances.clear();
    }

    /**
     * Gets the number of instances counted for the specified object
     * @param obj the object 
     * @return the number of instances of the object
     */
    public int getInstanceCount(T obj)
    {
        Integer count = instances.get(obj);
        if(count==null)
        {
            return 0;
        }
        return count;
    }
}
