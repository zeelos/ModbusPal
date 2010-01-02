/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import modbuspal.generator.Generator;

/**
 * This interface defines method that a class must implement in order to receive
 * notifications about the status of automations.
 * @author nnovic
 */
public interface AutomationEditionListener
{
    /**
     * this event is triggered when the initial value of an automation
     * is modified.
     * @param source
     * @param init
     */
    public void automationInitialValueChanged(Automation source, double init);

    /**
     * this event is triggered when the "loop" is enabled or disabled.
     * @param source
     * @param enabled
     */
    public void automationLoopEnabled(Automation source, boolean enabled);

    /**
     * this event is triggered when the name of the automation is changed.
     * @param source
     * @param newName
     */
    public void automationNameHasChanged(Automation source, String newName);

    /**
     * this event is triggered when the duration of the step is modified.
     * @param source
     * @param step
     */
    public void automationStepHasChanged(Automation source, double step);

    /**
     * this event is triggered when a generator is added into the automation.
     * @param source
     * @param generator
     * @param index
     */
    public void generatorHasBeenAdded(Automation source, Generator generator, int index);

    /**
     * this event is triggered when a generator is removed from the automation.
     * @param source
     * @param generator
     */
    public void generatorHasBeenRemoved(Automation source, Generator generator);

    /**
     * this event is triggered when two generators are swapped (usually, when
     * the user clicks on the "up" or the "down" buttons).
     * @param source
     * @param g1
     * @param g2
     */
    public void generatorsHaveBeenSwapped(Automation source, Generator g1, Generator g2);
}
