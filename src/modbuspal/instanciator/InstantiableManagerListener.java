/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

/**
 * object interseted in notifications from an InstantiableManager must
 * implement this interface
 * @author nnovic
 */
public interface InstantiableManagerListener
{
    /**
     * This method will be triggered when a new Instantiable is added to
     * the InstantiableManager.
     * @param factory the InstantiableManager where the new Instantiable has
     * been added
     * @param def  the Instantiable that has been added
     */
    public void instanciatorAdded(InstantiableManager factory, Instantiable def);

    /**
     * This method will be triggered when an Instantiable is remove from
     * the InstantiableManager.
     * @param factory the InstantiableManager from which the Instantiable has
     * been removed
     * @param def the Instantiable that has been removed
     */    
    public void instanciatorRemoved(InstantiableManager factory, Instantiable def);
}
