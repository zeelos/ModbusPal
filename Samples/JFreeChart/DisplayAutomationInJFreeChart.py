
from java.lang import *;
from javax.swing import *;
from java.awt import *;
from java.awt.event import WindowListener;
from org.jfree.data.xy import XYSeries;
from org.jfree.data.xy import XYSeriesCollection;
from org.jfree.chart import ChartFactory;
from org.jfree.chart import ChartPanel;
from org.jfree.chart.plot import PlotOrientation;
from modbuspal.main import ModbusPal;
from modbuspal.automation import AutomationValueListener;
from modbuspal.toolkit import Jythools;


class AutomationSeries(XYSeries, AutomationValueListener,WindowListener):

  def windowClosed(self,event):
    self.automation.removeAutomationValueListener(self);
    
  def automationValueHasChanged(self,source,time,value):
    while self.getItemCount() > 50:
      self.remove(0);
    self.add(time,value);
    return;
  
  def __init__(self,auto):
    XYSeries.__init__(self,auto.getName() );
    self.automation = auto;
    self.automation.addAutomationValueListener(self);
    

class AutomationChart():

  # constructor of the class
  def __init__(self,automations):
  
    # Create the frame
    frame = JFrame("Automation Viewer")
    frame.setSize(500, 300)
    frame.setLayout(BorderLayout())
    
    series = AutomationSeries
    # Finalize the window
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.setVisible(True)
    
    # Create an XY dataset
    dataset = XYSeriesCollection();

    for autoname in automations:
    
      automation = ModbusPal.getAutomation( autoname );
      series = AutomationSeries( automation );
      dataset.addSeries(series);
      frame.addWindowListener(series);
    
    # Create chart
    chart = ChartFactory.createXYLineChart("Automation Viewer","Time (seconds)","Value",dataset,PlotOrientation.VERTICAL,Boolean.TRUE,Boolean.TRUE,Boolean.FALSE);
    panel = ChartPanel(chart);
    
    # Add chart to panel
    frame.add(panel, BorderLayout.CENTER);



# First, load the AutomationSelector class and
# ask the user to choose the automations:
file = mbp_script_directory+'\..\Automations\AutomationSelector.py';
AutomationSelector = Jythools.getFromFile(file, 'AutomationSelector');
selector = AutomationSelector();
selector.setVisible(Boolean.TRUE);

# Second, retrieve the selected automations
# and display the chart
automations = selector.getSelectedAutomations();
AutomationChart(automations);
