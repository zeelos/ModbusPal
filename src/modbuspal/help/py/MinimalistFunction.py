from modbuspal.script import PythonFunction
from modbuspal.toolkit import ModbusTools

class MinimalistFunction(PythonFunction):

  def processPDU(self,functionCode,slaveID,buffer,offset,createIfNotExist):
    # offset+0 holds the function code, do not touch
    # put value 123 if the next byte of the reply
    ModbusTools.setUint8 (buffer, offset+1,  123);
    # the reply is only two-byte long
    return 2;

mf = MinimalistFunction();
ModbusPal.addFunctionInstantiator(mf);
