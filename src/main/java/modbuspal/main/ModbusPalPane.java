/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModbusPalGui.java
 *
 * Created on 16 déc. 2008, 08:35:06
 */

package modbuspal.main;

import java.awt.CardLayout;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.AutomationPanel;
import modbuspal.toolkit.NumericTextField;
import modbuspal.slave.ModbusSlavePanel;
import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import modbuspal.automation.Automation;
import modbuspal.help.HelpViewer;
import modbuspal.link.*;
import modbuspal.master.ModbusMasterDialog;
import modbuspal.master.ModbusMasterTask;
import modbuspal.recorder.ModbusPalRecorder;
import modbuspal.script.ScriptManagerDialog;
import modbuspal.slave.ModbusSlave;
import modbuspal.slave.ModbusSlaveAddress;
import modbuspal.toolkit.GUITools;
import modbuspal.toolkit.XFileChooser;
import org.xml.sax.SAXException;

/**
 * The core of the application
 * @author nnovic
 */
public class ModbusPalPane
extends JPanel
implements ModbusPalXML, WindowListener, ModbusPalListener, ModbusLinkListener
{
    /** Name and version of this application */
    public static final String APP_STRING = "ModbusPal 1.6b";

    /** Base registry key for the configuration of the application. */
    public static final String BASE_REGISTRY_KEY = "modbuspal";

    private ArrayList<ModbusPalProjectListener> listeners = new ArrayList<ModbusPalProjectListener>();

    private ModbusMasterDialog modbusMasterDialog = null;
    ScriptManagerDialog scriptManagerDialog = null;
    private ModbusLink currentLink = null;
    private AppConsole console = null;
    ModbusPalProject modbusPalProject;
    private HelpViewer helpViewer = null;

    /**
     * Adds a ModbusPalProjectListener listener to the list of listeners.
     * @param l the listener to add
     */
    public void addModbusPalProjectListener(ModbusPalProjectListener l)
    {
        if(listeners.contains(l)==false)
        {
            listeners.add(l);
        }
    }

    /**
     * Removes a ModbusPalProjectListener from the list of listeners
     * @param l the listener to remove
     */
    public void removeModbusPalProjectListener(ModbusPalProjectListener l)
    {
        if(listeners.contains(l)==true)
        {
            listeners.remove(l);
        }
    }



    /**
     * Defines the project that ModbusPal must execute.
     * @param project the project to execute.
     */
    public void setProject(ModbusPalProject project)
    {
        System.out.printf("Set project %s\r\n", project.getName());

        //- - - - - - - - - - - - - - -
        // Clear existing project
        //- - - - - - - - - - - - - - -

        if( modbusPalProject!=null )
        {
            modbusPalProject.removeModbusPalListener(this);
        }


        //- - - - - - - - - - - - - -
        // Register new project
        //- - - - - - - - - - - - - -

        ModbusPalProject old = modbusPalProject;
        modbusPalProject = project;
        modbusPalProject.addModbusPalListener(this);


        //- - - - - - - - - - - - - -
        // Refresh GUI
        //- - - - - - - - - - - - - -

        // Select the link tab:
        for( int i=0; i<linksTabbedPane.getComponentCount(); i++ ) {
            if( linksTabbedPane.getTitleAt(i).compareTo( project.selectedLink )==0 ) {
                linksTabbedPane.setSelectedIndex(i);
            }
        }

        // Init tcp/ip settings
        portTextField.setText( project.linkTcpipPort );

        // Init serial settings
        comPortComboBox.setSelectedItem( project.linkSerialComId );
        baudRateComboBox.setSelectedItem( project.linkSerialBaudrate );
        parityComboBox.setSelectedIndex( project.linkSerialParity );
        stopBitsComboBox.setSelectedIndex( project.linkSerialStopBits );
        xonxoffCheckBox.setSelected( project.linkSerialXonXoff );
        rtsctsCheckBox.setSelected( project.linkSerialRtsCts );

        // Init record/replay settings
        setReplayFile(project.linkReplayFile);


        //- - - - - - - - - - - - - -
        // Refresh list of slaves
        //- - - - - - - - - - - - - -

        slavesListPanel.removeAll();
        for(ModbusSlave s : project.getModbusSlaves())
        {
            modbusSlaveAdded(s);
        }

        //- - - - - - - - - - - - - -
        // Refresh list of automations
        //- - - - - - - - - - - - - -

        automationsListPanel.removeAll();
        Automation[] automations = project.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automationAdded(automations[i], i);
        }

        //- - - - - - - - - - - - -
        // Refresh list of scripts
        //- - - - - - - - - - - - -

        if(scriptManagerDialog!=null)
        {
            scriptManagerDialog.setProject(modbusPalProject);
        }

        // Refresh MASTER
        if( modbusMasterDialog!=null)
        {
            modbusMasterDialog.setProject(modbusPalProject);
        }

        System.out.printf("[%s] Project set\r\n", modbusPalProject.getName());

        notifyModbusPalProjectChanged(old, modbusPalProject);

        //- - - - - - - - - - -
        // Refresh Display
        //- - - - - - - - - - -

        validate();
        repaint();
    }



    private void notifyModbusPalProjectChanged(ModbusPalProject oldProject, ModbusPalProject newProject)
    {
        for(ModbusPalProjectListener l:listeners)
        {
            l.modbusPalProjectChanged(oldProject, newProject);
        }
    }


    public void saveProject()
    throws FileNotFoundException, IOException
    {
        saveProject(modbusPalProject.projectFile);
    }

    public void saveProject(File projectFile)
    throws FileNotFoundException, IOException
    {
        System.out.printf("[%s] Save project\r\n", modbusPalProject.getName());

        // update selected link tab:
        int index = linksTabbedPane.getSelectedIndex();
        modbusPalProject.selectedLink = linksTabbedPane.getTitleAt(index);

        // update tcp/ip settings
        modbusPalProject.linkTcpipPort = portTextField.getText();

        // update serial settings
        modbusPalProject.linkSerialComId = (String)comPortComboBox.getSelectedItem();
        modbusPalProject.linkSerialBaudrate = (String)baudRateComboBox.getSelectedItem();
        modbusPalProject.linkSerialParity = parityComboBox.getSelectedIndex();
        modbusPalProject.linkSerialStopBits = stopBitsComboBox.getSelectedIndex();
        modbusPalProject.linkSerialXonXoff = xonxoffCheckBox.isSelected();
        modbusPalProject.linkSerialRtsCts = rtsctsCheckBox.isSelected();

        // update record/replay settings
        modbusPalProject.linkReplayFile = (File)chosenRecordFileTextField.getClientProperty("record file");

        modbusPalProject.save(projectFile);
    }



    private void setReplayFile(File src)
    {
        if( src!=null )
        {
            chosenRecordFileTextField.setText( src.getPath() );
            chosenRecordFileTextField.putClientProperty("record file", src);
        }
        else
        {
            chosenRecordFileTextField.setText(null);
            chosenRecordFileTextField.putClientProperty("record file", null);
        }
    }













    public ModbusPalPane()
    {
        this(false);
    }

    /** Creates new form ModbusPalPane */
    public ModbusPalPane(boolean useInternalConsole)
    {
        initComponents();

        if(useInternalConsole)
        {
            installConsole();
            consoleToggleButton.setEnabled(true);
        }
        else
        {
            consoleToggleButton.setToolTipText("Console is disabled");
        }

        installRecorder();
        installCommPorts();
        //installScriptEngine();

        setProject( new ModbusPalProject() );

    }


    private void installConsole()
    {
        try {
            console = new AppConsole();
            console.addWindowListener(this);
        } catch (IOException ex) {
            Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Checks if 3rd-party RXTX library is available. It will
     * try to load the "gnu.io.CommPortIdentifier" class.
     * @return true if RXTX has been detected (the class could be loaded)
     */
    public static boolean verifyRXTX()
    {
        // try to load the CommPortVerifier class
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try
        {
            Class c = cl.loadClass("gnu.io.CommPortIdentifier");
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Checks if 3rd-party Jython library is available. It will
     * try to load the "org.python.util.PythonInterpreter" class.
     * @return true if Jython has been detected (the class could be loaded)
     */
    public static boolean verifyPython()
    {
        // try to load the CommPortVerifier class
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try
        {
            Class c = cl.loadClass("org.python.util.PythonInterpreter");
            return true;
        }
        catch (ClassNotFoundException ex)
        {
            return false;
        }
    }

    /**
     * Checks if 3rd-party JFreeChart library is available. It will
     * try to load the "org.jfree.chart.JFreeChart" class.
     * @return true if JFreeChart has been detected (the class could be loaded)
     */
    public static boolean verifyJFreeChart()
    {
        // try to load the CommPortVerifier class
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try
        {
            Class c = cl.loadClass("org.jfree.chart.JFreeChart");
            return true;
        }
        catch (ClassNotFoundException ex)
        {
            return false;
        }
    }

    private void installScriptEngine()
    {

        if( verifyPython() == true )
        {
            scriptManagerDialog = new ScriptManagerDialog();
            scriptManagerDialog.addWindowListener(this);
        }
        else
        {
            scriptManagerDialog = null;
        }
    }

    private void installRecorder()
    {
        ModbusPalRecorder.touch();
    }

    private void installCommPorts()
    {
        // check if RXTX Comm lib is available
        if( verifyRXTX() == false )
        {
            CardLayout layout = (CardLayout)jPanel1.getLayout();
            layout.show(jPanel1, "disabled");
            return;
        }

        // detect the comm ports
        ModbusSerialLink.enumerate();

        // get the list of comm ports (as strings)
        // and put it in the swing list
        comPortComboBox.setModel( ModbusSerialLink.getListOfCommPorts() );
    }





    @Override
    public void pduProcessed()
    {
        ( (TiltLabel)tiltLabel ).tilt(TiltLabel.GREEN);
    }


    @Override
    public void pduException()
    {
        ( (TiltLabel)tiltLabel ).tilt(TiltLabel.RED);
    }


    @Override
    public void pduNotServiced()
    {
        ( (TiltLabel)tiltLabel ).tilt(TiltLabel.YELLOW);
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

        settingsPanel = new javax.swing.JPanel();
        linkPanel = new javax.swing.JPanel();
        linksTabbedPane = new javax.swing.JTabbedPane();
        tcpIpSettingsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        portTextField = new NumericTextField();
        jPanel1 = new javax.swing.JPanel();
        serialSettingsPanel = new javax.swing.JPanel();
        comPortComboBox = new javax.swing.JComboBox();
        baudRateComboBox = new javax.swing.JComboBox();
        parityComboBox = new javax.swing.JComboBox();
        xonxoffCheckBox = new javax.swing.JCheckBox();
        rtsctsCheckBox = new javax.swing.JCheckBox();
        stopBitsComboBox = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        replaySettingsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        recordFileChooseButton = new javax.swing.JButton();
        chosenRecordFileTextField = new javax.swing.JTextField();
        runPanel = new javax.swing.JPanel();
        runToggleButton = new javax.swing.JToggleButton();
        learnToggleButton = new javax.swing.JToggleButton();
        tiltLabel = new TiltLabel();
        recordToggleButton = new javax.swing.JToggleButton();
        asciiToggleButton = new javax.swing.JToggleButton();
        projectPanel = new javax.swing.JPanel();
        loadButton = new javax.swing.JButton();
        saveProjectButton = new javax.swing.JButton();
        clearProjectButton = new javax.swing.JButton();
        saveProjectAsButton = new javax.swing.JButton();
        toolsPanel = new javax.swing.JPanel();
        masterToggleButton = new javax.swing.JToggleButton();
        scriptsToggleButton = new javax.swing.JToggleButton();
        helpButton = new javax.swing.JButton();
        consoleToggleButton = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        slavesListView = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addModbusSlaveButton = new javax.swing.JButton();
        enableAllSlavesButton = new javax.swing.JButton();
        disableAllSlavesButton = new javax.swing.JButton();
        slaveListScrollPane = new javax.swing.JScrollPane();
        slavesListPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        addAutomationButton = new javax.swing.JButton();
        startAllAutomationsButton = new javax.swing.JButton();
        stopAllAutomationsButton = new javax.swing.JButton();
        automationListScrollPane = new javax.swing.JScrollPane();
        automationsListPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        settingsPanel.setLayout(new java.awt.GridBagLayout());

        linkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Link settings"));
        linkPanel.setLayout(new java.awt.GridBagLayout());

        tcpIpSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("TCP Port:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 2);
        tcpIpSettingsPanel.add(jLabel1, gridBagConstraints);

        portTextField.setText("502");
        portTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 5);
        tcpIpSettingsPanel.add(portTextField, gridBagConstraints);

        linksTabbedPane.addTab("TCP/IP", tcpIpSettingsPanel);

        jPanel1.setLayout(new java.awt.CardLayout());

        serialSettingsPanel.setLayout(new java.awt.GridBagLayout());

        comPortComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM 1" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 2);
        serialSettingsPanel.add(comPortComboBox, gridBagConstraints);

        baudRateComboBox.setEditable(true);
        baudRateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "115200", "57600", "19200", "9600" }));
        baudRateComboBox.setSelectedIndex(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 5);
        serialSettingsPanel.add(baudRateComboBox, gridBagConstraints);

        parityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No parity", "Odd parity", "Even parity" }));
        parityComboBox.setSelectedIndex(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 2);
        serialSettingsPanel.add(parityComboBox, gridBagConstraints);

        xonxoffCheckBox.setText("XON/XOFF");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 2);
        serialSettingsPanel.add(xonxoffCheckBox, gridBagConstraints);

        rtsctsCheckBox.setText("RTS/CTS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 5);
        serialSettingsPanel.add(rtsctsCheckBox, gridBagConstraints);

        stopBitsComboBox.setEditable(true);
        stopBitsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 stop bit", "1.5 stop bits", "2 stop bits" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 5);
        serialSettingsPanel.add(stopBitsComboBox, gridBagConstraints);

        jPanel1.add(serialSettingsPanel, "enabled");

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Serial communication is disabled.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jLabel3, gridBagConstraints);

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
        jPanel5.add(jButton1, gridBagConstraints);

        jPanel1.add(jPanel5, "disabled");

        linksTabbedPane.addTab("Serial", jPanel1);

        replaySettingsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Record file:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 2);
        replaySettingsPanel.add(jLabel2, gridBagConstraints);

        recordFileChooseButton.setText("Choose...");
        recordFileChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordFileChooseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 5);
        replaySettingsPanel.add(recordFileChooseButton, gridBagConstraints);

        chosenRecordFileTextField.setEditable(false);
        chosenRecordFileTextField.setText("No file selected.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 5, 5);
        replaySettingsPanel.add(chosenRecordFileTextField, gridBagConstraints);

        linksTabbedPane.addTab("Replay", replaySettingsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        linkPanel.add(linksTabbedPane, gridBagConstraints);

        runPanel.setLayout(new java.awt.GridBagLayout());

        runToggleButton.setText("Run");
        runToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        runPanel.add(runToggleButton, gridBagConstraints);

        learnToggleButton.setText("Learn");
        learnToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                learnToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        runPanel.add(learnToggleButton, gridBagConstraints);

        tiltLabel.setText("X");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        runPanel.add(tiltLabel, gridBagConstraints);

        recordToggleButton.setText("Record");
        recordToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        runPanel.add(recordToggleButton, gridBagConstraints);

        asciiToggleButton.setText("Ascii");
        asciiToggleButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        runPanel.add(asciiToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        linkPanel.add(runPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        settingsPanel.add(linkPanel, gridBagConstraints);

        projectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Project"));
        projectPanel.setLayout(new java.awt.GridBagLayout());

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        projectPanel.add(loadButton, gridBagConstraints);

        saveProjectButton.setText("Save");
        saveProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        projectPanel.add(saveProjectButton, gridBagConstraints);

        clearProjectButton.setText("Clear");
        clearProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        projectPanel.add(clearProjectButton, gridBagConstraints);

        saveProjectAsButton.setText("Save as");
        saveProjectAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        projectPanel.add(saveProjectAsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        settingsPanel.add(projectPanel, gridBagConstraints);

        toolsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tools"));
        toolsPanel.setLayout(new java.awt.GridBagLayout());

        masterToggleButton.setText("Master");
        masterToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        toolsPanel.add(masterToggleButton, gridBagConstraints);

        scriptsToggleButton.setText("Scripts");
        scriptsToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scriptsToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        toolsPanel.add(scriptsToggleButton, gridBagConstraints);

        helpButton.setText("Help");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        toolsPanel.add(helpButton, gridBagConstraints);

        consoleToggleButton.setText("Console");
        consoleToggleButton.setEnabled(false);
        consoleToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consoleToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        toolsPanel.add(consoleToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        settingsPanel.add(toolsPanel, gridBagConstraints);

        add(settingsPanel, java.awt.BorderLayout.NORTH);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        slavesListView.setBorder(javax.swing.BorderFactory.createTitledBorder("Modbus slaves"));
        slavesListView.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        addModbusSlaveButton.setText("Add");
        addModbusSlaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModbusSlaveButtonActionPerformed(evt);
            }
        });
        jPanel2.add(addModbusSlaveButton);

        enableAllSlavesButton.setText("Enable all");
        enableAllSlavesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAllSlavesButtonActionPerformed(evt);
            }
        });
        jPanel2.add(enableAllSlavesButton);

        disableAllSlavesButton.setText("Disable all");
        disableAllSlavesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableAllSlavesButtonActionPerformed(evt);
            }
        });
        jPanel2.add(disableAllSlavesButton);

        slavesListView.add(jPanel2, java.awt.BorderLayout.NORTH);

        slaveListScrollPane.setPreferredSize(new java.awt.Dimension(300, 150));

        slavesListPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
        slavesListPanel.setLayout(null);
        slavesListPanel.setLayout( new ListLayout() );
        slaveListScrollPane.setViewportView(slavesListPanel);

        slavesListView.add(slaveListScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(slavesListView);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Automation"));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        addAutomationButton.setText("Add");
        addAutomationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAutomationButtonActionPerformed(evt);
            }
        });
        jPanel4.add(addAutomationButton);

        startAllAutomationsButton.setText("Start all");
        startAllAutomationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAllAutomationsButtonActionPerformed(evt);
            }
        });
        jPanel4.add(startAllAutomationsButton);

        stopAllAutomationsButton.setText("Stop all");
        stopAllAutomationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAllAutomationsButtonActionPerformed(evt);
            }
        });
        jPanel4.add(stopAllAutomationsButton);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        automationListScrollPane.setPreferredSize(new java.awt.Dimension(300, 150));

        automationsListPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
        automationsListPanel.setLayout(null);
        automationsListPanel.setLayout( new ListLayout() );
        automationListScrollPane.setViewportView(automationsListPanel);

        jPanel3.add(automationListScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel3);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    private void startSerialLink(boolean isMaster)
    {
        //- - - - - - - - - - - -
        // GET BAUDRATE
        //- - - - - - - - - - - -

        int baudrate = 57600;

        try
        {
            String selectedBaudrate = (String)baudRateComboBox.getSelectedItem();
            baudrate = Integer.valueOf( selectedBaudrate );
        }
        catch(NumberFormatException ex)
        {
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("Baud rate error");
            dialog.append("Baudrate is not a number.");
            dialog.setVisible(true);
            return;
        }

        //- - - - - - - - - -
        // GET PARITY
        //- - - - - - - - - -
        int parity = parityComboBox.getSelectedIndex();

        //- - - - - - - - - - -
        // GET STOP BITS
        //- - - - - - - - - - -

        int stopBits = stopBitsComboBox.getSelectedIndex();

        //- - - - - - - - - - -
        // GET FLOW CONTROL
        //- - - - - - - - - - -

        boolean xonxoff = xonxoffCheckBox.isSelected();
        boolean rtscts = rtsctsCheckBox.isSelected();

        //- - - - - - - - - - - - -
        // GET COMM PORT AND START
        //- - - - - - - - - - - - -

        try
        {
            int commPortIndex = comPortComboBox.getSelectedIndex();
            currentLink = new ModbusSerialLink(modbusPalProject, commPortIndex, baudrate, parity, stopBits, xonxoff, rtscts);

            if(isMaster)
            {
                currentLink.startMaster(this);
            }
            else
            {
                currentLink.start(this);
            }

            ((TiltLabel)tiltLabel).start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("TCP/IP error");
            dialog.append("Cannot bind port " + portTextField.getText() + "\r\n");
            dialog.append("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            dialog.append("Message:"+ex.getLocalizedMessage());
            dialog.setVisible(true);
            return;
        }

    }

    private void startTcpIpLink(boolean isMaster)
    {
        //portTextField.setEnabled(false);
        int port = -1;

        try
        {
            String portNumber = portTextField.getText();
            port = Integer.valueOf(portNumber);
        }
        catch(NumberFormatException ex)
        {
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("TCP Port error");
            dialog.append("The TCP port number must be a value between 0 and 65535. The default value is 502.");
            dialog.setVisible(true);
            return;
        }

        try
        {
            System.out.printf("[%s] Start TCP/link (port=%d)\r\n", modbusPalProject.getName(), port);
            currentLink = new ModbusTcpIpLink(modbusPalProject, port);

            if( isMaster )
            {
                currentLink.startMaster(this);
            }
            else
            {
                currentLink.start(this);
            }

            ((TiltLabel)tiltLabel).start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("TCP/IP error");
            dialog.append("Cannot bind port " + portTextField.getText() + "\r\n");
            dialog.append("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            dialog.append("Message:"+ex.getLocalizedMessage());
            dialog.setVisible(true);
            return;
        }
    }

    private void startReplayLink(boolean isMaster)
    {
        File recordFile = null;

        recordFile = (File)chosenRecordFileTextField.getClientProperty("record file");
        if( recordFile==null )
        {
            recordFile = chooseRecordFile();
        }

        try
        {
            currentLink = new ModbusReplayLink(modbusPalProject, recordFile);

            if(isMaster)
            {
                currentLink.startMaster(this);
            }
            else
            {
                currentLink.start(this);
            }

            ((TiltLabel)tiltLabel).start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("Replay error");
            dialog.append("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            dialog.append("Message:"+ex.getLocalizedMessage());
            dialog.setVisible(true);
            return;
        }
    }

    /**
     * Starts the currently selected link, as if the user has clicked
     * on the "Run" button.
     */
    public void startLink()
    {
        System.out.printf("[%s] Start link\r\n", modbusPalProject.getName());
        GUITools.setAllEnabled(linksTabbedPane,false);

        masterToggleButton.setEnabled(false);
        boolean isMaster = masterToggleButton.isSelected();

        // if link is tcp/ip
        if( linksTabbedPane.getSelectedComponent()==tcpIpSettingsPanel )
        {
            startTcpIpLink(isMaster);
        }

        // if link is serial
        else if( linksTabbedPane.getSelectedComponent()==jPanel1 )
        {
            startSerialLink(isMaster);
        }

        // if link is replay:
        else if( linksTabbedPane.getSelectedComponent()==replaySettingsPanel )
        {
            startReplayLink(isMaster);
        }

        else
        {
            throw new UnsupportedOperationException("not yet implemented");
        }

        if(isMaster)
        {
            // start the master
            modbusMasterDialog.start(currentLink);
        }
    }


    /**
     * Stops the currently running link, as if the user has clicked on the
     * "Run" button.
     */
    public void stopLink()
    {
        System.out.printf("[%s] Stop link\r\n", modbusPalProject.getName());
        boolean isMaster = masterToggleButton.isSelected();


        if(isMaster)
        {
            // stop the master
            modbusMasterDialog.stop();
        }

        if( currentLink != null )
        {
            if(isMaster)
            {
                currentLink.stopMaster();
            }
            else
            {
                currentLink.stop();
            }
            ((TiltLabel)tiltLabel).stop();
            currentLink = null;
        }

        if(modbusMasterDialog!=null)
        {
            if( modbusMasterDialog.isRunning() )
            {
                modbusMasterDialog.stop();
            }
        }


        masterToggleButton.setEnabled(true);
        GUITools.setAllEnabled(linksTabbedPane,true);
    }


    public void setSerialLinkParameters(String port, int baudrate, int parity, boolean xonxoff, boolean rtscts)
    {
        comPortComboBox.setSelectedItem( port );
        baudRateComboBox.setSelectedItem( String.valueOf(baudrate) );
        parityComboBox.setSelectedIndex(parity);
        xonxoffCheckBox.setSelected(xonxoff);
        rtsctsCheckBox.setSelected(rtscts);
    }


    /**
     * this event is triggered when the user toggle the "run" button.
     * @param evt
     */
    private void runToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runToggleButtonActionPerformed

        // if run button is toggled, start the link
        if( runToggleButton.isSelected() == true )
        {
            startLink();
        }

        // otherwise, stop the link
        else
        {
            stopLink();
        }
}//GEN-LAST:event_runToggleButtonActionPerformed

    private void addModbusSlaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModbusSlaveButtonActionPerformed

        AddSlaveDialog dialog = new AddSlaveDialog();
        dialog.setVisible(true);
        if( dialog.isAdded() )
        {
            //int id = dialog.getSlaveId();
            //String name = dialog.getSlaveName();
            //ModbusSlave slave = new ModbusSlave(id);
            //slave.setName(name);
            //ModbusPal.addModbusSlave(slave);

            ModbusSlaveAddress ids[] = dialog.getTargetList();
            String name = dialog.getTargetName();
            for( int i=0; i<ids.length; i++ )
            {
                ModbusSlave slave = new ModbusSlave(ids[i]);
                slave.setName(name);
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, slave.getName(), "Hello");
                modbusPalProject.addModbusSlave(slave);
            }
        }
    }//GEN-LAST:event_addModbusSlaveButtonActionPerformed

    private void saveProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectButtonActionPerformed

        // if no project file is currently defined, then display
        // a FileChooser so that the user can choose where to save
        // the current project.
        //
        if( (modbusPalProject.projectFile==null) || (evt.getSource()==saveProjectAsButton) )
        {
            //JFileChooser saveDialog = new XFileChooser(XFileChooser.PROJECT_FILE);
            //saveDialog.showSaveDialog(this);
            File projectFile = selectProjectFileToSave(); //saveDialog.getSelectedFile();

            // if no project file is selected, do not save
            // the project (leave method)
            if( projectFile == null )
            {
                return;
            }

            // if project file already exists, ask "are you sure?"
            if( projectFile.exists() )
            {
                // TODO: ARE YOUR SURE?
            }

            modbusPalProject.projectFile = projectFile;
        }

        WorkInProgressDialog dialog = new WorkInProgressDialog("Save project","Saving project...");
        Runnable loadTask = createProjectSavingTask(modbusPalProject.projectFile, dialog);

        Thread saver = new Thread( loadTask );
        saver.setName("saver");
        saver.start();
        GUITools.align(this, dialog);
        dialog.setVisible(true);
        /*
        try
        {
            saveProject(modbusPalProject.projectFile);
            // TODO: setTitle(APP_STRING+" ("+projectFile.getName()+")");
        }
        catch (FileNotFoundException ex)
        {
            // TODO: display an error message
            Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            // TODO: diplay an error message
            Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
        }*/
}//GEN-LAST:event_saveProjectButtonActionPerformed

    /**
     * This method will load the specified project file by creating a new
     * ModbusPalProject instance, will set it as the current project by calling
     * setProject(), and return it.
     * @param path complete pathname of the project file to load
     * @return the instance of ModbusPalProject, created after the specified
     * project file, that has been set as the current project.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ModbusPalProject loadProject(String path)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        File projectFile = new File(path);
        return loadProject(projectFile);
    }

    /**
     * This method will load the specified project file by creating a new
     * ModbusPalProject instance, will set it as the current project by calling
     * setProject(), and return it.
     * @param projectFile the project file to load
     * @return the instance of ModbusPalProject, created after the specified
     * project file, that has been set as the current project.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ModbusPalProject loadProject(File projectFile)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        ModbusPalProject mpp = ModbusPalProject.load(projectFile);
        setProject(mpp);
        return mpp;
    }

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed

        File projectFile = selectProjectFileToOpen();
        if( projectFile == null )
        {
            return;
        }

        WorkInProgressDialog dialog = new WorkInProgressDialog("Load project","Loading project...");
        Runnable loadTask = createProjectLoadingTask(projectFile, dialog);

        Thread loader = new Thread( loadTask );
        loader.setName("loader");
        loader.start();
        GUITools.align(this, dialog);
        dialog.setVisible(true);
}//GEN-LAST:event_loadButtonActionPerformed







    private void addAutomationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAutomationButtonActionPerformed

        String name = Automation.DEFAULT_NAME + " #" + String.valueOf( modbusPalProject.idGenerator.createID() );
        Automation automation = new Automation( name );
        modbusPalProject.addAutomation(automation);

    }//GEN-LAST:event_addAutomationButtonActionPerformed

    /**
     * this function is triggered when the user toggles the "master"
     * button.
     * @param evt
     */
    private void masterToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterToggleButtonActionPerformed

        if( masterToggleButton.isSelected()==true )
        {
            if( modbusMasterDialog == null )
            {
                modbusMasterDialog = new ModbusMasterDialog(this);
                modbusMasterDialog.addWindowListener(this);
            }
            modbusMasterDialog.setVisible(true);
        }
        else
        {
            if( modbusMasterDialog != null )
            {
                modbusMasterDialog.setVisible(false);
                //modbusMasterDialog = null;
            }
        }
}//GEN-LAST:event_masterToggleButtonActionPerformed

    /**
     * Enables or disables the specified slave.
     * @param id the MODBUS id of the slave
     * @param enabled true to enable the slave, false to disable it
     */
    public void setModbusSlaveEnabled(ModbusSlaveAddress id, boolean enabled)
    {
        ModbusSlave slave = modbusPalProject.getModbusSlave(id, modbusPalProject.isLeanModeEnabled());
        if( slave!=null )
        {
            slave.setEnabled(enabled);
        }
    }


    /**
     * This method is called when the user clicks on the "enable all" button
     * located in the "modbus slaves" frame.
     * @param evt
     */
    private void enableAllSlavesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllSlavesButtonActionPerformed

        ModbusSlave slaves[] = modbusPalProject.getModbusSlaves();
        for(int i=0; i<slaves.length; i++)
        {
            if( slaves[i] != null )
            {
                slaves[i].setEnabled(true);
            }
        }
    }//GEN-LAST:event_enableAllSlavesButtonActionPerformed

    private void startAllAutomationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAllAutomationsButtonActionPerformed
        startAllAutomations();
}//GEN-LAST:event_startAllAutomationsButtonActionPerformed

    private void disableAllSlavesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllSlavesButtonActionPerformed

        ModbusSlave slaves[] = modbusPalProject.getModbusSlaves();
        for(int i=0; i<slaves.length; i++)
        {
            if( slaves[i] != null )
            {
                slaves[i].setEnabled(false);
            }
        }
    }//GEN-LAST:event_disableAllSlavesButtonActionPerformed

    private void stopAllAutomationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllAutomationsButtonActionPerformed
        stopAllAutomations();
    }//GEN-LAST:event_stopAllAutomationsButtonActionPerformed

    /**
     * Starts all the automations of the project
     */
    public void startAllAutomations()
    {
        System.out.printf("[%s] Start all automations\r\n", modbusPalProject.getName());
        Automation automations[] = modbusPalProject.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automations[i].start();
        }
    }

    /**
     * Stops all the automations of the project
     */
    public void stopAllAutomations()
    {
        System.out.printf("[%s] Stop all automations\r\n", modbusPalProject.getName());
        Automation automations[] = modbusPalProject.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automations[i].stop();
        }
    }

    private void scriptsToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptsToggleButtonActionPerformed

        if( scriptManagerDialog==null )
        {
            scriptsToggleButton.setSelected(false);
            // create warning dialog
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("Scripts disabled");
            dialog.append("It seems that Jython is not present on your computer. Scripting is disabled.");
            dialog.append("If you need to use your scripts, go to http://www.jython.org and install Jython on your system.");
            dialog.setVisible(true);
            return;
        }

        else
        {
            if( scriptsToggleButton.isSelected()==true )
            {
                GUITools.align(this, scriptManagerDialog);

                scriptManagerDialog.setVisible(true);
            }
            else
            {
                scriptManagerDialog.setVisible(false);
            }
        }
    }//GEN-LAST:event_scriptsToggleButtonActionPerformed

    private void learnToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_learnToggleButtonActionPerformed
        modbusPalProject.setLearnModeEnabled( learnToggleButton.isSelected() );
    }//GEN-LAST:event_learnToggleButtonActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed

        if( helpViewer==null )
        {
            helpViewer = new HelpViewer();
            helpViewer.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            helpViewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        helpViewer.setVisible(true);
        helpViewer.toFront();
        /*if( Desktop.isDesktopSupported()==true )
        {
            try
            {
                URL url = getClass().getResource("../help/index.html");
                Desktop.getDesktop().browse( url.toURI() );
            }

            catch (URISyntaxException ex)
            {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }            catch (IOException ex)
            {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
    }//GEN-LAST:event_helpButtonActionPerformed

    private void clearProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearProjectButtonActionPerformed

        // TODO:
        ModbusPalProject mpp = new ModbusPalProject();
        setProject(mpp);
        //ModbusPal.clearProject();
        // TODO:setTitle(APP_STRING);

    }//GEN-LAST:event_clearProjectButtonActionPerformed

    private void consoleToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleToggleButtonActionPerformed

        if( consoleToggleButton.isSelected()==true )
        {
            GUITools.align(this, console);
            console.setVisible(true);
        }
        else
        {
            console.setVisible(false);
        }
    }//GEN-LAST:event_consoleToggleButtonActionPerformed

    private void recordToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordToggleButtonActionPerformed

        if( recordToggleButton.isSelected() )
        {
            try {
                ModbusPalRecorder.start();
            } catch (IOException ex) {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                ModbusPalRecorder.stop();
            } catch (IOException ex) {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_recordToggleButtonActionPerformed



    private File chooseRecordFile()
    {
        XFileChooser fc = new XFileChooser( XFileChooser.RECORDER_FILE );
        fc.showOpenDialog(this);
        File src = fc.getSelectedFile();
        modbusPalProject.linkReplayFile = src;
        setReplayFile(src);
        return src;
    }


    private void recordFileChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordFileChooseButtonActionPerformed

        chooseRecordFile();

    }//GEN-LAST:event_recordFileChooseButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ErrorMessage dialog = new ErrorMessage("Close");
        dialog.setTitle("Serial link disabled");
        dialog.append("It seems that RXTX is not present on your computer. Serial communication is disabled.");
        dialog.append("If you need to use your COM ports, go to http://www.rxtx.org and install the RXTX library that suits your system.");
        dialog.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed


    /**
     * Starts all automations and starts the currently selected link
     */
    public void startAll()
    {
        System.out.printf("[%s] Start everything\r\n",modbusPalProject.getName());
        startAllAutomations();
        startLink();
    }

    /**
     * Stops all the automations and stops the currently running link
     */
    public void stopAll()
    {
        stopLink();
        stopAllAutomations();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAutomationButton;
    private javax.swing.JButton addModbusSlaveButton;
    private javax.swing.JToggleButton asciiToggleButton;
    private javax.swing.JScrollPane automationListScrollPane;
    private javax.swing.JPanel automationsListPanel;
    private javax.swing.JComboBox baudRateComboBox;
    private javax.swing.JTextField chosenRecordFileTextField;
    private javax.swing.JButton clearProjectButton;
    private javax.swing.JComboBox comPortComboBox;
    private javax.swing.JToggleButton consoleToggleButton;
    private javax.swing.JButton disableAllSlavesButton;
    private javax.swing.JButton enableAllSlavesButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton learnToggleButton;
    private javax.swing.JPanel linkPanel;
    private javax.swing.JTabbedPane linksTabbedPane;
    private javax.swing.JButton loadButton;
    private javax.swing.JToggleButton masterToggleButton;
    private javax.swing.JComboBox parityComboBox;
    private javax.swing.JTextField portTextField;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JButton recordFileChooseButton;
    private javax.swing.JToggleButton recordToggleButton;
    private javax.swing.JPanel replaySettingsPanel;
    private javax.swing.JCheckBox rtsctsCheckBox;
    private javax.swing.JPanel runPanel;
    private javax.swing.JToggleButton runToggleButton;
    private javax.swing.JButton saveProjectAsButton;
    private javax.swing.JButton saveProjectButton;
    javax.swing.JToggleButton scriptsToggleButton;
    private javax.swing.JPanel serialSettingsPanel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JScrollPane slaveListScrollPane;
    private javax.swing.JPanel slavesListPanel;
    private javax.swing.JPanel slavesListView;
    private javax.swing.JButton startAllAutomationsButton;
    private javax.swing.JButton stopAllAutomationsButton;
    private javax.swing.JComboBox stopBitsComboBox;
    private javax.swing.JPanel tcpIpSettingsPanel;
    private javax.swing.JLabel tiltLabel;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JCheckBox xonxoffCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void windowOpened(WindowEvent e)
    {
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        Object source = e.getSource();

        if( source==modbusMasterDialog )
        {
            masterToggleButton.setSelected(false);
        }
        else if( source==scriptManagerDialog )
        {
            scriptsToggleButton.setSelected(false);
        }
        else if( source==console )
        {
            consoleToggleButton.setSelected(false);
        }
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }

    @Override
    public void modbusSlaveAdded(ModbusSlave slave)
    {
        // add slave panel into the gui and refresh gui
        ModbusSlavePanel panel = new ModbusSlavePanel(this,slave);
        slavesListPanel.add( panel /*, new Integer(slave.getSlaveId())*/ );
        slave.addModbusSlaveListener(panel);
        slaveListScrollPane.validate();
    }


    private ModbusSlavePanel findModbusSlavePanel(ModbusSlaveAddress slaveId)
    {
        Component panels[] = slavesListPanel.getComponents();
        for( int i=0; i<panels.length; i++ )
        {
            if( panels[i] instanceof ModbusSlavePanel )
            {
                ModbusSlavePanel panel = (ModbusSlavePanel)panels[i];
                if( panel.getSlaveId().equals(slaveId) )
                {
                    return panel;
                }
            }
        }
        return null;
    }

    @Override
    public void modbusSlaveRemoved(ModbusSlave slave)
    {
        ModbusSlaveAddress slaveID = slave.getSlaveId();

        ModbusSlavePanel panel = findModbusSlavePanel(slaveID);

        if( panel != null )
        {
            // the dialog will be disconnect, so remove it to:
            panel.delete();

            // remove panel from the list
            slavesListPanel.remove( panel );

            // force the list to redo the layout
            slaveListScrollPane.validate();

            // force the list to be repainted
            slavesListPanel.repaint();
        }
    }


    @Override
    public void automationAdded(Automation automation, int index)
    {
        // add slave panel into the gui and refresh gui
        AutomationPanel panel = new AutomationPanel(automation, this );
        //panel.requestFocus();
        automationsListPanel.add( panel, new Integer(index) );
        automationListScrollPane.validate();
        panel.focus();
    }


    private AutomationPanel findAutomationPanel(Automation automation)
    {
        Component panels[] = automationsListPanel.getComponents();
        for( int i=0; i<panels.length; i++ )
        {
            if( panels[i] instanceof AutomationPanel )
            {
                AutomationPanel panel = (AutomationPanel)panels[i];
                if( panel.getAutomation()==automation )
                {
                    return panel;
                }
            }
        }
        return null;
    }


    @Override
    public void automationRemoved(Automation automation)
    {
        AutomationPanel panel = findAutomationPanel(automation);

        if( panel != null )
        {
            // the dialog will be disconnect, so remove it too:
            panel.disconnect();

            // remove panel from the list
            automationsListPanel.remove( panel );

            // force the list to redo the layout
            automationListScrollPane.validate();

            // force the list to be repainted
            automationsListPanel.repaint();
        }
    }

    @Override
    public void linkBroken()
    {
        GUITools.setAllEnabled(linksTabbedPane,true);
        runToggleButton.setSelected(false);
    }



    /**
     * This method must be called when the instance of ModbusPalPane is
     * disposed of, because it will do some cleaning:
     * - stopping the running threads (automations, link)
     */
    public void exit()
    {
        stopAll();

        try {
            // stop recorder
            ModbusPalRecorder.stop();
        } catch (IOException ex) {
            Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
        }

        // close all windows
    }

    /**
     * @see #showScriptManagerDialog()
     * @param tabIndex the index of the tab to display
     * @deprecated tabIndex is ignored. now there is only one list of scripts
     */
    @Deprecated
    public void showScriptManagerDialog(int tabIndex)
    {
        showScriptManagerDialog();
    }

    /**
     * Summons the Script Manager Dialog, as if the user has just
     * clicked on the "Script" button.
     */
    public void showScriptManagerDialog()
    {
        scriptManagerDialog.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        scriptManagerDialog.setVisible(true);
        scriptsToggleButton.setSelected(true);
    }

    /**
     * Gets the ModbusPalProject currently being managed by this ModbusPalPane.
     * @return the current ModbusPalPane
     */public ModbusPalProject getProject()
    {
        return modbusPalProject;
    }

    /**
      * Shows or hides the the Slaves list. Some applications that embbeds
      * ModbusPal might find it nice to hide this list.
      * @param b true to show the slaves list. false to hide it.
      */
    public void setSlavesListVisible(boolean b)
    {
        slavesListView.setVisible(b);
    }

    protected File selectProjectFileToOpen()
    {
        JFileChooser loadDialog = new XFileChooser(XFileChooser.PROJECT_FILE);
        loadDialog.showOpenDialog(null);
        File projectFile = loadDialog.getSelectedFile();
        return projectFile;
    }

    protected File selectProjectFileToSave()
    {
        JFileChooser loadDialog = new XFileChooser(XFileChooser.PROJECT_FILE);
        loadDialog.showSaveDialog(null);
        File projectFile = loadDialog.getSelectedFile();
        return projectFile;
    }

    private Runnable createProjectLoadingTask(final File projectFile, final WorkInProgressDialog dialog)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    loadProject(projectFile);
                    //TODO: setTitle(APP_STRING+" ("+projectFile.getName()+")");
                }
                catch (Exception ex)
                {
                    Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
                }

                if( dialog.isVisible() )
                {
                    dialog.setVisible(false);
                }
            }
        };
    }


    private Runnable createProjectSavingTask(final File projectFile, final WorkInProgressDialog dialog)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    saveProject(projectFile);
                    //TODO: setTitle(APP_STRING+" ("+projectFile.getName()+")");
                }
                catch (Exception ex)
                {
                    Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
                }

                if( dialog.isVisible() )
                {
                    dialog.setVisible(false);
                }
            }
        };
    }

    @Override
    public void modbusMasterTaskRemoved(ModbusMasterTask mmt)
    {
        //
    }

    @Override
    public void modbusMasterTaskAdded(ModbusMasterTask mmt)
    {
        //
    }

}
