from modbuspal.main import ModbusPal
from modbuspal.main import ModbusConst

# Try each possible Modbus slave addresses
for addr in range(ModbusConst.FIRST_MODBUS_SLAVE, ModbusConst.LAST_MODBUS_SLAVE+1):
  
  # Get the modbus slave object corresponding to this address
  slave = ModbusPal.getModbusSlave(addr);
  
  # Verify that there actually IS a modbus slave with this address
  if not(slave is None):
  
    # Disable the slave:
    slave.setEnabled(False);
    