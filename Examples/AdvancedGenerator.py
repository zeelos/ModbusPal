from modbuspal.script import PythonGenerator
from modbuspal.toolkit import NumericTextField
from modbuspal.toolkit import XMLTools
from java.awt import *
from javax.swing import *

class AdvancedGenerator(PythonGenerator):

  # Init function: 
  # - set generator icon
  # - create the control panel
  def init(self):
  
    self.setIcon("./CustomGenerator.png");
    self.createCtrlPane();
    
    
  # This function will create a control panel using Java Swing components.
  # The control panel will appear in the middle of the generator panel,
  # in the automation editor.
  def createCtrlPane(self):
  
    self.ctrlPane = JPanel();
    self.ctrlPane.setLayout( FlowLayout() );
    
    self.ctrlPane.add( JLabel("A=") );
    self.aTextField = NumericTextField(1.0);
    self.ctrlPane.add( self.aTextField );
    
    
  # Override the getControlPanel function so that the
  # control panel created in the init function is returned
  def getControlPanel(self):
    
    return self.ctrlPane;


  # Return the generated value, f(x)=ax+b
  # where a is defined by the user (in the control panel)
  # and b is the initial value of the generator (that is the
  # current value of the automation when the generator starts).
  def getValue(self,x):
  
    a = float( self.aTextField.getDouble() );
    b = self.getInitialValue();
    return a*x+b;


  # Save the parameters of this generator with XML formatting into
  # the provided output stream.
  def saveSettings(self, out):
    
    out.write("<a value=\""+ self.aTextField.getText() +"\" />\r\n");
  

  # Load the parameters of this generator from the provided DOM structure.
  def loadSettings(self,nodes):
  
    node = XMLTools.getNode(nodes,"a");
    if not (node is None) :
      value = XMLTools.getAttribute("value",node);
      self.aTextField.setText(value);
  