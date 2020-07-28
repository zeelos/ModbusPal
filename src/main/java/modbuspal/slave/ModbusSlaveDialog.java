/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModbusSlaveDialog.java
 *
 * Created on 17 déc. 2008, 11:52:45
 */

package modbuspal.slave;

import modbuspal.toolkit.*;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import modbuspal.main.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.instanciator.InstantiableManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * the dialog where the user edits the values and parameters of a particular
 * modbus slave
 * @author nnovic
 */
public class ModbusSlaveDialog
extends javax.swing.JDialog
implements ModbusConst, ModbusSlaveListener
{
    final ModbusSlave modbusSlave;
    final ModbusPalPane modbusPalPane;
    final ModbusPalProject modbusPalProject;

    /** Creates new form ModbusSlaveDialog
     * @param p the modbuspalpane to which this dialog is connected
     * @param s the modbus slave being displayed by this dialog
     */
    public ModbusSlaveDialog(ModbusPalPane p, ModbusSlave s)
    {
        modbusPalPane = p;
        modbusPalProject = modbusPalPane.getProject();
        modbusSlave = s;

        ModbusSlaveAddress id = s.getSlaveId();
        String name = s.getName();
        setTitle( String.valueOf(id) + ":" + name );
        setIconImage(FileTools.getImage("/img/icon32.png"));

        modbusSlave.addModbusSlaveListener(this);
        initComponents();
        holdingRegistersPanel.add(new ModbusRegistersPanel(this, modbusSlave.getHoldingRegisters()),BorderLayout.CENTER);
        coilsPanel.add(new ModbusCoilsPanel(this, modbusSlave.getCoils()),BorderLayout.CENTER);
        extendedRegistersPanel.add(new ModbusExtendedRegistersPanel(this, modbusSlave.getExtendedRegisters()),BorderLayout.CENTER);
        functionsPanel.add( new ModbusFunctionsPanel(this,modbusPalProject.getFunctionFactory()),BorderLayout.CENTER);

        // add function tabs for user defined functions
        // that may have been added by scripts prior to
        // adding the slave to the project, in which case
        // the modbusSlavePduProcessorChanged event has
        // not been triggered.
        ModbusPduProcessor mpps[]= s.getPduProcessorInstances();
        for(int i=0; i<mpps.length; i++)
        {
            addPane(mpps[i]);
        }

        // set tuning values.
        modbusSlaveReplyDelayChanged(s, s.getMinReplyDelay(), s.getMaxReplyDelay());
        modbusSlaveErrorRatesChanged(s, s.getNoReplyErrorRate() );
    }

    ModbusSlave getModbusSlave()
    {
        return modbusSlave;
    }



//    private void importSlave(Document doc)
//    {
//        NodeList slaveNodes = doc.getElementsByTagName("slave");
//        if( slaveNodes.getLength()==1 )
//        {
//            importSlave(slaveNodes.item(0) );
//        }
//        else
//        {
//            ImportSlaveDialog dialog = new ImportSlaveDialog(mainGui, slaveNodes);
//            dialog.setVisible(true);
//            Node data = dialog.getImport();
//            if( data != null )
//            {
//                importSlave(data);
//            }
//        }
//    }

    private void importSlave(File importFile)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        // open import file
        Document doc = XMLTools.ParseXML(importFile);

        // normalize text representation
        doc.getDocumentElement().normalize();

        // how many slaves in the file?
        NodeList slaves = doc.getElementsByTagName("slave");

        // if only one slave...
        if( slaves.getLength()==1 )
        {
            // any bindings ?
            Node uniqNode = slaves.item(0);
            Collection<Node> bindings = XMLTools.findChildren(uniqNode,"binding");

            // if no bindings, then make a simle call to "load"
            if( bindings.isEmpty() )
            {
                modbusSlave.load(modbusPalProject, uniqNode, true);
                return;
            }
        }

        // if several slaves are defined in the import file, and/or if
        // bindings are defined in the import file, display the import dialog:
        ImportSlaveDialog dialog = new ImportSlaveDialog(GUITools.findFrame(this), doc);
        dialog.setVisible(true);

        // get the selected slave:
        int idSrc = dialog.getSelectedSlaveID();

        // rip-off any information that is not related to this slave:
        for( int i=0; i<slaves.getLength(); i++ )
        {
            Node slave = slaves.item(i);
            String id = XMLTools.getAttribute("id", slave);
            int sId = Integer.valueOf(id);
            if(sId!=idSrc)
            {
                doc.removeChild(slave);
            }
        }
        ModbusPalProject.optimize(doc,false);

        boolean importBindings = dialog.importBindings();
        boolean importAutomations = dialog.importAutomations();

        modbusPalProject.importSlave(doc, modbusSlave, importBindings, importAutomations);
    }

    /** This method is called getStartingAddress within the constructor getQuantity
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        holdingRegistersPanel = new javax.swing.JPanel();
        extendedRegistersPanel = new javax.swing.JPanel();
        coilsPanel = new javax.swing.JPanel();
        functionsPanel = new javax.swing.JPanel();
        tuningPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        minReplyDelayTextField = new NumericTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        maxReplyDelayTextField = new NumericTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        noReplyRateSlider = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        implementationComboBox = new javax.swing.JComboBox();
        stayOnTopCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        holdingRegistersPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        holdingRegistersPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Holding registers", holdingRegistersPanel);

        extendedRegistersPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        extendedRegistersPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Extended registers", extendedRegistersPanel);

        coilsPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Coils", coilsPanel);

        functionsPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        functionsPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Functions", functionsPanel);

        tuningPanel.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Reply delay"));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Min:");
        jPanel4.add(jLabel1, new java.awt.GridBagConstraints());

        minReplyDelayTextField.setColumns(5);
        minReplyDelayTextField.setText("0");
        minReplyDelayTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                minReplyDelayTextFieldFocusLost(evt);
            }
        });
        jPanel4.add(minReplyDelayTextField, new java.awt.GridBagConstraints());

        jLabel2.setText("ms");
        jPanel4.add(jLabel2, new java.awt.GridBagConstraints());

        jLabel3.setText("Max:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jLabel3, gridBagConstraints);

        maxReplyDelayTextField.setColumns(5);
        maxReplyDelayTextField.setText("0");
        maxReplyDelayTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                maxReplyDelayTextFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel4.add(maxReplyDelayTextField, gridBagConstraints);

        jLabel4.setText("ms");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel4, gridBagConstraints);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Error rates"));
        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("No reply:");
        jPanel5.add(jLabel5, new java.awt.GridBagConstraints());

        noReplyRateSlider.setMajorTickSpacing(25);
        noReplyRateSlider.setMinorTickSpacing(5);
        noReplyRateSlider.setPaintLabels(true);
        noReplyRateSlider.setPaintTicks(true);
        noReplyRateSlider.setValue(0);
        noReplyRateSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noReplyRateSliderStateChanged(evt);
            }
        });
        jPanel5.add(noReplyRateSlider, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel5, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel2);

        tuningPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Tuning", tuningPanel);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        jPanel1.add(importButton);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jPanel1.add(exportButton);

        implementationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Modbus", "J-Bus" }));
        implementationComboBox.setSelectedIndex(modbusSlave.getImplementation());
        implementationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                implementationComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(implementationComboBox);

        stayOnTopCheckBox.setText("Stay on top");
        stayOnTopCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stayOnTopCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(stayOnTopCheckBox);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        statusLabel.setText("-");
        jPanel3.add(statusLabel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        boolean exportBindings = false;
        boolean exportAutomations = false;

        // Any bindings ?
        if( modbusSlave.hasBindings()==true )
        {
            // Create option dialog
            ExportSlaveDialog optionDialog = new ExportSlaveDialog(GUITools.findFrame(this));
            GUITools.align(this, optionDialog);
            optionDialog.setVisible(true);

            // check that the option dialog has been validated
            if( optionDialog.isOK()==false )
            {
                return;
            }

            exportBindings = optionDialog.exportBindings();
            exportAutomations = optionDialog.exportAutomations();
        }

        // Create dialog
        JFileChooser saveDialog = new XFileChooser(XFileChooser.SLAVE_FILE);

        // show dialog
        saveDialog.showSaveDialog(this);

        // get selected file
        File exportFile = saveDialog.getSelectedFile();

        if( exportFile == null )
        {
            setStatus("Cancelled by user.");
            return;
        }

        try
        {
            modbusPalProject.exportSlave(exportFile, modbusSlave.getSlaveId(), exportBindings, exportAutomations );
            setStatus("Export completed.");
        }
        catch (Exception ex)
        {
            Logger.getLogger(ModbusSlaveDialog.class.getName()).log(Level.SEVERE, null, ex);
            setStatus("Export failed.");
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed

        // create dialog
        JFileChooser loadDialog = new XFileChooser(XFileChooser.SLAVE_FILE);

        // show dialog
        setStatus("Importing...");
        loadDialog.showOpenDialog(this);

        // get selected file
        File importFile = loadDialog.getSelectedFile();

        if( importFile == null )
        {
            setStatus("Import cancelled by user.");
            return;
        }

        try
        {
            importSlave(importFile);
        }
        catch (Exception ex)
        {
            Logger.getLogger(ModbusSlaveDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        setStatus("Data imported.");
    }//GEN-LAST:event_importButtonActionPerformed

    private void implementationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_implementationComboBoxActionPerformed

        switch( implementationComboBox.getSelectedIndex() )
        {
            default:
            case 0: // modbus
                modbusSlave.setImplementation(IMPLEMENTATION_MODBUS);
                break;
            case 1: // J-Bus
                modbusSlave.setImplementation(IMPLEMENTATION_JBUS);
                break;
        }
}//GEN-LAST:event_implementationComboBoxActionPerformed

    private void stayOnTopCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stayOnTopCheckBoxActionPerformed
        setAlwaysOnTop( stayOnTopCheckBox.isSelected() );
    }//GEN-LAST:event_stayOnTopCheckBoxActionPerformed

    private void minReplyDelayTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_minReplyDelayTextFieldFocusLost
        replyDelayValidate();
    }//GEN-LAST:event_minReplyDelayTextFieldFocusLost

    private void maxReplyDelayTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxReplyDelayTextFieldFocusLost
        replyDelayValidate();
    }//GEN-LAST:event_maxReplyDelayTextFieldFocusLost

    private void noReplyRateSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noReplyRateSliderStateChanged
        noReplyRateValidate();
    }//GEN-LAST:event_noReplyRateSliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel coilsPanel;
    private javax.swing.JButton exportButton;
    private javax.swing.JPanel functionsPanel;
    private javax.swing.JPanel holdingRegistersPanel;
    private javax.swing.JPanel extendedRegistersPanel;
    private javax.swing.JComboBox implementationComboBox;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField maxReplyDelayTextField;
    private javax.swing.JTextField minReplyDelayTextField;
    private javax.swing.JSlider noReplyRateSlider;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JCheckBox stayOnTopCheckBox;
    private javax.swing.JPanel tuningPanel;
    // End of variables declaration//GEN-END:variables

    void setStatus(String text)
    {
        statusLabel.setText(text);
    }

    @Override
    public void modbusSlaveEnabled(ModbusSlave slave, boolean enabled)
    {
    }

    @Override
    public void modbusSlaveNameChanged(ModbusSlave slave, String newName)
    {
    }

    @Override
    public void modbusSlaveImplChanged(ModbusSlave slave, int impl)
    {
        switch( impl )
        {
            default:
            case IMPLEMENTATION_MODBUS:
                implementationComboBox.setSelectedIndex(0);
                break;
            case IMPLEMENTATION_JBUS:
                implementationComboBox.setSelectedIndex(1);
                break;
        }
    }

    @Override
    public void modbusSlavePduProcessorChanged(ModbusSlave slave, byte functionCode, ModbusPduProcessor old, ModbusPduProcessor mspp)
    {
        // check if old instance's panel must be removed
        if(old!=null)
        {
            if( modbusSlave.containsPduProcessorInstance(old)==false )
            {
                removePane(old);
            }
        }

        // check if new instance's panel must be added
        if(mspp!=null)
        {
            if( modbusSlave.containsPduProcessorInstance(mspp)==false )
            {
                addPane(mspp);
            }
        }
    }

    private void addPane(ModbusPduProcessor mspp)
    {
        if(mspp!=null)
        {
            JPanel jp = mspp.getPduPane();
            if( jp!=null )
            {
                jTabbedPane1.add( InstantiableManager.makeInstanceName(mspp), jp);
            }
        }
    }

    private void removePane(ModbusPduProcessor mspp)
    {
        if(mspp!=null)
        {
            JPanel jp = mspp.getPduPane();
            if( jp!=null )
            {
                jTabbedPane1.remove(jp);
            }
        }
    }

    private void replyDelayValidate() {
        long min = ((NumericTextField)minReplyDelayTextField).getLong();
        long max = ((NumericTextField)maxReplyDelayTextField).getLong();
        modbusSlave.setReplyDelay(min, max);

    }

    private void noReplyRateValidate() {
        float noReply = ((float)noReplyRateSlider.getValue()) / 100f;
        modbusSlave.setErrorRates(noReply);
    }

    @Override
    public void modbusSlaveReplyDelayChanged(ModbusSlave slave, long min, long max) {
        ((NumericTextField)minReplyDelayTextField).setValue( min );
        ((NumericTextField)maxReplyDelayTextField).setValue( max );
    }

    @Override
    public void modbusSlaveErrorRatesChanged(ModbusSlave slave, float noReplyRate)
    {
        noReplyRateSlider.setValue( (int)(noReplyRate*100f) );
    }


}
