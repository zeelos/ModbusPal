"""
Create a modbus slave with the following characteristics:
- slave address is 17
- slave name is "myDevice"
"""

from modbuspal.slave import ModbusSlave
from modbuspal.main import ModbusPal

# Create the modbus slave with slave address 17
slave = ModbusSlave(17)

# Add the slave into the application
# Warning! under certain circumstances, the "slave" object
# can be modified by the application
slave = ModbusPal.addModbusSlave(slave)

# Change name of the modbus slave
# note: you can do it before adding the
# slave into the application.
slave.setName("myDevice")
