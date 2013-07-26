/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import javax.swing.tree.DefaultMutableTreeNode;
import modbuspal.slave.ModbusSlaveAddress;

/**
 *
 * @author JMC15
 */
public class ModbusMasterTarget
extends DefaultMutableTreeNode
{
    private String targetName;
    private ModbusSlaveAddress[] targetList;
            
    void setTargetName(String s) 
    {
        targetName = s;
        setUserObject(targetName);
    }

    void setTargetList(ModbusSlaveAddress[] a) 
    {
        targetList = a;
    }
    
}
