/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddSlaveDialog.java
 *
 * Created on 20 déc. 2008, 12:07:28
 */

package modbuspal.main;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modbuspal.slave.*;

/**
 * a dialog in which the user defines new modbus slave to add into the project
 * @author nnovic
 */
public class AddSlaveDialog
extends javax.swing.JDialog
{
    private boolean added = false;
    
    /** 
     * Creates new form AddSlaveDialog. This constructor is usually
     * called when the user add a new slave in the project.
     * @param slaves the list of modbus slaves in the project,
     * indexed by slave number.
     */
    public AddSlaveDialog()
    {
        this(null);
        setTitle("New slave");
    }

    /** 
     * Creates new form AddSlaveDialog. This constructor is usually
     * called when the user duplicates a modbus slave.
     * @param slaves the list of modbus slaves in the project,
     * indexed by slave number.
     * @param name suggested name for the modbus slave(s) to add
     */
    public AddSlaveDialog(String name)
    {
        setModalityType(ModalityType.DOCUMENT_MODAL);

        initComponents();
        setTitle("Duplicate "+name);
        //GUITools.align(parent,this);

        if( name!=null )
        {
            nameTextField.setText(name);
        }
    }

    /**
     * Indicates that the user has validated the selection by clicking
     * on the ok button.
     * @return true if the user has validate the selection by clicking on the
     * ok button
     */
    public boolean isAdded()
    {
        return added;
    }

    
    private List<ModbusSlaveAddress> tryParseRtuAddress(String s)
    {
        Pattern rtuPattern = Pattern.compile("([\\d]+)(?:[\\s]*-[\\s]*([\\d]+))?");
        Matcher m = rtuPattern.matcher(s.trim());
        if(m.matches()==true)
        {
            String group1 =  m.group(1);
            int startIndex = Integer.parseInt(group1);
            int endIndex = startIndex;
            int groupCount = m.groupCount();
            if( groupCount==2 )
            {
                String group2 = m.group(2);
                if( group2!=null)
                {
                    endIndex = Integer.parseInt(group2);
                }
            }
            
            if( (startIndex<ModbusConst.FIRST_MODBUS_SLAVE) 
            || (startIndex>ModbusConst.LAST_MODBUS_SLAVE))
            {
                throw new ArrayIndexOutOfBoundsException();
            }
            
            if( (endIndex<ModbusConst.FIRST_MODBUS_SLAVE) 
            || (endIndex>ModbusConst.LAST_MODBUS_SLAVE))
            {
                throw new ArrayIndexOutOfBoundsException();
            }
            
            if( startIndex > endIndex )
            {
                throw new IllegalArgumentException();
            }
            
            int count = 1 + (endIndex-startIndex);
            ArrayList<ModbusSlaveAddress> output = new ArrayList<ModbusSlaveAddress>(count);
            for(int i=0; i<count; i++)
            {
                ModbusSlaveAddress msa = new ModbusSlaveAddress(startIndex+i);
                output.add(msa);
            }
            return output;
        }
        return null;
    }
    
    
    private int[] parseIpv4(String s)
    {
        Pattern ipv4Pattern = Pattern.compile("([\\d]{1,3})\\.([\\d]{1,3})\\.([\\d]{1,3})\\.([\\d]{1,3})");
        Matcher m = ipv4Pattern.matcher(s);
        if(m.find()==true)
        {
            String a = m.group(1);
            String b = m.group(2);
            String c = m.group(3);
            String d = m.group(4);
            
            int[] output = new int[4];
            
            output[0] = Integer.parseInt(a);
            output[1] = Integer.parseInt(b);
            output[2] = Integer.parseInt(c);
            output[3] = Integer.parseInt(d);
            
            if( (output[0]>255) || (output[1]>255) || (output[2]>255) || (output[3]>255) )
            {
                return null;
            }
            
            return output;
        }
        return null;
    }
        
    private List<ModbusSlaveAddress> tryParseIpAddress(String s)
    {
        Pattern p = Pattern.compile("([\\d\\.]+)(?:[\\s]*-[\\s]*([\\d\\.]+))?");
        //Pattern p = Pattern.compile("([\\d\\.]+)");
        Matcher m = p.matcher(s.trim());
        if( m.find() )
        {
            int count = m.groupCount();
            String firstIp = m.group(1);
            String lastIp = m.group(2);
            if( lastIp==null )
            {
                lastIp=firstIp;
            }
            
            int[] startIp = parseIpv4(firstIp);
            int[] endIp = parseIpv4(lastIp);
            ArrayList<ModbusSlaveAddress> output = new ArrayList<ModbusSlaveAddress>(count);
            for(int a = startIp[0]; a<= endIp[0]; a++ )
            {
                for(int b = startIp[1]; b<= endIp[1]; b++ )
                {
                    for(int c = startIp[2]; c<= endIp[2]; c++ )
                    {
                        for(int d = startIp[3]; d<= endIp[3]; d++ )
                        {   
                            try 
                            {
                                byte[] ip = new byte[]{ (byte)a, (byte)b, (byte)c, (byte)d };
                                InetAddress addr = Inet4Address.getByAddress(ip);
                                ModbusSlaveAddress msa = new ModbusSlaveAddress(addr);
                                output.add(msa);
                            } 
                            catch (UnknownHostException ex) 
                            {
                                Logger.getLogger(AddSlaveDialog.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }   
                    }   
                } 
            }
            return output;
        }
        return null;
    }
    

    
    
    private List<ModbusSlaveAddress> parseSlaveIds()
    {
        ArrayList<ModbusSlaveAddress> output = new ArrayList<ModbusSlaveAddress>();
        String rawList = slavesTextArea.getText();
        String[] chunks = rawList.split("[,\r\n]+");
        
        for(String chunk:chunks)
        {
            List<ModbusSlaveAddress> msa = tryParseRtuAddress(chunk);
            if(msa == null)
            {
                msa = tryParseIpAddress(chunk);
                /*if(msa==null)
                {
                    msa = tryParseIpRtuAddress(chunk);
                }*/
            }
            
            if( msa != null )
            {
                output.addAll(msa);
            }
        }
        
        return output;
    }
    
    
    /**
     * Gets the list of the slaves to add in the project, identified by their
     * slave numbers
     * @return list of the modbus slave numbers to create in the project
     */
    public ModbusSlaveAddress[] getSlaveIds()
    {
        List<ModbusSlaveAddress> list = parseSlaveIds();
        ModbusSlaveAddress[] output = new ModbusSlaveAddress[0];
        return list.toArray(output);
        
        /*Object sel[] = slaveIdList.getSelectedValues();
        int ids[] = new int[sel.length];

        for( int i=0; i<sel.length; i++)
        {
            ids[i] = (Integer)sel[i];
        }
        return ids;*/
    }

    /**
     * Gets the slave name that has been typed by the user and shall be
     * used for the modbus slave(s) to create in the project
     * @return slave name typed by the user
     */
    public String getSlaveName()
    {
        return nameTextField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        slavesTextArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New slave");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 10, 10);
        getContentPane().add(addButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 10, 2);
        getContentPane().add(cancelButton, gridBagConstraints);

        jLabel1.setText("Add slave:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 2, 2);
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setText("Slave name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 10, 2);
        getContentPane().add(jLabel2, gridBagConstraints);

        nameTextField.setText("unknown slave");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 10, 2);
        getContentPane().add(nameTextField, gridBagConstraints);

        slavesTextArea.setColumns(20);
        slavesTextArea.setRows(5);
        jScrollPane2.setViewportView(slavesTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 2);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        added = true;
        setVisible(false);
    }//GEN-LAST:event_addButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        added = false;
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextArea slavesTextArea;
    // End of variables declaration//GEN-END:variables

}
