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

import modbuspal.toolkit.XMLTools;
import modbuspal.toolkit.NumericTextField;
import java.net.URISyntaxException;
import modbuspal.slave.ModbusSlavePanel;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import modbuspal.automation.Automation;
import modbuspal.binding.BindingFactory;
import modbuspal.generator.GeneratorFactory;
import modbuspal.link.*;
import modbuspal.master.ModbusMaster;
import modbuspal.master.ModbusMasterDialog;
import modbuspal.script.ScriptManagerDialog;
import modbuspal.slave.ModbusSlave;
import modbuspal.toolkit.XFileChooser;
import org.w3c.dom.*;

/**
 *
 * @author nnovic
 */
public class ModbusPalGui
extends JFrame
implements ModbusPalXML, WindowListener, ModbusPalListener
{
    private static final String APP_STRING = "ModbusPal 1.4";
    private static ModbusPalGui uniqueInstance;





    public static JFrame getFrame()
    {
        return uniqueInstance;
    }



    /**
     * this method will try to change the Look and Feel of the applcation,
     * using the system l&f. It means that the application will get the Windows
     * l&f on Windows, etc...
     */
    private static void setNativeLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
          System.out.println("Error setting native LAF: " + e);
        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                setNativeLookAndFeel();
                uniqueInstance=new ModbusPalGui();
                ModbusPal.addModbusPalListener(uniqueInstance);
                uniqueInstance.setVisible(true);
            }
        });
    }


    
    private ModbusMaster modbusMaster = new ModbusMaster();
    private ModbusMasterDialog modbusMasterDialog = null;
    private static File projectFile = null;
    private ScriptManagerDialog scriptManagerDialog = null;
    private ModbusLink currentLink = null;
    private AppConsole console = null;





    



    static void loadLinks(Document doc)
    {
        uniqueInstance.loadLinkParameters(doc);
    }


    public static void showScriptManagerDialog(int tabIndex)
    {
        uniqueInstance.scriptManagerDialog.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        uniqueInstance.scriptManagerDialog.setVisible(true);
        uniqueInstance.scriptsToggleButton.setSelected(true);
        uniqueInstance.scriptManagerDialog.setSelectedTab(tabIndex);
    }

    void loadLinkParameters(Document doc)
    {
        NodeList linksNode = doc.getElementsByTagName("links");
        if( linksNode.getLength() == 1 )
        {
            loadLinksParameters(linksNode.item(0));
        }
        else if( linksNode.getLength() > 1 )
        {
            //TODO: error
        }
    }

    private void loadLinksParameters(Node linkRoot)
    {
        // get the "selected" attributes of the "link" openTag
        String selected = XMLTools.getAttribute("selected", linkRoot);
        if(selected.compareTo("tcpip")==0 )
        {
            linksTabbedPane.setSelectedComponent(tcpIpSettingsPanel);
        }
        else if(selected.compareTo("serial")==0 )
        {
            linksTabbedPane.setSelectedComponent(serialSettingsPanel);
        }

        // find child "tcpip" node and load parameters from it
        Node tcpipNode = XMLTools.findChild(linkRoot, "tcpip");
        loadTcpIpLinkParameters(tcpipNode);

        // find child "serial" node and load parameters from it
        Node serialNode = XMLTools.findChild(linkRoot, "serial");
        loadSerialLinkParameters(serialNode);
    }


    private void loadTcpIpLinkParameters(Node root)
    {
        // load "port" attribute
        String portValue = XMLTools.getAttribute("port", root);
        portTextField.setText(portValue);
    }


    private void loadSerialLinkParameters(Node root)
    {
        // load "com" attribute
        String comId = XMLTools.getAttribute("com", root);
        if( comId != null )
        {
            // check if "comId" exists:
            if( ModbusSerialLink.exists(comId)==true )
            {
                comPortComboBox.setSelectedItem(comId);
            }
            else
            {
                // TODO: show warning message
            }
        }
        
        // load "baudrate" attribute
        String baudrate = XMLTools.getAttribute("baudrate", root);
        if( baudrate!=null )
        {
            baudRateComboBox.setSelectedItem(baudrate);
        }
        
        // load "parity" attribute
        String parity = XMLTools.getAttribute("parity", root);
        if( parity!=null )
        {
            if( parity.compareTo("none")==0 )
            {
                parityComboBox.setSelectedIndex(ModbusSerialLink.PARITY_NONE);
            }
            else if( parity.compareTo("odd")==0 )
            {
                parityComboBox.setSelectedIndex(ModbusSerialLink.PARITY_ODD);
            }
            else
            {
                parityComboBox.setSelectedIndex(ModbusSerialLink.PARITY_EVEN);
            }
        }

        // load flow control parameters
        Node flowControlNode = XMLTools.findChild(root, "flowcontrol");
        if( flowControlNode != null )
        {
            loadFlowControlParameters(flowControlNode);
        }
    }

    private void loadFlowControlParameters(Node root)
    {
        // load xonxoff attribute
        String xonxoffValue = XMLTools.getAttribute("xonxoff", root);
        if( xonxoffValue!=null )
        {
            boolean xonxoff = Boolean.parseBoolean(xonxoffValue);
            xonxoffCheckBox.setSelected(xonxoff);
        }

        // load rtscts attribute
        String rtsctsValue = XMLTools.getAttribute("rtscts", root);
        if( rtsctsValue!=null )
        {
            boolean rtscts = Boolean.parseBoolean(rtsctsValue);
            rtsctsCheckBox.setSelected(rtscts);
        }
    }


    static void saveLinks(OutputStream out)
    throws IOException
    {
        uniqueInstance.saveLinkParameters(out);
    }

    void saveLinkParameters(OutputStream out)
    throws IOException
    {
        StringBuffer openTag = new StringBuffer("<links ");

        // fill "current" with "tcpip" or "serial" depending on the
        // currently selected tab
        Component selectedTab = linksTabbedPane.getSelectedComponent();
        if( selectedTab==tcpIpSettingsPanel )
        {
            openTag.append("selected=\"tcpip\" ");
        }
        else if( selectedTab==serialSettingsPanel )
        {
            openTag.append("selected=\"serial\" ");
        }

        // terminate open openTag and write it
        openTag.append(">\r\n");
        out.write( openTag.toString().getBytes() );

        // write tcp/ip settings
        saveTcpIpLinkParameters(out);

        // write serial settings
        saveSerialLinkParameters(out);

        // close openTag
        String closeTag = "</links>\r\n";
        out.write( closeTag.getBytes() );
    }


    private void saveTcpIpLinkParameters(OutputStream out) 
    throws IOException
    {
        StringBuffer tag = new StringBuffer("<tcpip ");
        tag.append("port=\""+ portTextField.getText() +"\" ");
        tag.append("/>\r\n");
        out.write( tag.toString().getBytes() );
    }


    private void saveSerialLinkParameters(OutputStream out) throws IOException
    {
        StringBuffer openTag = new StringBuffer("<serial ");
        openTag.append("com=\""+ (String)comPortComboBox.getSelectedItem() +"\" ");
        openTag.append("baudrate=\""+ (String)baudRateComboBox.getSelectedItem() +"\" ");
        switch(parityComboBox.getSelectedIndex())
        {
            case ModbusSerialLink.PARITY_NONE: openTag.append("parity=\"none\" "); break;
            case ModbusSerialLink.PARITY_ODD: openTag.append("parity=\"odd\" "); break;
            default:
            case ModbusSerialLink.PARITY_EVEN: openTag.append("parity=\"even\" "); break;
        }
        openTag.append(">\r\n");
        out.write( openTag.toString().getBytes() );

        StringBuffer flowControl = new StringBuffer("<flowcontrol ");
        flowControl.append("xonxoff=\""+ String.valueOf( xonxoffCheckBox.isSelected() ) +"\" ");
        flowControl.append("rtscts=\""+ String.valueOf( rtsctsCheckBox.isSelected() ) +"\" ");
        flowControl.append("/>\r\n");

        out.write( flowControl.toString().getBytes() );

        String closeTag = "</serial>\r\n";
        out.write( closeTag.getBytes() );
    }





    /** Creates new form ModbusPalGui */
    ModbusPalGui()
    {
        initComponents();
        setTitle(APP_STRING);
        installCommPorts();
        installScriptEngine();
    }


    private boolean verifyRXTX()
    {
        // try to load the CommPortVerifier class
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try
        {
            Class c = cl.loadClass("gnu.io.CommPortIdentifier");
            return true;
        }
        catch (ClassNotFoundException ex)
        {
            return false;
        }
    }

    private boolean verifyPython()
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

    private void installScriptEngine()
    {
        scriptManagerDialog = new ScriptManagerDialog(this);
        scriptManagerDialog.addWindowListener(this);
        GeneratorFactory.getFactory().addInstanciatorListener(scriptManagerDialog);
        BindingFactory.getFactory().addInstanciatorListener(scriptManagerDialog);
        ModbusPal.addScriptListener(scriptManagerDialog);

        if( verifyPython() == false )
        {
            // remove the serial settings panel
            linksTabbedPane.remove(scriptsToggleButton);
            // create warning dialog
            ErrorMessage dialog = new ErrorMessage(this,"Close");
            dialog.setTitle("Scripts disabled");
            dialog.append("It seems that Jython is not present on your computer. Scripting is disabled.");
            dialog.append("If you need to use your scripts, go to http://www.jython.org and install Jython on your system.");
            dialog.setVisible(true);
            return;
        }
    }



    private void installCommPorts()
    {
        // check if RXTX Comm lib is available
        if( verifyRXTX() == false )
        {
            // remove the serial settings panel
            linksTabbedPane.remove(serialSettingsPanel);
            // create warning dialog
            ErrorMessage dialog = new ErrorMessage(this,"Close");
            dialog.setTitle("Serial link disabled");
            dialog.append("It seems that RXTX is not present on your computer. Serial communication is disabled.");
            dialog.append("If you need to use your COM ports, go to http://www.rxtx.org and install the RXTX lib that suits your system.");
            dialog.setVisible(true);

            return;
        }

        // detect the comm ports
        ModbusSerialLink.install();
        
        // get the list of comm ports (as strings)
        // and put it in the swing list
        comPortComboBox.setModel( ModbusSerialLink.getListOfCommPorts() );
    }
    
   



    @Override
    public void tilt()
    {
        ( (TiltLabel)tiltLabel ).tilt();
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
        serialSettingsPanel = new javax.swing.JPanel();
        comPortComboBox = new javax.swing.JComboBox();
        baudRateComboBox = new javax.swing.JComboBox();
        parityComboBox = new javax.swing.JComboBox();
        xonxoffCheckBox = new javax.swing.JCheckBox();
        rtsctsCheckBox = new javax.swing.JCheckBox();
        runPanel = new javax.swing.JPanel();
        runToggleButton = new javax.swing.JToggleButton();
        learnToggleButton = new javax.swing.JToggleButton();
        tiltLabel = new TiltLabel();
        projectPanel = new javax.swing.JPanel();
        loadButton = new javax.swing.JButton();
        saveProjectButton = new javax.swing.JButton();
        clearProjectButton = new javax.swing.JButton();
        toolsPanel = new javax.swing.JPanel();
        masterToggleButton = new javax.swing.JToggleButton();
        scriptsToggleButton = new javax.swing.JToggleButton();
        helpButton = new javax.swing.JButton();
        consoleToggleButton = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        linksTabbedPane.addTab("Serial", serialSettingsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        linkPanel.add(linksTabbedPane, gridBagConstraints);

        runPanel.setLayout(new java.awt.GridBagLayout());

        runToggleButton.setText("Run");
        runToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        runPanel.add(learnToggleButton, gridBagConstraints);

        tiltLabel.setText("X");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        runPanel.add(tiltLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
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
        projectPanel.add(saveProjectButton, gridBagConstraints);

        clearProjectButton.setText("Clear");
        clearProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearProjectButtonActionPerformed(evt);
            }
        });
        projectPanel.add(clearProjectButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        settingsPanel.add(projectPanel, gridBagConstraints);

        toolsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tools"));
        toolsPanel.setLayout(new java.awt.GridBagLayout());

        masterToggleButton.setText("Master");
        masterToggleButton.setEnabled(false);
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

        getContentPane().add(settingsPanel, java.awt.BorderLayout.NORTH);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Modbus slaves"));
        jPanel1.setLayout(new java.awt.BorderLayout());

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

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        slaveListScrollPane.setPreferredSize(new java.awt.Dimension(300, 150));

        slavesListPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("List.background"));
        slavesListPanel.setLayout(null);
        slavesListPanel.setLayout( new ListLayout() );
        slaveListScrollPane.setViewportView(slavesListPanel);

        jPanel1.add(slaveListScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);

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

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void startSerialLink()
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
            ErrorMessage dialog = new ErrorMessage(this,"Close");
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
            currentLink = new ModbusSerialLink(commPortIndex, baudrate, parity, xonxoff, rtscts);
            currentLink.start();
            modbusMaster.setLink(currentLink);

            ((TiltLabel)tiltLabel).start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage(this,"Close");
            dialog.setTitle("TCP/IP error");
            dialog.append("Cannot bind port " + portTextField.getText() + "\r\n");
            dialog.append("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            dialog.append("Message:"+ex.getLocalizedMessage());
            dialog.setVisible(true);
            return;
        }

    }
    
    private void startTcpIpLink()
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
            ErrorMessage dialog = new ErrorMessage(this,"Close");
            dialog.setTitle("TCP Port error");
            dialog.append("The TCP port number must be a value between 0 and 65535. The default value is 502.");
            dialog.setVisible(true);
            return;
        }

        try
        {
            currentLink = new ModbusTcpIpLink(port);
            currentLink.start();
            modbusMaster.setLink(currentLink);

            ((TiltLabel)tiltLabel).start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            runToggleButton.doClick();
            ErrorMessage dialog = new ErrorMessage(this,"Close");
            dialog.setTitle("TCP/IP error");
            dialog.append("Cannot bind port " + portTextField.getText() + "\r\n");
            dialog.append("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            dialog.append("Message:"+ex.getLocalizedMessage());
            dialog.setVisible(true);
            return;
        }
    }
    
    
    
    /**
     * this event is triggered when the user toggle the "run" button.
     * @param evt
     */
    private void runToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runToggleButtonActionPerformed

        // if run button is toggled, start the link
        if( runToggleButton.isSelected() == true )
        {
            linksTabbedPane.setEnabled(false);
            portTextField.setEnabled(false);
            comPortComboBox.setEnabled(false);
            baudRateComboBox.setEnabled(false);
            parityComboBox.setEnabled(false);
            xonxoffCheckBox.setEnabled(false);
            rtsctsCheckBox.setEnabled(false);

            // if link is tcp/ip 
            if( linksTabbedPane.getSelectedComponent()==tcpIpSettingsPanel )
            {
                startTcpIpLink();
            }
            
            // if lnk is serial
            else if( linksTabbedPane.getSelectedComponent()==serialSettingsPanel )
            {
                startSerialLink();
            }
        }

        // otherwise, stop the link
        else
        {
            if( currentLink != null )
            {
                currentLink.stop();
                ((TiltLabel)tiltLabel).stop();
                currentLink = null;
                modbusMaster.setLink(null);
            }

            xonxoffCheckBox.setEnabled(true);
            rtsctsCheckBox.setEnabled(true);
            portTextField.setEnabled(true);
            comPortComboBox.setEnabled(true);
            baudRateComboBox.setEnabled(true);
            parityComboBox.setEnabled(true);
            linksTabbedPane.setEnabled(true);
        }
}//GEN-LAST:event_runToggleButtonActionPerformed

    private void addModbusSlaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModbusSlaveButtonActionPerformed

        AddSlaveDialog dialog = new AddSlaveDialog(this);
        dialog.setVisible(true);
        if( dialog.isAdded() )
        {
            //int id = dialog.getSlaveId();
            //String name = dialog.getSlaveName();
            //ModbusSlave slave = new ModbusSlave(id);
            //slave.setName(name);
            //ModbusPal.addModbusSlave(slave);

            int ids[] = dialog.getSlaveIds();
            String name = dialog.getSlaveName();
            for( int i=0; i<ids.length; i++ )
            {
                ModbusSlave slave = new ModbusSlave(ids[i]);
                slave.setName(name);
                ModbusPal.addModbusSlave(slave);
            }
        }
    }//GEN-LAST:event_addModbusSlaveButtonActionPerformed

    private void saveProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectButtonActionPerformed

        // if no project file is currently defined, then display
        // a FileChooser so that the user can choose where to save
        // the current project.
        //
        if( projectFile == null )
        {
            JFileChooser saveDialog = new XFileChooser(XFileChooser.PROJECT_FILE);
            saveDialog.showSaveDialog(this);
            projectFile = saveDialog.getSelectedFile();

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
        }


        try
        {
            ModbusPal.saveProject(projectFile);
            setTitle(APP_STRING+" ("+projectFile.getName()+")");
        }

        catch (FileNotFoundException ex)
        {
            // TODO: display an error message
            Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            // TODO: diplay an error message
            Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_saveProjectButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed

        // create loadGenerators dialog
        JFileChooser loadDialog = new XFileChooser(XFileChooser.PROJECT_FILE);
        loadDialog.showOpenDialog(this);
        projectFile = loadDialog.getSelectedFile();

        if( projectFile == null )
        {
            return;
        }

        final WorkInProgressDialog dialog = new WorkInProgressDialog(this,"Load project","Loading project...");
        Thread loader = new Thread( new Runnable()
        {
            public void run()
            {
                try
                {
                    ModbusPal.loadProject(projectFile);
                    setTitle(APP_STRING+" ("+projectFile.getName()+")");
                }
                catch (Exception ex)
                {
                    Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
                }

                if( dialog.isVisible() )
                {
                    dialog.setVisible(false);
                }
            }
        });

        loader.start();
        dialog.setVisible(true);
}//GEN-LAST:event_loadButtonActionPerformed







    private void addAutomationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAutomationButtonActionPerformed
        
        String name = Automation.DEFAULT_NAME + " #" + String.valueOf( ModbusPal.createID() );
        Automation automation = new Automation( name );
        ModbusPal.addAutomation(automation);

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
                modbusMasterDialog = new ModbusMasterDialog(this,modbusMaster);
                modbusMasterDialog.addWindowListener(this);
            }
            modbusMasterDialog.setVisible(true);
        }
        else
        {
            if( modbusMasterDialog != null )
            {
                modbusMasterDialog.setVisible(false);
                modbusMasterDialog = null;
            }
        }
}//GEN-LAST:event_masterToggleButtonActionPerformed

    /**
     * This method is called when the user clicks on the "enable all" button
     * located in the "modbus slaves" frame.
     * @param evt
     */
    private void enableAllSlavesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAllSlavesButtonActionPerformed

        ModbusSlave slaves[] = ModbusPal.getModbusSlaves();
        for(int i=0; i<slaves.length; i++)
        {
            if( slaves[i] != null )
            {
                slaves[i].setEnabled(true);
            }
        }
    }//GEN-LAST:event_enableAllSlavesButtonActionPerformed

    private void startAllAutomationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAllAutomationsButtonActionPerformed
        
        Automation automations[] = ModbusPal.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automations[i].start();
        }
        
}//GEN-LAST:event_startAllAutomationsButtonActionPerformed

    private void disableAllSlavesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableAllSlavesButtonActionPerformed

        ModbusSlave slaves[] = ModbusPal.getModbusSlaves();
        for(int i=0; i<slaves.length; i++)
        {
            if( slaves[i] != null )
            {
                slaves[i].setEnabled(false);
            }
        }
    }//GEN-LAST:event_disableAllSlavesButtonActionPerformed

    private void stopAllAutomationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAllAutomationsButtonActionPerformed
        
        Automation automations[] = ModbusPal.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automations[i].stop();
        }
    }//GEN-LAST:event_stopAllAutomationsButtonActionPerformed

    private void scriptsToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptsToggleButtonActionPerformed

        if( scriptsToggleButton.isSelected()==true )
        {
            scriptManagerDialog.setVisible(true);
        }
        else
        {
            scriptManagerDialog.setVisible(false);
        }
        
    }//GEN-LAST:event_scriptsToggleButtonActionPerformed

    private void learnToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_learnToggleButtonActionPerformed
        ModbusPal.setLearnModeEnabled( learnToggleButton.isSelected() );
    }//GEN-LAST:event_learnToggleButtonActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        if( Desktop.isDesktopSupported()==true )
        {
            try
            {
                Desktop.getDesktop().browse(new URI("http://modbuspal.wiki.sourceforge.net/"));
            }
            catch (URISyntaxException ex)
            {
                Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_helpButtonActionPerformed

    private void clearProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearProjectButtonActionPerformed
        
        projectFile=null;
        ModbusPal.clearProject();
        setTitle(APP_STRING);

    }//GEN-LAST:event_clearProjectButtonActionPerformed

    private void consoleToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleToggleButtonActionPerformed

        if( consoleToggleButton.isSelected()==true )
        {
            if( console==null )
            {
                try
                {
                    console = new AppConsole(this);
                    console.addWindowListener(this);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            console.setVisible(true);
        }
        else
        {
            console.setVisible(false);
        }
    }//GEN-LAST:event_consoleToggleButtonActionPerformed




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAutomationButton;
    private javax.swing.JButton addModbusSlaveButton;
    private javax.swing.JScrollPane automationListScrollPane;
    private javax.swing.JPanel automationsListPanel;
    private javax.swing.JComboBox baudRateComboBox;
    private javax.swing.JButton clearProjectButton;
    private javax.swing.JComboBox comPortComboBox;
    private javax.swing.JToggleButton consoleToggleButton;
    private javax.swing.JButton disableAllSlavesButton;
    private javax.swing.JButton enableAllSlavesButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton learnToggleButton;
    private javax.swing.JPanel linkPanel;
    private javax.swing.JTabbedPane linksTabbedPane;
    private javax.swing.JButton loadButton;
    private javax.swing.JToggleButton masterToggleButton;
    private javax.swing.JComboBox parityComboBox;
    private javax.swing.JTextField portTextField;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JCheckBox rtsctsCheckBox;
    private javax.swing.JPanel runPanel;
    private javax.swing.JToggleButton runToggleButton;
    private javax.swing.JButton saveProjectButton;
    private javax.swing.JToggleButton scriptsToggleButton;
    private javax.swing.JPanel serialSettingsPanel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JScrollPane slaveListScrollPane;
    private javax.swing.JPanel slavesListPanel;
    private javax.swing.JButton startAllAutomationsButton;
    private javax.swing.JButton stopAllAutomationsButton;
    private javax.swing.JPanel tcpIpSettingsPanel;
    private javax.swing.JLabel tiltLabel;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JCheckBox xonxoffCheckBox;
    // End of variables declaration//GEN-END:variables

    public void windowOpened(WindowEvent e)
    {
    }

    public void windowClosing(WindowEvent e)
    {
    }

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

    public void windowIconified(WindowEvent e)
    {
    }

    public void windowDeiconified(WindowEvent e)
    {
    }

    public void windowActivated(WindowEvent e)
    {
    }

    public void windowDeactivated(WindowEvent e)
    {
    }

    @Override
    public void modbusSlaveAdded(ModbusSlave slave)
    {
        // add slave panel into the gui and refresh gui
        ModbusSlavePanel panel = new ModbusSlavePanel(slave);
        slavesListPanel.add( panel, new Integer(slave.getSlaveId()) );
        slave.addModbusSlaveListener(panel);
        slaveListScrollPane.validate();
        //slavesListPanel.repaint();
    }


    private ModbusSlavePanel findModbusSlavePanel(int slaveId)
    {
        Component panels[] = slavesListPanel.getComponents();
        for( int i=0; i<panels.length; i++ )
        {
            if( panels[i] instanceof ModbusSlavePanel )
            {
                ModbusSlavePanel panel = (ModbusSlavePanel)panels[i];
                if( panel.getSlaveId()==slaveId )
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
        int slaveID = slave.getSlaveId();

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
        AutomationPanel panel = new AutomationPanel(this,automation);
        automationsListPanel.add( panel, new Integer(index) );
        automationListScrollPane.validate();
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
            panel.dispose();

            // remove panel from the list
            automationsListPanel.remove( panel );

            // force the list to redo the layout
            automationListScrollPane.validate();

            // force the list to be repainted
            automationsListPanel.repaint();
        }
    }



}
