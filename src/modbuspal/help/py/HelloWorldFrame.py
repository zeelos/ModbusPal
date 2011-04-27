from javax.swing import *
from java.awt import *

class HelloWorldFrame(JFrame):

  def buttonPushed(self,event):
      
    self.setVisible(False);

  def __init__(self):
    
    self.setTitle("Hello world");
    self.setSize(300, 100);
    self.setLayout(BorderLayout());
    self.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    
    # create the Label
    self.label = JLabel('Hello, world !');
    self.add(self.label, BorderLayout.CENTER);
    
    # create the button
    self.button = JButton('OK',actionPerformed=self.buttonPushed);
    self.add(self.button, BorderLayout.SOUTH);



# Create the Hello world frame:
frame = HelloWorldFrame();

# Make it visible:
frame.setVisible(True);

# Make it the top window:
frame.toFront();

