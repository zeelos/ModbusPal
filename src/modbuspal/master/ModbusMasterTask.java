/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
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
    private TreeSelectionModel treeSelectionModel = null;
    
    
    public ModbusMasterTask()
    {
        
    }
    
    
    void attach(JTree t)
    {
        treeSelectionModel = t.getSelectionModel();
    }
    
    void detach()
    {
        treeSelectionModel = null;
    }
    
    void setTaskName(String s) 
    {
        taskName = s;
        setUserObject(s);
    }
    
    private void select(DefaultMutableTreeNode tn)
    {
        try
        {
            synchronized(treeSelectionModel)
            {
                TreePath tp = new TreePath(tn.getPath());
                treeSelectionModel.addSelectionPath( tp );
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void unselect(DefaultMutableTreeNode tn)
    {
        try
        {
            synchronized(treeSelectionModel)
            {
            TreePath tp = new TreePath(tn.getPath());
            treeSelectionModel.removeSelectionPath( tp );
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }    
    
    
    private void executeRequest(ModbusLink link, ModbusMasterRequest mmr, ModbusMasterTarget mmt)
    throws InterruptedException
    {
        if( mmr instanceof ModbusMasterDelay )
        {
            int delayMs = ((ModbusMasterDelay)mmr).getDelay();
            Thread.sleep( delayMs );

        }
        else

        {
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

                Thread.sleep(DELAY_BETWEEN_REQUESTS);
            } 
        }        
    }
    
    
    private void modbusMasterTaskBody(ModbusLink link) 
    throws InterruptedException
    {
        // retrieve list of targets
        for(int i=0; i<getChildCount(); i++)
        {
            ModbusMasterTarget mmt = (ModbusMasterTarget)getChildAt(i);

            // enumerate all modbus requests for this target
            for(int j=0; j<mmt.getChildCount(); j++)
            {
                ModbusMasterRequest mmr = (ModbusMasterRequest)mmt.getChildAt(j);                    
                select(mmr);

                try
                {
                    executeRequest(link, mmr, mmt);
                }
                finally
                {
                    unselect(mmr);
                }
            }
        }        
    }
    
    
    public void run(ModbusLink link)
    {
        while( Thread.interrupted()==false )
        {
            try
            {
                modbusMasterTaskBody(link);
            }
            catch(InterruptedException ex)
            {
                break;
            }
        }
    }
}
