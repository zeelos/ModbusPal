from modbuspal.script import PythonGenerator

class CustomGenerator(PythonGenerator):

  def getValue(self,time):
    return time;

  def init(self):
    self.setIcon("./CustomGenerator.png");