from modbuspal.python import PythonGenerator

class CustomGenerator(PythonGenerator):

  def getValue(self,time):
    return time;
