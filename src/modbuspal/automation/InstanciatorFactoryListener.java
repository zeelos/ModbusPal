/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;


/**
 *
 * @author nnovic
 */
public interface InstanciatorFactoryListener
{
    public void generatorInstanciatorAdded(GeneratorInstanciator def);

    public void generatorInstanciatorRemoved(GeneratorInstanciator def);
}
