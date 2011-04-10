/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

/**
 *
 * @author nnovic
 */
public interface InstantiableManagerListener
{
    public void instanciatorAdded(InstantiableManager factory, Instantiable def);

    public void instanciatorRemoved(InstantiableManager factory, Instantiable def);
}
