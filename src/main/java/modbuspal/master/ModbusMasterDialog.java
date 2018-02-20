/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModbusMasterDialog.java
 *
 * Created on 4 janv. 2009, 12:47:46
 */

package modbuspal.master;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import modbuspal.automation.Automation;
import modbuspal.link.ModbusLink;
import modbuspal.main.AddSlaveDialog;
import modbuspal.main.ModbusPalListener;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalPane;
import modbuspal.slave.ModbusSlave;
import modbuspal.slave.ModbusSlaveAddress;
import modbuspal.toolkit.GUITools;

/**
 *
 * @author nnovic
 */
public class ModbusMasterDialog
extends javax.swing.JDialog
implements ModbusPalListener
{
    private final ModbusPalPane modbusPalPane;
    private ModbusPalProject modbusPalProject;
    private ModbusMasterRoot modbusMasterRoot;
    private DefaultTreeModel mmTreeModel;
    private ArrayList<Thread> threads;
    private boolean isRunning = false;
    
    /** Creates new form ModbusMasterDialog */
    public ModbusMasterDialog(ModbusPalPane p)
    {
        modbusPalPane = p;
        setProject(p.getProject());
        threads = new ArrayList<Thread>();
        initComponents();
        initTree();
    }

    public void setProject(ModbusPalProject p)
    {
        if( modbusPalProject!=null)
        {
            modbusPalProject.removeModbusPalListener(this);
        }
        modbusPalProject = p;
        if(modbusPalProject!=null)
        {
            modbusPalProject.addModbusPalListener(this);
        }
    }
    
    private void initTree()
    {
        // remove all tree nodes
        jTree1.removeAll();
        
        // add the root
        modbusMasterRoot = new ModbusMasterRoot();
        mmTreeModel = new DefaultTreeModel(modbusMasterRoot);
        
        jTree1.setModel(mmTreeModel);
        
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }
    
    
    
    
    private void addNewTaskWizard()
    {
        NewTaskDialog ntd = new NewTaskDialog(null, true);
        ntd.setVisible(true);
        if( ntd.isOK() == true )
        {
            // obtain data from the form
            String taskName = ntd.getTaskName();
            
            // create new task
            ModbusMasterTask mmt = new ModbusMasterTask();
            mmt.setTaskName(taskName);
            
            modbusPalProject.addModbusMasterTask(mmt);
        }
    }    
    
    
    
    private void modifyTask(ModbusMasterTask mmt)
    {
        NewTaskDialog ntd = new NewTaskDialog(null, true);
        ntd.initializeWith(mmt);
        ntd.setVisible(true);
        if( ntd.isOK() == true )
        {
            // obtain data from the form
            String taskName = ntd.getTaskName();
            
            mmt.setTaskName(taskName);

            mmTreeModel.nodeChanged(mmt);
        }
    }
    
    
    private void removeTask(ModbusMasterTask mmt)
    {
        // remove all targets that are children of this task
        for(int i=0; i<mmt.getChildCount(); i++)
        {
            ModbusMasterTarget child = (ModbusMasterTarget)mmt.getChildAt(i);
            removeTarget(child);
        }
        
        // remove the task
        modbusPalProject.removeModbusMasterTask(mmt);
    }
    

    
    
    private void addNewTargetWizard(ModbusMasterTask parent)
    {
        // create dialog for target selection
        AddSlaveDialog asd = new AddSlaveDialog("Target slave(s)");
        asd.setVisible(true);
        if( asd.isAdded() == false )
        {
            return;
        }
        
        // obtain data from the form
        ModbusSlaveAddress[] targets = asd.getTargetList();
        String targetsAsString = asd.getTargetListAsText();
        String targetName = asd.getTargetName();
        
        // create new target node in the tree
        ModbusMasterTarget mmt = new ModbusMasterTarget();
        mmt.setTargetName(targetName);
        mmt.setTargetList(targets);
        mmt.setTargetListAsText(targetsAsString);
        
        // add the new node in the tree
        mmTreeModel.insertNodeInto(mmt, parent, parent.getChildCount());
        jTree1.setSelectionPath( new TreePath( mmt.getPath() ) );
    }
    
    
    private void modifyTarget(ModbusMasterTarget mmt)
    {
        AddSlaveDialog asd = new AddSlaveDialog("Target slave(s)");
        asd.initializeWith(mmt);
        asd.setVisible(true);
        if( asd.isAdded() == false )
        {
            return;
        }        
        
         // obtain data from the form
        ModbusSlaveAddress[] targets = asd.getTargetList();
        String targetsAsString = asd.getTargetListAsText();
        String targetName = asd.getTargetName();
        
        // create new target node in the tree
        mmt.setTargetName(targetName);
        mmt.setTargetList(targets);
        mmt.setTargetListAsText(targetsAsString);
        
        mmTreeModel.nodeChanged(mmt);
    }
    
    
    private void removeTarget(ModbusMasterTarget mmt)
    {
        // remove all request that are children of this target
        for(int i=0; i<mmt.getChildCount(); i++)
        {
            ModbusMasterRequest child = (ModbusMasterRequest)mmt.getChildAt(i);
            removeRequest(child);
        }
        
        // remove the target
        mmTreeModel.removeNodeFromParent(mmt);        
    }
    
    private void addNewRequestWizard( ModbusMasterTarget parent )
    {
        // create dialog for request selection
        ModbusRequestDialog mrd = new ModbusRequestDialog();
        mrd.setVisible(true);
        if( mrd.isOK() == false )
        {
            return;
        }
        
        ModbusMasterRequest mmr = mrd.getRequest();
        mmTreeModel.insertNodeInto(mmr, parent, parent.getChildCount());
        jTree1.setSelectionPath( new TreePath( mmr.getPath() ) );
    }
    
    
    private void modifyRequest(ModbusMasterRequest mmr)
    {
        ModbusRequestDialog mrd = new ModbusRequestDialog();
        mrd.initializeWith(mmr);
        mrd.setVisible(true);
        if( mrd.isOK() == false )
        {
            return;
        }                
        
        ModbusMasterRequest newRequest = mrd.getRequest();
        
        MutableTreeNode parent = (MutableTreeNode)mmr.getParent();
        int index = mmTreeModel.getIndexOfChild(parent, mmr);
        mmTreeModel.removeNodeFromParent(mmr);
                
        mmTreeModel.insertNodeInto(newRequest, parent, index);
        jTree1.setSelectionPath( new TreePath( newRequest.getPath() ) );
    }
    
    
    
    private void removeRequest(ModbusMasterRequest mmr)
    {
        mmTreeModel.removeNodeFromParent(mmr);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        modifyButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ModbusPal Master");
        setMinimumSize(new java.awt.Dimension(300, 300));

        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        addButton.setText("Add task...");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(addButton);

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(removeButton);

        modifyButton.setText("Modify");
        modifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(modifyButton);
        modifyButton.getAccessibleContext().setAccessibleName("Modify");

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        
        TreePath selection = jTree1.getSelectionPath();
        if( selection != null )
        {
            Object lastCpnt = selection.getLastPathComponent();
            if( lastCpnt instanceof ModbusMasterRoot )
            {
                addNewTaskWizard();
            }
            else if( lastCpnt instanceof ModbusMasterTask )
            {
                addNewTargetWizard( (ModbusMasterTask)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterTarget )
            {
                addNewRequestWizard( (ModbusMasterTarget)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterRequest )
            {
                ModbusMasterRequest req = (ModbusMasterRequest)lastCpnt;
                addNewRequestWizard( (ModbusMasterTarget)req.getParent() );
            }
        }
        else
        {
            addNewTaskWizard();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        
        Object o = null;
        TreePath selection = evt.getNewLeadSelectionPath();
        
        if( selection != null )
        {
            o = selection.getLastPathComponent();
        }
        
        if( o == null )
        {
            addButton.setText("Add task...");
        }
        else
        {
            if( o instanceof ModbusMasterTask )
            {
                addButton.setText("Add target...");
            }
            else if( o instanceof ModbusMasterTarget )
            {
                addButton.setText("Add request...");
            }
            else if(o instanceof ModbusMasterRequest )
            {
                addButton.setText("Add request...");
            }
            else
            {
                addButton.setText("Add task...");
            }
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        
        
        TreePath selection = jTree1.getSelectionPath();
        if( selection != null )
        {
            Object lastCpnt = selection.getLastPathComponent();
            if( lastCpnt instanceof ModbusMasterTask )
            {
                removeTask( (ModbusMasterTask)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterTarget )
            {
                removeTarget( (ModbusMasterTarget)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterRequest )
            {
                ModbusMasterRequest req = (ModbusMasterRequest)lastCpnt;
                removeRequest(req);
            }
        }       
        
    }//GEN-LAST:event_removeButtonActionPerformed

    private void modifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyButtonActionPerformed
        
        TreePath selection = jTree1.getSelectionPath();
        if( selection != null )
        {
            Object lastCpnt = selection.getLastPathComponent();
            if( lastCpnt instanceof ModbusMasterTask )
            {
                modifyTask( (ModbusMasterTask)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterTarget )
            {
                modifyTarget( (ModbusMasterTarget)lastCpnt );
            }
            else if( lastCpnt instanceof ModbusMasterRequest )
            {
                ModbusMasterRequest req = (ModbusMasterRequest)lastCpnt;
                modifyRequest(req);
            }
        }    
        
    }//GEN-LAST:event_modifyButtonActionPerformed

    
    public boolean isRunning()
    {
        return isRunning;
    }
    
    
    private void disableDialog()
    {
        GUITools.setAllEnabled(getContentPane(), false);
        
        
        try
        {            
            jTree1.clearSelection();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void start(final ModbusLink link)
    {
        isRunning = true;
        
        disableDialog();
        
        // retrieve the list of tasks
        List<ModbusMasterTask> tasks = modbusPalProject.getModbusMasterTasks();
        
        // create a thread for eack task
        for(final ModbusMasterTask task : tasks)
        {
            Thread t = new Thread( new Runnable()
            {
                @Override
                public void run() 
                {
                    task.run(link);
                }
            });
            threads.add(t);
        }
        
        // start all threads
        for(Thread t : threads)
        {
            t.start();
        }
        
    }

    public void stop()
    {
        // interrupt all threads
        for(Thread t : threads)
        {
            t.interrupt();
        }
        
        threads.clear();
        isRunning = false;
        //addButton.setEnabled(true);
        //removeButton.setEnabled(true);        
        GUITools.setAllEnabled(getContentPane(), true);
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JButton modifyButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void modbusSlaveAdded(ModbusSlave slave)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modbusSlaveRemoved(ModbusSlave slave)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void automationAdded(Automation automation, int index) 
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void automationRemoved(Automation automation)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pduProcessed() 
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pduException() 
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pduNotServiced() 
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void modbusMasterTaskRemoved(ModbusMasterTask mmt) 
    {
        mmt.detach();
        mmTreeModel.removeNodeFromParent(mmt);
    }

    @Override
    public void modbusMasterTaskAdded(ModbusMasterTask mmt) 
    {
        // add new task to the tree
        mmt.attach(jTree1);
        mmTreeModel.insertNodeInto(mmt, modbusMasterRoot, modbusMasterRoot.getChildCount());
        jTree1.setSelectionPath( new TreePath( mmt.getPath() ) );
    }

}
