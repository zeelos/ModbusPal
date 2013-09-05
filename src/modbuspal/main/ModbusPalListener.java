/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import modbuspal.automation.Automation;
import modbuspal.master.ModbusMasterTask;
import modbuspal.slave.ModbusSlave;

/**
 * An object interested in ModbusPal related events should implement
 * this interface.
 * @author nnovic
 */
public interface ModbusPalListener
{
    /**
     * This method is triggered when a new MODBUS slave is added into the project.
     * @param slave the slave that has been added
     */
    public void modbusSlaveAdded(ModbusSlave slave);

    /**
     * This method is triggered when a MODBUS slave is removed from the project.
     * @param slave the slave being removed
     */
    public void modbusSlaveRemoved(ModbusSlave slave);

    /**
     * This method is triggered when an automation is added into the project.
     * @param automation the automation that has been added
     * @param index the index of this automation into the list of automations
     */
    public void automationAdded(Automation automation, int index);

    /**
     * This method is triggered when an automation is removed from the project.
     * @param automation the automation being removed
     */
    public void automationRemoved(Automation automation);

    /**
     * This method is triggered when a PDU has been successfully processed
     * by ModbusPal.
     */
    public void pduProcessed();

    /**
     * This method is triggered when a PDU has been processed and the result
     * is an exception reply.
     */
    public void pduException();

    /**
     * This method is triggered when a PDU has been received but no reply is
     * given to it.
     */
    public void pduNotServiced();

    public void modbusMasterTaskRemoved(ModbusMasterTask mmt);

    public void modbusMasterTaskAdded(ModbusMasterTask mmt);
}
