/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author avincon
 */
public class InstanceCounter<T>
implements Iterable<T>
{

    private HashMap<T,Integer> instances = new HashMap<T,Integer>();

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

    public void removeAllInstances(T obj)
    {
        instances.remove(obj);
    }


    public Set<T> getInstanceSet()
    {
        return instances.keySet();
    }

    public Iterator<T> iterator() {
        return getInstanceSet().iterator();
    }

    public boolean contains(T obj)
    {
        return instances.containsValue(obj);
    }


    public void clear()
    {
        instances.clear();
    }

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
