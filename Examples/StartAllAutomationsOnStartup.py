from modbuspal.main import ModbusPal

# Get the list of all automations in the project
list = ModbusPal.getAutomations();

# Start every automation in the list
for automation in list:
  automation.start();
