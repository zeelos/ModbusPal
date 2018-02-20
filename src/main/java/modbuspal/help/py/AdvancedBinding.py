from modbuspal.script import PythonBinding
from java.util import Calendar

class AdvancedBinding(PythonBinding):

  def getClassName(self):
    return "AdvancedBinding";

  def getSize(self):
    return 3*16;

  # Assuming that the provided "value" is a Unix timestamp (32-bit integer
  # value representing the number of seconds since 1st January 1970),
  # this binding will transfom that timestamp to the following formatting:
  # - register #0 will contain minutes in the high byte, and seconds in the low byte
  # - register #1 will contain days in the high byte, and hours in the low byte
  # - register #2 will contain years in the high byte, and months in the low byte
  def getRegister(self,rank,value):
    
    unix_timestamp = long(value);
    cal = Calendar.getInstance();
    cal.setTimeInMillis(  unix_timestamp * 1000 );
    
    if rank==0 :
      second = cal.get( Calendar.SECOND );
      minute = cal.get( Calendar.MINUTE );
      return minute * 256 + second;
      
    elif rank==1 :
      hour = cal.get( Calendar.HOUR_OF_DAY );
      day = cal.get( Calendar.DAY_OF_MONTH ) ;
      return day * 256 + hour;
      
    elif rank==2 :
      month = cal.get( Calendar.MONTH ) ;
      year = cal.get( Calendar.YEAR) % 100;
      return year * 256 + month;
      
    else:
      return 0;

ab = AdvancedBinding();
ModbusPal.addBindingInstantiator(ab);
