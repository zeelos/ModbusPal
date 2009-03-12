"""
Create a JFrame containing a text field and a button.
The button will enable (and create if necessary) a modbus slave
with the address or name specified in the text field.
"""

from javax.swing import *
from java.awt import *
from modbuspal.main import ModbusPal
from modbuspal.slave import ModbusSlave

class EnableButton:


  def toggleSlaveById(self, id):
    
    # check if slave exists:
    slave = ModbusPal.getModbusSlave(id)
    
    # if not, create it:
    if slave==None:
      slave = ModbusSlave(id)
      slave = ModbusPal.addModbusSlave(slave)
      
    # if so, change its "enable" state:
    else:
      enabledState = slave.isEnabled()
      slave.setEnabled( not enabledState )


  def toggleSlavesByName(self, name):
    
    # get slaves with the same name
    slaves = ModbusPal.findModbusSlaves(name)
    
    # if none, do nothing
    if slaves==None:
      return
      
    # invert "enable" status of all slaves in the list
    for slave in slaves:
      enabledState = slave.isEnabled()
      slave.setEnabled( not enabledState )


  def buttonPushed(self,event):
      
    # read the content of the text field
    txt = self.textField.getText()
    
    try:
      # check if 'txt' can be converted to an interger value
      slaveId = int(txt)
      self.toggleSlaveById(slaveId)
      
    except ValueError:
      self.toggleSlavesByName(txt)
      

  def __init__(self):
    
    # create the JFrame
    frame = JFrame("Enable Button")
    frame.setSize(300, 200)
    frame.setLayout(BorderLayout())
    
    # create the text field
    self.textField = JTextField('Type address or name here')
    frame.add(self.textField, BorderLayout.NORTH)
    
    # create the button
    button = JButton('Enable/Disable',actionPerformed=self.buttonPushed)
    frame.add(button, BorderLayout.SOUTH)
    
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.setVisible(True)

EnableButton()
