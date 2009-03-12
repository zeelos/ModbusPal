/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 * HISTORY:
 * - events "generatorHasEnded" and "generatorHasStarted"
 *   have been moved into "GeneratorListener" instead of "AutomationListener"
 * @author avincon
 */
public interface GeneratorListener
{

    public void generatorHasEnded(Generator gen);

    public void generatorHasStarted(Generator gen);
   
}
