/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AutomationEditor.java
 *
 * HISTORY:
 * - RandomGenerator has been added
 * - Do not implement GeneratorListener anymore.
 *
 * Created on 21 déc. 2008, 18:46:08
 */

package modbuspal.automation;

import java.awt.CardLayout;
import modbuspal.generator.Generator;
import modbuspal.instanciator.Instantiable;
import modbuspal.instanciator.InstantiableManagerListener;
import modbuspal.generator.GeneratorRenderer;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.instanciator.InstantiableManager;
import modbuspal.main.ErrorMessage;
import modbuspal.main.ListLayout;
import modbuspal.main.ModbusPalPane;
import modbuspal.toolkit.XFileChooser;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Build a dialog for creating/editing an automation.
 * @author nnovic
 */
public class AutomationEditor
extends javax.swing.JDialog
implements AutomationEditionListener, AutomationExecutionListener, InstantiableManagerListener
{
    private final Automation automation;
    private final ListLayout listLayout;
    private final InstantiableManager<Generator> generatorFactory;
    private final ModbusPalPane modbusPalPane;
    private JPanel chartPanel;

    /** Creates new form AutomationEditor 
     * @param a the automation to edit
     * @param p reference on the ModbusPalPane that is summoning this editor
     */
    public AutomationEditor(Automation a, ModbusPalPane p)
    {
        //mainGui = gui;
        modbusPalPane = p;
        generatorFactory = modbusPalPane.getProject().getGeneratorFactory();
        automation = a;
        
        setTitle( "Automation:"+automation.getName() );
        Image img = Toolkit.getDefaultToolkit().createImage( getClass().getResource("/modbuspal/main/img/icon32.png") );
        setIconImage(img);
        
        listLayout = new ListLayout();
        initComponents();
        addAlreadyExistingGeneratorsToList();
        addGeneratorButtons();
        pack();

        // try to install the ChartPanel
        if( ModbusPalPane.verifyJFreeChart()==true )
        {
            chartPanel = new AutomationChart(a);
            jPanel1.add("chart",chartPanel);
        }
        else
        {
            chartPanel=null;
        }
    }

    
    private void addGeneratorButton(final String className)
    {
        JButton button = new JButton( className );
        button.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try 
                {
                    Generator gen = generatorFactory.newInstance(className);
                    automation.addGenerator(gen);
                }
                catch (InstantiationException ex)
                {
                    Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (IllegalAccessException ex)
                {
                    Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        generatorsPanel.add(button);
        addGenPanel.validate();
    }


    private void removeGeneratorButton(String className)
    {
        int max = generatorsPanel.getComponentCount();
        for(int i=0; i<max; i++)
        {
            Component comp = generatorsPanel.getComponent(i);
            if( comp instanceof JButton )
            {
                JButton button = (JButton)comp;
                if( button.getText().compareTo(className)==0 )
                {
                    generatorsPanel.remove(button);
                }
            }
        }
        addGenPanel.validate();
        repaint();
    }



    private void addGeneratorButtons()
    {
        // get the list of generators:
        String list[] = generatorFactory.getList();

        // for each generator, add a button;
        for( int i=0; i<list.length; i++ )
        {
            addGeneratorButton(list[i]);
        }
    }

    /**
     * Moves the specified generator down in the list of generators.
     * @param source the generator to move down
     */
    public void down(GeneratorRenderer source)
    {
        Generator gen = source.getGenerator();
        automation.down(gen);
    }

    /**
     * Removes the specified generator from the list of generators
     * @param renderer the generator to remove
     */
    public void remove(GeneratorRenderer renderer)
    {
        // get the generator
        Generator gen = renderer.getGenerator();

        // remove generator from the list
        automation.removeGenerator(gen);
    }

    /**
     * Moves the specified generator up in the generators list
     * @param source the generator to move up
     */
    public void up(GeneratorRenderer source)
    {
        Generator gen = source.getGenerator();
        automation.up(gen);
    }

    private void addAlreadyExistingGeneratorsToList()
    {
        Generator generators[]=automation.getGenerators();
        if(generators==null)
        {
            return;
        }

        for(int i=0; i<generators.length; i++)
        {
            GeneratorRenderer renderer = new GeneratorRenderer(this, generators[i]);
            automation.addGeneratorListener(renderer);
            generatorsListPanel.add( renderer, new Integer(i) );
        }
        generatorsListScrollPane.validate();
    }

    private void exportAutomation(File target)
    throws FileNotFoundException, IOException
    {
        OutputStream out = new FileOutputStream(target);
        exportAutomation(out);
        out.close();
    }

    private void exportAutomation(OutputStream out)
    throws IOException
    {
        String xmlTag = "<?xml version=\"1.0\"?>\r\n";
        out.write( xmlTag.getBytes() );

        String docTag = "<!DOCTYPE modbuspal_automation SYSTEM \"modbuspal.dtd\">\r\n";
        out.write( docTag.getBytes() );

        String openTag = "<modbuspal_automation>\r\n";
        out.write( openTag.getBytes() );
        
        automation.save(out);

        String closeTag = "</modbuspal_automation>\r\n";
        out.write( closeTag.getBytes() );
    }

    private GeneratorRenderer findComponent(Generator g1)
    {
        Component comps[] = generatorsListPanel.getComponents();
        for(int i=0; i<comps.length; i++)
        {
            if( comps[i] instanceof GeneratorRenderer )
            {
                GeneratorRenderer renderer = (GeneratorRenderer)comps[i];
                if( renderer.getGenerator() == g1 )
                {
                    return renderer;
                }
            }
        }
        return null;
    }

    private void importAutomation(Document doc)
    throws InstantiationException, IllegalAccessException
    {
        NodeList slaveNodes = doc.getElementsByTagName("automation");
        if( slaveNodes.getLength()==1 )
        {
            importAutomation(slaveNodes.item(0));
        }
        else
        {
            //TODO:
//            ImportSlaveDialog dialog = new ImportSlaveDialog(mainGui, slaveNodes);
//            dialog.setVisible(true);
//            Node data = dialog.getImport();
//            if( data != null )
//            {
//                importSlave(data);
//            }
        }
    }

    private void importAutomation(File importFile)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        Document doc = XMLTools.ParseXML(importFile);

        // normalize text representation
         doc.getDocumentElement().normalize();

         importAutomation(doc);
    }

    private void importAutomation(Node item)
    throws InstantiationException, IllegalAccessException
    {
        NamedNodeMap attributes = item.getAttributes();
        NodeList content = item.getChildNodes();
        automation.loadAttributes(attributes);
        automation.loadGenerators(content, generatorFactory);
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

        buttonsPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        playToggleButton = new javax.swing.JToggleButton();
        stopButton = new javax.swing.JButton();
        loopToggleButton = new javax.swing.JToggleButton();
        addGenPanel = new javax.swing.JPanel();
        genButtonsPanel = new javax.swing.JPanel();
        removeInstanciatorButton = new javax.swing.JButton();
        generatorsPanel = new javax.swing.JPanel();
        valuePanel = new javax.swing.JPanel();
        valueTextField = new javax.swing.JTextField();
        settingsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        initTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        stepTextField = new javax.swing.JTextField();
        importExportPanel = new javax.swing.JPanel();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        chartToggleButton = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        generatorsListScrollPane = new javax.swing.JScrollPane();
        generatorsListPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        controlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Controls"));
        controlsPanel.setLayout(new java.awt.GridBagLayout());

        playToggleButton.setText("Play");
        playToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        controlsPanel.add(playToggleButton, gridBagConstraints);

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        controlsPanel.add(stopButton, gridBagConstraints);

        loopToggleButton.setSelected(automation.isLoopEnabled());
        loopToggleButton.setText("Loop");
        loopToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        controlsPanel.add(loopToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        buttonsPanel.add(controlsPanel, gridBagConstraints);

        addGenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Add generators"));
        addGenPanel.setLayout(new java.awt.BorderLayout());

        genButtonsPanel.setLayout(new java.awt.GridBagLayout());

        removeInstanciatorButton.setText("...");
        removeInstanciatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeInstanciatorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        genButtonsPanel.add(removeInstanciatorButton, gridBagConstraints);

        addGenPanel.add(genButtonsPanel, java.awt.BorderLayout.EAST);

        generatorsPanel.setLayout(new java.awt.GridLayout(0, 3));
        addGenPanel.add(generatorsPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(addGenPanel, gridBagConstraints);

        valuePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));

        valueTextField.setEditable(false);
        valueTextField.setText("000.0000");
        valueTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        valuePanel.add(valueTextField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        buttonsPanel.add(valuePanel, gridBagConstraints);

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Global settings"));
        settingsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Init:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(jLabel3, gridBagConstraints);

        initTextField.setText(String.valueOf( automation.getInitialValue() ));
        initTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                initTextFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(initTextField, gridBagConstraints);

        jLabel2.setText("Step:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(jLabel2, gridBagConstraints);

        stepTextField.setText(String.valueOf(automation.getStepDelay()));
        stepTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        stepTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                stepTextFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        settingsPanel.add(stepTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        buttonsPanel.add(settingsPanel, gridBagConstraints);

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);

        importExportPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        importExportPanel.add(importButton);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        importExportPanel.add(exportButton);

        chartToggleButton.setText("Chart");
        chartToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartToggleButtonActionPerformed(evt);
            }
        });
        importExportPanel.add(chartToggleButton);

        getContentPane().add(importExportPanel, java.awt.BorderLayout.PAGE_START);

        jPanel1.setLayout(new java.awt.CardLayout());

        generatorsListScrollPane.setPreferredSize(new java.awt.Dimension(300, 250));

        generatorsListPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
        generatorsListPanel.setLayout(null);
        generatorsListPanel.setLayout(listLayout);
        generatorsListScrollPane.setViewportView(generatorsListPanel);

        jPanel1.add(generatorsListScrollPane, "generators");

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Chart is disabled.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        jButton1.setText("Why?");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton1, gridBagConstraints);

        jPanel1.add(jPanel2, "disabled");

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playToggleButtonActionPerformed

        if( playToggleButton.isSelected() )
        {
            playAutomation();
        }
        else
        {
            pauseAutomation();
        }
    }//GEN-LAST:event_playToggleButtonActionPerformed


    private void playAutomation()
    {
        if( automation.isSuspended() )
        {
            automation.resume();
        }
        else
        {
            automation.start();
        }
    }

    private void pauseAutomation()
    {
        playToggleButton.setText("Play");
        automation.suspend();
    }

    private void stopAutomation()
    {
        automation.stop();
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        stopAutomation();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void loopToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopToggleButtonActionPerformed
        automation.setLoopEnabled( loopToggleButton.isSelected() );
    }//GEN-LAST:event_loopToggleButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

        // newInstance and setup dialog
        JFileChooser chooser = new XFileChooser(XFileChooser.AUTOMATION_FILE);

        chooser.showDialog(this, "Export");
        File target = chooser.getSelectedFile();
        if( target != null )
        {
            try
            {
                exportAutomation(target);
            } 
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void stepTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stepTextFieldFocusLost
        String exp = stepTextField.getText();
        double val = Double.parseDouble(exp);
        automation.setStepDelay(val);
    }//GEN-LAST:event_stepTextFieldFocusLost

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed

        // newInstance and setup dialog
        JFileChooser chooser = new XFileChooser(XFileChooser.AUTOMATION_FILE);

        chooser.showDialog(this, "Import");
        File target = chooser.getSelectedFile();
        if( target == null )
        {
            return;
        }

        try
        {
            importAutomation(target);
        } 
        catch (ParserConfigurationException ex)
        {
            Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SAXException ex)
        {
            Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex)
        {
            Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(AutomationEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_importButtonActionPerformed

    private void initTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_initTextFieldFocusLost
        String val = initTextField.getText();
        double dval = Double.parseDouble(val);
        automation.setInitialValue( dval );
    }//GEN-LAST:event_initTextFieldFocusLost

    private void removeInstanciatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeInstanciatorButtonActionPerformed

        // ask script manager to appear, with the "generators" tab selected
        modbusPalPane.showScriptManagerDialog();

    }//GEN-LAST:event_removeInstanciatorButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ErrorMessage dialog = new ErrorMessage("Close");
        dialog.setTitle("Chart disabled");
        dialog.append("It seems that jFreeChart is not present on your computer, and it is required to draw the chart.");
        dialog.append("If you want to use the chart, go to http://www.jfree.org/jfreechart/.");
        dialog.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void chartToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartToggleButtonActionPerformed
        CardLayout cl = (CardLayout)jPanel1.getLayout();
        if( chartToggleButton.isSelected()==true )
        {
            if( chartPanel==null )
            {
                cl.show(jPanel1, "disabled");
            }
            else
            {
                cl.show(jPanel1, "chart");
            }
        }
        else
        {    
            cl.show(jPanel1, "generators");
        }

    }//GEN-LAST:event_chartToggleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addGenPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JToggleButton chartToggleButton;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton exportButton;
    private javax.swing.JPanel genButtonsPanel;
    private javax.swing.JPanel generatorsListPanel;
    private javax.swing.JScrollPane generatorsListScrollPane;
    private javax.swing.JPanel generatorsPanel;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel importExportPanel;
    private javax.swing.JTextField initTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToggleButton loopToggleButton;
    private javax.swing.JToggleButton playToggleButton;
    private javax.swing.JButton removeInstanciatorButton;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTextField stepTextField;
    private javax.swing.JButton stopButton;
    private javax.swing.JPanel valuePanel;
    private javax.swing.JTextField valueTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void automationHasEnded(Automation source)
    {
        if( source == automation )
        {
            // disable any button in "Add Generators" panel
            Component buttons[] = generatorsPanel.getComponents();
            for(int i=0; i<buttons.length; i++)
            {
                buttons[i].setEnabled(true);
            }

            // enable any control panel in "Generators list"
            Component generators[] = generatorsListPanel.getComponents();
            for(int i=0; i<generators.length; i++)
            {
                generators[i].setEnabled(true);
            }

            playToggleButton.setText("Play");
            playToggleButton.setSelected(false);

            // enable any input in "Settings" list
            Component inputs[] = settingsPanel.getComponents();
            for(int i=0; i<inputs.length; i++)
            {
                if( inputs[i] instanceof JTextField )
                {
                    ((JTextField)inputs[i]).setEditable(true);
                }
            }
        }
    }

    @Override
    public void automationHasStarted(Automation aThis)
    {
        // disable any button in "Add Generators" panel
        Component buttons[] = generatorsPanel.getComponents();
        for(int i=0; i<buttons.length; i++)
        {
            buttons[i].setEnabled(false);
        }

        // disable any control panel in "Generators list"
        Component generators[] = generatorsListPanel.getComponents();
        for(int i=0; i<generators.length; i++)
        {
            generators[i].setEnabled(false);
        }

        // check that the "play" button is pushed. This is not the
        // case if the automation is started by pushing the button
        // on the main gui instead of the button on the automation
        // editor.
        playToggleButton.setSelected(true);
        playToggleButton.setText("Pause");

        // disable any input in "Global settings" list
        Component inputs[] = settingsPanel.getComponents();
        for(int i=0; i<inputs.length; i++)
        {
            if( inputs[i] instanceof JTextField )
            {
                ((JTextField)inputs[i]).setEditable(false);
            }
        }
    }

    @Override
    public void automationValueHasChanged(Automation source, double time, double value)
    {
        valueTextField.setText( String.valueOf(value) );
        valueTextField.validate();
    }

    @Override
    public void automationNameHasChanged(Automation aThis, String newName)
    {
        setTitle( "Automation:"+newName );
    }

    @Override
    public void generatorHasBeenAdded(Automation source, Generator generator, int index)
    {
        // add slave panel into the gui and refresh gui
        GeneratorRenderer renderer = new GeneratorRenderer(this, generator);
        automation.addGeneratorListener(renderer);
        generatorsListPanel.add( renderer, new Integer(index) );
        generatorsListScrollPane.validate();
    }

    @Override
    public void automationLoopEnabled(Automation aThis, boolean enabled)
    {
        loopToggleButton.setSelected(enabled);
    }

    @Override
    public void automationStepHasChanged(Automation aThis, double step)
    {
        stepTextField.setText( String.valueOf(step) );
    }

    @Override
    public void generatorHasBeenRemoved(Automation source, Generator generator)
    {
        // remove generator's panel from the gui and refresh gui
        generatorsListPanel.remove( findComponent(generator) );
        generatorsListScrollPane.validate();
        generatorsListScrollPane.repaint();
    }

    @Override
    public void generatorsHaveBeenSwapped(Automation source, Generator g1, Generator g2)
    {
        Component comp1 = findComponent(g1);
        Component comp2 = findComponent(g2);
        listLayout.swapComponents(comp1,comp2);
        generatorsListPanel.doLayout();
    }

    @Override
    public void automationInitialValueChanged(Automation aThis, double init)
    {
        initTextField.setText( String.valueOf(init) );
    }

    @Override
    public void instanciatorAdded(InstantiableManager factory, Instantiable def)
    {
        if( def instanceof Generator )
        {
            addGeneratorButton( def.getClassName() );
        }
    }

    @Override
    public void instanciatorRemoved(InstantiableManager factory, Instantiable def)
    {
        if( def instanceof Generator )
        {
            removeGeneratorButton(def.getClassName());
        }
    }

    @Override
    public void automationReloaded(Automation source)
    {
    }

}
