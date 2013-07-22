/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JMC15
 */
public class ModbusMasterTask 
extends DefaultMutableTreeNode
{
    public static final String DEFAULT_NAME = "unnamed task";
    
    private String taskName =  DEFAULT_NAME;
    
    void setTaskName(String s) 
    {
        taskName = s;
        setUserObject(s);
    }
    
}
