/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import modbuspal.automation.Automation;
import modbuspal.script.ScriptManager;
import modbuspal.slave.ModbusSlave;

/**
 *
 * @author nnovic
 */
public interface ModbusPalListener
{
    public void modbusSlaveAdded(ModbusSlave slave);

    public void modbusSlaveRemoved(ModbusSlave slave);

    public void automationAdded(Automation automation, int index);

    public void automationRemoved(Automation automation);

    public void tilt();

    public void scriptManagerAdded(ScriptManager script);
}
