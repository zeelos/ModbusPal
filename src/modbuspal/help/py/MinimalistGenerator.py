from modbuspal.script import PythonGenerator

class MinimalistGenerator(PythonGenerator):

  def getValue(self,time):
    return time;

mg = MinimalistGenerator();
ModbusPal.addGeneratorInstantiator(mg);
