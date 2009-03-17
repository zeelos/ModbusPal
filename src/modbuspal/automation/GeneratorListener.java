/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 * Specifies the method that a class must implement in order to receive notifications
 * from generators.
 * @author nnovic
 */
public interface GeneratorListener
{

    public void generatorHasEnded(Generator gen);

    public void generatorHasStarted(Generator gen);
   
}
