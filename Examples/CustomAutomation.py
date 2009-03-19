"""
In this example, we will create a custom automation and add it
into the current ModbusPal project.
"""

from modbuspal.automation import Automation
from modbuspal.main import ModbusPal

# Create a new automation
automation = Automation("myCustomAutomation");

# Add the automation 
# into the application
ok = ModbusPal.addAutomation(automation);

# Start automation (only if added successfully)
if( ok ):
  automation.start();
