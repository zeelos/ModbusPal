from modbuspal.script import PythonBinding

class BasicBinding(PythonBinding):

  # This binding uses a 32-bit signed integer formatting,
  # then the size is 32 bits.
  def getSize(self):
    return 32;

  # Override the getRegister() method so that it returns either
  # the least or the most significant 16-bit word of the
  # 32-bit integer.
  def getRegister(self,rank,value):
    
    # Cast value as an int
    value_as_a_32bit_int = int(value);

    # If rank is 0, extract the least significant 16-bit word
    if rank==0:
      value = value_as_a_32bit_int & 0xFFFF;
      return value;
      
    # If rank is 1, extract the most significant 16-bit word.
    elif rank==1:
      value = (value_as_a_32bit_int>>16) & 0xFFFF;
      return value;
      
    # It should never happen but, just in case, 
    # treat the higher ranks.
    else:
    
      # If the 32-bit value is positive, then higher 16-bit words are 0x0000
      if value_as_a_32bit_int >= 0:
        return 0x0000;
        
      # If the 32-bit value is negative, then higher 16-bit words are 0xFFFF
      else:
        return 0xFFFF;
       
bb = BasicBinding();
ModbusPal.addBindingInstantiator(bb);
