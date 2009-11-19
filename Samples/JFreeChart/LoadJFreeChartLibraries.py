
from java.lang import *
from modbuspal.toolkit import ClassPathHacker

# Hack the class path
ClassPathHacker.addFile( mbp_script_directory+'\jcommon-1.0.16.jar' );
ClassPathHacker.addFile( mbp_script_directory+'\jfreechart-1.0.13.jar' );
