/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 * This interface defines method that a class must implement in order to receive
 * notifications about the values of automations.
 * @author nnovic
 */
public interface AutomationValueListener
{
    /**
     * this event is triggered when the current value of the automation is
     * modified.
     * @param source
     * @param value
     */
    public void automationValueHasChanged(Automation source, double value);
}
