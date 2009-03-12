/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 * HISTORY:
 * - event "generatorHasBeenAdded" is now attached to AutomationStateListener
 *   instead of "GeneratorListener"
 * @author avincon
 */
public interface AutomationStateListener
{

    public void automationHasEnded(Automation source);

    public void automationHasStarted(Automation source);

    public void automationInitialValueChanged(Automation aThis, double init);

    public void automationLoopEnabled(Automation source, boolean enabled);

    public void automationNameHasChanged(Automation source, String newName);

    public void automationStepHasChanged(Automation source, double step);

    public void generatorHasBeenAdded(Automation source, Generator generator, int index);

    public void generatorHasBeenRemoved(Automation source, Generator generator);

    public void generatorsHaveBeenSwapped(Automation source, Generator g1, Generator g2);
}
