from modbuspal.script import PythonBinding

class CustomBinding(PythonBinding):

  def getSize(self):
    return 16;

  def getRegister(self,rank,value):
    return 555;
