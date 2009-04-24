/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

/**
 *
 * @author nnovic
 */
public interface InstanciatorListener
{
    public void instanciatorAdded(InstanciatorManager factory, Instanciator def);

    public void instanciatorRemoved(InstanciatorManager factory, Instanciator def);
}
