from modbuspal.script import PythonGenerator

class CustomGenerator(PythonGenerator):

  def getValue(self,time):
    return time;
