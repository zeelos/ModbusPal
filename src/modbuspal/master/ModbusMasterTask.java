/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import modbuspal.link.ModbusLink;
import modbuspal.slave.ModbusSlaveAddress;

/**
 *
 * @author JMC15
 */
public class ModbusMasterTask 
extends DefaultMutableTreeNode
{
    private static final int REQUEST_TIMEOUT = 1000;
    private static final long DELAY_BETWEEN_REQUESTS = 100;
    public static final String DEFAULT_NAME = "unnamed task";
    
    private String taskName =  DEFAULT_NAME;
    
    void setTaskName(String s) 
    {
        taskName = s;
        setUserObject(s);
    }
    
    
    
    public void run(ModbusLink link)
    {
        while( Thread.interrupted()==false )
        {
            // retrieve list of targets
            for(int i=0; i<getChildCount(); i++)
            {
                ModbusMasterTarget mmt = (ModbusMasterTarget)getChildAt(i);
                
                // enumerate all modbus requests for this target
                for(int j=0; j<mmt.getChildCount(); j++)
                {
                    ModbusMasterRequest mmr = (ModbusMasterRequest)mmt.getChildAt(j);

                    // enumerate all modbus slaves in the target
                    List<ModbusSlaveAddress> targets = mmt.getTargetList();

                    for(ModbusSlaveAddress target : targets)
                    {
                        try 
                        {
                            // send the modbus request to all
                            // the slaves
                            link.execute(target, mmr, REQUEST_TIMEOUT);
                        } 
                        catch (IOException ex) 
                        {
                            Logger.getLogger(ModbusMasterTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        try 
                        {
                            Thread.sleep(DELAY_BETWEEN_REQUESTS);
                        } 
                        catch (InterruptedException ex) 
                        {
                            break;
                        }
                    }   
                }
            }
        }
    }
}
