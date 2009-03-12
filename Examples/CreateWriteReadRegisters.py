"""
Create a modbus slave with holding registers,
write a value into a register and read it back.
"""

from modbuspal.slave import ModbusSlave
from modbuspal.main import ModbusPal

# Create the modbus slave:
slave = ModbusSlave(17)
slave = ModbusPal.addModbusSlave(slave)

# Create holding registers from 5 to 15
# (starting address is 5, quantity of registers is 10)
registers = slave.getHoldingRegisters()
registers.create(5,10)

# Write register values
registers.setRegister(5, 1000)
registers.setRegister(11, 2000)

# Read register values
value1 = registers.getRegister(5)
value2 = registers.getRegister(11)
