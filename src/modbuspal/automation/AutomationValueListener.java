/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 *
 * @author avincon
 */
public interface AutomationValueListener
{
    public void automationValueHasChanged(Automation source, double value);
}
