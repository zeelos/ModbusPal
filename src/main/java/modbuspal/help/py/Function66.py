from modbuspal.script import PythonFunction
from modbuspal.toolkit import ModbusTools
from modbuspal.toolkit import XMLTools
from modbuspal.binding import Binding_SINT32
from modbuspal.slave import ModbusSlave
from modbuspal.automation import AutomationExecutionListener
from modbuspal.automation import AutomationSelectionDialog
from modbuspal.automation import *
from javax.swing import *
from javax.swing.border import TitledBorder
from javax.swing.event import *
from java.awt import GridBagLayout
from java.awt import GridBagConstraints
from java.awt import BorderLayout
from java.awt.event import ActionListener
from java.lang import *

class Function66(PythonFunction,ChangeListener,ActionListener,AutomationExecutionListener):

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def init(self):
    
    # data of the WDS
    self.sequenceNumber = 0;
    self.configSwitch = 0;
    self.pulseCount1 = 0;
    self.pulseCount2 = 0;
    self.analog1 = 0;
    self.analog2 = 0;
    self.power = 0;
    self.jennicDIS = 0;
    self.vyear = 11;
    self.vmonth = 3;
    self.vday = 22;
    
    # bindings
    self.binding_sint32 = Binding_SINT32();
    
    # automations
    self.pulse1automation = None;
    self.pulse2automation = None;
    self.analog1automation = None;
    self.analog2automation = None;
    
    # main panel
    self.pduPanel = JPanel();
    self.pduPanel.setLayout( BorderLayout() );
    scrollPane =JScrollPane();
    self.pduPanel.add(scrollPane,BorderLayout.CENTER);
    
    mainPanel = JPanel();
    mainLayout = GridBagLayout();
    mainPanel.setLayout( mainLayout );
    scrollPane.setViewportView(mainPanel);
    
    # pulse panel
    pulsePanel = JPanel();
    pulsePanel.setBorder( TitledBorder('Pulses') );
    pulsePanel.setLayout( GridBagLayout() );
    mainPanel.add(pulsePanel);
    ct = GridBagConstraints();
    
    pulse1Label = JLabel('Pulse 1:');
    ct.gridx = 0;
    ct.gridy = 0;
    pulsePanel.add(pulse1Label,ct);
    
    self.pulse1Spinner = JSpinner( SpinnerNumberModel(0,0,Integer.MAX_VALUE,1) );
    self.pulse1Spinner.addChangeListener(self);
    ct.gridx = 1;
    ct.gridy = 0;
    pulsePanel.add(self.pulse1Spinner,ct);
    
    self.pulse1Button = JButton('...');
    self.pulse1Button.addActionListener(self);
    ct.gridx = 2;
    ct.gridy = 0;
    pulsePanel.add(self.pulse1Button,ct);
    
    pulse2Label = JLabel('Pulse 2:');
    ct.gridx = 0;
    ct.gridy = 1;    
    pulsePanel.add(pulse2Label,ct);
    
    self.pulse2Spinner = JSpinner( SpinnerNumberModel(0,0,Integer.MAX_VALUE,1) );
    self.pulse2Spinner.addChangeListener(self);
    ct.gridx = 1;
    ct.gridy = 1;
    pulsePanel.add(self.pulse2Spinner,ct);
    
    self.pulse2Button = JButton('...');
    self.pulse2Button.addActionListener(self);
    ct.gridx = 2;
    ct.gridy = 1;
    pulsePanel.add(self.pulse2Button,ct);
    
    # analog panel
    analogPanel = JPanel();
    analogPanel.setBorder( TitledBorder('Analog inputs') );
    analogPanel.setLayout( GridBagLayout() );
    mainPanel.add(analogPanel);
    ct = GridBagConstraints();
    
    analog1Label = JLabel('Analog 1:');
    ct.gridx = 0;
    ct.gridy = 0;
    analogPanel.add(analog1Label,ct);
    
    self.analog1Spinner = JSpinner( SpinnerNumberModel(0,0,4095,1) );
    self.analog1Spinner.addChangeListener(self);
    ct.gridx = 1;
    ct.gridy = 0;
    analogPanel.add(self.analog1Spinner,ct);
    
    self.analog1Button = JButton('...');
    self.analog1Button.addActionListener(self);
    ct.gridx = 2;
    ct.gridy = 0;
    analogPanel.add(self.analog1Button,ct);
    
    analog2Label = JLabel('Analog 2:');
    ct.gridx = 0;
    ct.gridy = 1;    
    analogPanel.add(analog2Label,ct);
    
    self.analog2Spinner = JSpinner( SpinnerNumberModel(0,0,4095,1) );
    self.analog2Spinner.addChangeListener(self);
    ct.gridx = 1;
    ct.gridy = 1;
    analogPanel.add(self.analog2Spinner,ct);
    
    self.analog2Button = JButton('...');
    self.analog2Button.addActionListener(self);
    ct.gridx = 2;
    ct.gridy = 1;
    analogPanel.add(self.analog2Button,ct);

    
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def stateChanged(self, changeEvent):
    
    if changeEvent.getSource()==self.pulse1Spinner:
      value=self.pulse1Spinner.getValue();
      self.pulseCount1=value;
    
    elif changeEvent.getSource()==self.pulse2Spinner:
      value=self.pulse2Spinner.getValue();
      self.pulseCount2=value;
    
    elif changeEvent.getSource()==self.analog1Spinner:
      value=self.analog1Spinner.getValue();
      self.analog1=value;
    
    elif changeEvent.getSource()==self.analog2Spinner:
      value=self.analog2Spinner.getValue();
      self.analog2=value;
  
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def reset(self):
    if self.pulse1automation is not None:
      self.pulse1automation.removeAutomationExecutionListener(self);
    if self.pulse2automation is not None:
      self.pulse2automation.removeAutomationExecutionListener(self);
    if self.analog1automation is not None:
      self.analog1automation.removeAutomationExecutionListener(self);
    if self.analog2automation is not None:
      self.analog2automation.removeAutomationExecutionListener(self);
      
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def actionPerformed(self, actionEvent):
  
    if actionEvent.getSource()==self.pulse1Button:
      automations = ModbusPal.getAutomations();
      dialog = AutomationSelectionDialog(automations, self.pulse1automation);
      dialog.setVisible(Boolean.TRUE);
      if self.pulse1automation is not None:
        self.pulse1automation.removeAutomationExecutionListener(self);
      self.pulse1automation = dialog.getSelectedAutomation();
      if self.pulse1automation is not None:
        self.pulse1automation.addAutomationExecutionListener(self);
    
    elif actionEvent.getSource()==self.pulse2Button:
      automations = ModbusPal.getAutomations();
      dialog = AutomationSelectionDialog(automations, self.pulse2automation);
      dialog.setVisible(Boolean.TRUE);
      automation = dialog.getSelectedAutomation();
      if self.pulse2automation is not None:
        self.pulse2automation.removeAutomationExecutionListener(self);
      self.pulse2automation = dialog.getSelectedAutomation();
      if self.pulse2automation is not None:
        self.pulse2automation.addAutomationExecutionListener(self);
      
    elif actionEvent.getSource()==self.analog1Button:
      automations = ModbusPal.getAutomations();
      dialog = AutomationSelectionDialog(automations, self.analog1automation);
      dialog.setVisible(Boolean.TRUE);
      if self.analog1automation is not None:
        self.analog1automation.removeAutomationExecutionListener(self);
      self.analog1automation = dialog.getSelectedAutomation();
      if self.analog1automation is not None:
        self.analog1automation.addAutomationExecutionListener(self);
    
    elif actionEvent.getSource()==self.analog2Button:
      automations = ModbusPal.getAutomations();
      dialog = AutomationSelectionDialog(automations, self.analog2automation);
      dialog.setVisible(Boolean.TRUE);
      automation = dialog.getSelectedAutomation();
      if self.analog2automation is not None:
        self.analog2automation.removeAutomationExecutionListener(self);
      self.analog2automation = dialog.getSelectedAutomation();
      if self.analog2automation is not None:
        self.analog2automation.addAutomationExecutionListener(self);

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def automationHasStarted(self, automation):
    return;
    
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def automationHasEnded(self, automation):
    return;

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def automationValueHasChanged(self, automation, time, value):
    
    if self.pulse1automation==automation:
      self.pulse1Spinner.setValue( int(value) );
      self.pulseCount1 = int(value);
    if self.pulse2automation==automation:
      self.pulse2Spinner.setValue( int(value) );
      self.pulseCount2 = int(value);
    if self.analog1automation==automation:
      self.analog1Spinner.setValue( int(value) );
      self.analog1 = int(value);
    if self.analog2automation==automation:
      self.analog2Spinner.setValue( int(value) );
      self.analog2 = int(value);

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def automationReloaded(self, automation):
    return;

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  # Byte 0: Function code (66)
  # Byte 1: Sequence number (0-255 with rollover)
  # Byte 2: Configuration switch settings
  # Byte 3-6: Pulse count 1
  # Byte 7-10: Pulse count 2
  # Byte 11-12: Analog input 1
  # Byte 13-14: Analog input 2
  # Byte 15-16: Power monitor
  # Byte 17-20: Jennic digital states from u32AHI_DIOReadInput() 
  # Byte 21-27: Reserved for future expansion
  # Byte 28: Version Year     11 (0x0B)
  # Byte 29: Version Month   03 (0x03)
  # Byte 30: Version Day      22 (0x16)
  def processPDU(self,functionCode,slaveID,buffer,offset,createIfNotExist):
    
    # increment sequence number:
    self.sequenceNumber = self.sequenceNumber+1;
    
    ModbusTools.setUint8 (buffer, offset+1,  self.sequenceNumber);
    ModbusTools.setUint8 (buffer, offset+2,  self.configSwitch);
    ModbusTools.setUint16(buffer, offset+3,  self.binding_sint32.getRegister(1,self.pulseCount1) );
    ModbusTools.setUint16(buffer, offset+5,  self.binding_sint32.getRegister(0,self.pulseCount1) );
    ModbusTools.setUint16(buffer, offset+7,  self.binding_sint32.getRegister(1,self.pulseCount2) );
    ModbusTools.setUint16(buffer, offset+9,  self.binding_sint32.getRegister(0,self.pulseCount2) );
    ModbusTools.setUint16(buffer, offset+11, self.analog1 );
    ModbusTools.setUint16(buffer, offset+13, self.analog2 );
    ModbusTools.setUint16(buffer, offset+15, self.power );
    ModbusTools.setUint16(buffer, offset+17, self.binding_sint32.getRegister(1,self.jennicDIS) );
    ModbusTools.setUint16(buffer, offset+19, self.binding_sint32.getRegister(0,self.jennicDIS) );
    ModbusTools.setUint8 (buffer, offset+28, self.vyear);
    ModbusTools.setUint8 (buffer, offset+29, self.vmonth);
    ModbusTools.setUint8 (buffer, offset+30, self.vday);
    return 31;
    
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def getClassName(self):
    return "Function66";
    
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def getPduPane(self):
    return self.pduPanel;
    
  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def savePduProcessorSettings(self,outputStream):
    
    # pulse1:
    tag = "<pulse1 value=\"" + str(self.pulseCount1) +"\"";
    if self.pulse1automation is not None:
      tag = tag + " automation=\""+self.pulse1automation.getName()+"\"";
    tag = tag + " />\r\n";
    outputStream.write(tag);
    
    # pulse2:
    tag = "<pulse2 value=\"" + str(self.pulseCount2) +"\"";
    if self.pulse2automation is not None:
      tag = tag + " automation=\""+self.pulse2automation.getName()+"\"";
    tag = tag + " />\r\n";
    outputStream.write(tag);

    # analog1
    tag = "<analog1 value=\"" + str(self.analog1)+"\"";
    if self.analog1automation is not None:
      tag = tag + " automation=\""+self.analog1automation.getName()+"\"";
    tag = tag + " />\r\n";
    outputStream.write(tag);
    
    # analog2
    tag = "<analog2 value=\"" + str(self.analog2)+"\"";
    if self.analog2automation is not None:
      tag = tag + " automation=\""+self.analog2automation.getName()+"\"";
    tag = tag + " />\r\n";
    outputStream.write(tag);

  #- - - - - - - - - - - - - - - - - - - - - - - - - - - -
  def loadPduProcessorSettings(self,nodeList):

    # pulse1:
    nodePulse1 = XMLTools.getNode(nodeList,"pulse1");
    if nodePulse1 is not None:
        self.pulseCount1 = int( XMLTools.getAttribute("value",nodePulse1) );
        self.pulse1Spinner.setValue(self.pulseCount1);
        automationName = XMLTools.getAttribute("automation",nodePulse1);
        if automationName is not None:
          self.pulse1automation = ModbusPal.getAutomation(automationName);
    
    # pulse2:
    nodePulse2 = XMLTools.getNode(nodeList,"pulse2");
    if nodePulse2 is not None:
        self.pulseCount2 = int( XMLTools.getAttribute("value",nodePulse2) );
        self.pulse2Spinner.setValue(self.pulseCount2);
        automationName = XMLTools.getAttribute("automation",nodePulse2);
        if automationName is not None:
          self.pulse2automation = ModbusPal.getAutomation(automationName);

    # analog1:
    nodeAnalog1 = XMLTools.getNode(nodeList,"analog1");
    if nodeAnalog1 is not None:
        self.analog1 = int( XMLTools.getAttribute("value",nodeAnalog1) );
        self.analog1Spinner.setValue(self.analog1);
        automationName = XMLTools.getAttribute("automation",nodeAnalog1);
        if automationName is not None:
          self.analog1automation = ModbusPal.getAutomation(automationName);

    # analog2:
    nodeAnalog2 = XMLTools.getNode(nodeList,"analog2");
    if nodeAnalog2 is not None:
        self.analog2 = int( XMLTools.getAttribute("value",nodeAnalog2) );
        self.analog2Spinner.setValue(self.analog2);
        automationName = XMLTools.getAttribute("automation",nodeAnalog2);
        if automationName is not None:
          self.analog2automation = ModbusPal.getAutomation(automationName);

Function66Instance = Function66();
ModbusPal.addFunctionInstantiator( Function66Instance );
