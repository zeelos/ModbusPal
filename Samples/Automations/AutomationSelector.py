"""
AutomationSelector.py
The goal of this module is to create a dialog displaying
a list of all automations that are available in the current
ModbusPal project. The user has the ability to dismiss the
dialog by clicking the "Cancel" button, or selecting
automations and clicking the "Ok" button.
"""
from java.lang import *;
from java.awt import *;
from java.awt.event import *;
from javax.swing import *;
from modbuspal.main import *;

class AutomationList(AbstractListModel):

  def __init__(self):
    self.automations = ModbusPal.getAutomations();
  
  def getElementAt(self,index):
    return self.automations[index].getName();
  
  def getSize(self):
    return len(self.automations);
  

class AutomationSelector(JDialog, ActionListener):
  
  def actionPerformed(self, event):
    
    source = event.getSource();
    if source == self.okButton:
      self.okButtonPushed = Boolean.TRUE;
    else:
      self.okButtonPushed = Boolean.FALSE;
    self.setVisible(Boolean.FALSE);
  
  def getSelectedAutomations(self):
    
    if self.okButtonPushed==Boolean.TRUE:
      return self.autoList.getSelectedValues();
    else:
      return none;
  
  def __init__(self):
  
    self.setTitle("Automation Selector");
    self.setSize(300,200);
    self.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);

    # Create the main panel:
    mainPanel = JPanel();
    mainPanel.setLayout( BorderLayout() );
    self.add(mainPanel);
    
    # Create a panel for the label:
    lbPanel = JPanel();
    lbPanel.setLayout( FlowLayout(FlowLayout.LEFT) );
    caption = JLabel("Select:");
    caption.setToolTipText("select");
    lbPanel.add(caption);
    mainPanel.add( lbPanel, BorderLayout.NORTH );
    
    # Create a panel for the buttons:
    btPanel = JPanel();
    btPanel.setLayout( FlowLayout() );
    self.okButton = JButton("OK");
    self.okButtonPushed=Boolean.FALSE;
    self.okButton.addActionListener(self);
    self.cancelButton = JButton("Cancel");
    self.cancelButton.addActionListener(self);
    btPanel.add(self.okButton);
    btPanel.add(self.cancelButton);
    mainPanel.add(btPanel,BorderLayout.SOUTH);
    
    # Create the model for the list
    self.listModel = AutomationList();
    
    # Create the list and put it in the middle
    # of the panel.
    self.autoList = JList(self.listModel);
    scrollPane = JScrollPane();
    scrollPane.setViewportView(self.autoList);
    mainPanel.add( scrollPane, BorderLayout.CENTER );
    
    # finish:
    self.validate();
