/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.Automation;
import modbuspal.script.ScriptListener;
import modbuspal.script.ScriptRunner;
import modbuspal.slave.ModbusSlave;
import org.xml.sax.SAXException;

/**
 *
 * @author nnovic
 */
public class ModbusPal
implements ModbusPalXML, ModbusConst
{
    public static final String APP_STRING = "ModbusPal 1.6";
    public static final String BASE_REGISTRY_KEY = "modbuspal";
    
    @Deprecated
    public static void addScript(File scriptFile)
    {
        uniqueInstance.modbusPalProject.addScript(scriptFile);
    }

    @Deprecated
    public static void removeScript(ScriptRunner runner)
    {
        uniqueInstance.modbusPalProject.removeScript(runner);
    }

    @Deprecated
    public static void addStartupScript(File scriptFile)
    {
        uniqueInstance.modbusPalProject.addStartupScript(scriptFile);
    }

    /*private static void removeAllScripts()
    {
        removeStartupScripts();
        removeOndemandScripts();
    }*/
    
    /*private static void removeStartupScripts()
    {
        ScriptRunner list[] = new ScriptRunner[0];
        list = startupScripts.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            removeStartupScript( list[i] );
        }
    }*/

    @Deprecated
    public static void removeStartupScript(ScriptRunner script)
    {
        uniqueInstance.modbusPalProject.removeStartupScript(script);
    }

    /*private static void removeOndemandScripts()
    {
        ScriptRunner list[] = new ScriptRunner[0];
        list = ondemandScripts.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            removeOndemandScript( list[i] );
        }
    }*/

    /*private static void removeOndemandScript(ScriptRunner script)
    {
        if( ondemandScripts.contains(script) )
        {
            ondemandScripts.remove(script);
            notifyScriptRemoved(script);
        }

    }*/

    //
    //
    // SLAVES
    //
    //


    /**
     * Returns a reference to a ModbusSlave object, which holds all the
     * information on the slave with the specified address.
     * @param address the address of the slave you want to get
     * @return the object which represents the slave, or null if there is no
     * slave with the specified address.
     */
    @Deprecated
    public static ModbusSlave getModbusSlave(int address)
    {
        return uniqueInstance.modbusPalProject.knownSlaves[address];
    }

    /**
     * Returns an array of ModbusSlave objects, which hold all the
     * information on the slaves with the specified name. In ModbusPal, multiple
     * slaves can have the same name.
     * @param name the name of the slaves you want to get
     * @return an array of ModbusSlave objects, each slave in the array having the
     * specified name.
     */
    @Deprecated
    public static ModbusSlave[] findModbusSlaves(String name)
    {
        return uniqueInstance.modbusPalProject.findModbusSlaves(name);
    }

    /**
     * Count how many modbus slaves are defined in the current project.
     * @return the number of modbus slaves defined in the project.
     */
    @Deprecated
    public static int getModbusSlaveCount()
    {
        return uniqueInstance.modbusPalProject.getModbusSlaveCount();
    }


    /**
     * Get all the slaves indexed by their modbus address.
     * @return an array containing the modbus slaves currently defined in the
     * application. 
     */
    @Deprecated
    public static ModbusSlave[] getModbusSlaves()
    {
        return uniqueInstance.modbusPalProject.getModbusSlaves();
    }

    /**
     * This method adds a modbus slave into the application. If a modbus slave
     * with the same id already exists, a dialog will popup and invite the user
     * to choose between keeping the existing slave or replacing it by the new.
     * @param slave the new modbus slave to add
     * @return a reference on the new or existing modbus slave, depending on the
     * user's choice. null if an error occured.
     */
    @Deprecated
    public static ModbusSlave submitModbusSlave(ModbusSlave slave)
    {
        return uniqueInstance.modbusPalProject.submitModbusSlave(slave);
    }

    /**
     * This method adds a modbus slave into the application. The addition will
     * fail a modbus slave with the same id already exists in thecurrent project.
     * @param slave the new modbus slave to add
     * @return true if added successfully, false otherwise.
     */
    @Deprecated
    public static boolean addModbusSlave(ModbusSlave slave)
    {
        return uniqueInstance.modbusPalProject.addModbusSlave(slave);
    }


    @Deprecated
    public static void removeModbusSlave(ModbusSlave slave)
    {
        uniqueInstance.modbusPalProject.removeModbusSlave(slave);
    }


    @Deprecated
    public static void duplicateModbusSlave(int id, String name, ModbusSlave model)
    {
        uniqueInstance.modbusPalProject.duplicateModbusSlave(id, name, model);
    }




    /*private static void removeAllModbusSlaves()
    {
        for(int i=0; i<knownSlaves.length; i++ )
        {
            if( knownSlaves[i]!=null )
            {
                removeModbusSlave( knownSlaves[i] );
            }
        }
    }*/


    @Deprecated
    public static void setSlaveEnabled(int slaveID, boolean b)
    {
        uniqueInstance.modbusPalProject.setSlaveEnabled(slaveID,b);
    }

    /**
     * determine if the slave is enabled or not. If "learn mode" is enabled, this
     * method will always return true. Otherwise, it will return true only if the
     * slave exists in the list of known slaves AND if that slave is enabled.
     * In all cases, if the slave with the provided ID doesn't exist in the list
     * of known slaves, it will be added to the list.
     * @param slaveID
     * @return true if the slave is enabled; false otherwise.
     */
    @Deprecated
    public static boolean isSlaveEnabled(int slaveID)
    {
        return uniqueInstance.modbusPalProject.isSlaveEnabled(slaveID);
    }

    //
    //
    // FUNCTIONS
    //
    //

    @Deprecated
    public static boolean isFunctionEnabled(int slaveID, byte functionCode)
    {
        return uniqueInstance.modbusPalProject.isFunctionEnabled(slaveID, functionCode);
    }

    
    //
    //
    // AUTOMATIONS
    //
    //



    /**
     * Find the automation which exactly matches the specified name.
     * @param name is the name of the automation to obtain
     * @return the Automation object associated with the specified name, or null
     * is no automation exists with that name.
     */
    @Deprecated
    public static Automation getAutomation(String name)
    {
        return uniqueInstance.modbusPalProject.getAutomation(name);
    }

    @Deprecated
    public static boolean automationExists(String name)
    {
        return uniqueInstance.modbusPalProject.automationExists(name);
    }


    /**
     * the goal of this method is to verify that each automation in the project
     * has a unique name.
     * @param auto
     * @param name
     * @return
     */
    @Deprecated
    public static String checkAutomationNewName(Automation auto, String name)
    {
        return uniqueInstance.modbusPalProject.checkAutomationNewName(auto, name);
    }



    // TODO: is it safe ???
    /**
     * Get all the automations that are defined in the current project.
     * @return an array containing all the automations that are defined in
     * the project.
     */
    @Deprecated
    public static Automation[] getAutomations()
    {
        return uniqueInstance.modbusPalProject.getAutomations();
    }


    /**
     * This method adds an automation slave into the application. If an automation
     * with the same name already exists, a dialog will popup and invite the user
     * to choose between keeping the existing automation or replacing it by the new.
     * @param automation the new automation to add
     * @return a reference on the new or existing automation, depending on the
     * user's choice. null if an error occured.
     */
    @Deprecated
    public static Automation submitAutomation(Automation automation)
    {
        return uniqueInstance.modbusPalProject.submitAutomation(automation);
    }


    /**
     * Add the provided automation into the current application. Please note that
     * each automation in the project must have a unique name. The addition will
     * fail if an existing automation already uses the same name as the automation
     * you want to add.
     * @param automation
     * @return true if the automation is added successfully, false otherwise.
     */
    @Deprecated
    public static boolean addAutomation(Automation automation)
    {
        return uniqueInstance.modbusPalProject.addAutomation(automation);
    }


    @Deprecated
    public static void startAllAutomations()
    {
        uniqueInstance.modbusPalProject.startAllAutomations();
    }


    @Deprecated
    public static void stopAllAutomations()
    {
        uniqueInstance.modbusPalProject.stopAllAutomations();
    }


    @Deprecated
    public static void removeGeneratorScript(ScriptRunner runner)
    {
        uniqueInstance.modbusPalProject.removeGeneratorScript(runner);
    }

    @Deprecated
    public static void removeBindingScript(ScriptRunner runner)
    {
        uniqueInstance.modbusPalProject.removeBindingScript(runner);
    }



    /**
     * remove all instances of the generator whose name is passed
     * in argument. the method will scan all automations of the current
     * project and remove each instance of the generator identified
     * by the provided name.
     * @param classname
     */
    @Deprecated
    private static void removeAllBindings(String classname)
    {
        uniqueInstance.modbusPalProject.removeAllBindings(classname);
    }

    @Deprecated
    public static void removeAutomation(Automation automation)
    {
        uniqueInstance.modbusPalProject.removeAutomation(automation);
    }

    @Deprecated
    private static void removeAllAutomations()
    {
        uniqueInstance.modbusPalProject.removeAllAutomations();
    }


    //TODO: is this method really necessary???
    /*static long createID()
    {
        return idGenerator.createID();
    }*/


    //
    //
    // LISTENERS
    //
    //

    /*public static void removeAllListeners()
    {
        listeners.clear();
        scriptListeners.clear();
    }*/

    @Deprecated
    public static void addModbusPalListener(ModbusPalListener l)
    {
        uniqueInstance.modbusPalProject.addModbusPalListener(l);
    }

    @Deprecated
    public static void removeModbusPalListener(ModbusPalListener l)
    {
        uniqueInstance.modbusPalProject.removeModbusPalListener(l);
    }

    @Deprecated
    public static void addScriptListener(ScriptListener l)
    {
        uniqueInstance.modbusPalProject.addScriptListener(l);
    }

    @Deprecated
    public static void removeScriptListener(ScriptListener l)
    {
        uniqueInstance.modbusPalProject.removeScriptListener(l);
    }

    //
    //
    // EVENTS
    //
    //

    @Deprecated
    public static void notifyPDUprocessed()
    {
        uniqueInstance.modbusPalProject.notifyPDUprocessed();
    }

    @Deprecated
    public static void notifyExceptionResponse()
    {
        uniqueInstance.modbusPalProject.notifyExceptionResponse();
    }







    /*private static void notifyStartupScriptRemoved(ScriptRunner runner)
    {
        for(ScriptListener l:scriptListeners)
        {
            l.startupScriptRemoved(runner);
        }
    }*/


    /*private static void notifyStartupScriptRemove(ScriptRunner script)
    {
        for(ScriptListener l:scriptListeners)
        {
            l.startupScriptRemoved(script);
        }
    }*/

    //
    //
    // SAVE PROJECT
    //
    //
    @Deprecated
    public static void saveProject(File target)
    throws FileNotFoundException, IOException
    {
        uniqueInstance.modbusPalProject.projectFile = target;
        uniqueInstance.modbusPalProject.save();
    }








    //
    //
    // " PROJECT
    //
    //
    @Deprecated
    public static void loadProject(File source)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        ModbusPalProject mpp = ModbusPalProject.load(source);
        uniqueInstance.setProject(mpp);
    }


























    

    //
    //
    // GETTERS AND SETTERS
    //
    //

    @Deprecated
    public static void setLearnModeEnabled(boolean en)
    {
        uniqueInstance.modbusPalProject.setLearnModeEnabled(en);
    }

    @Deprecated
    public static byte getHoldingRegisters(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return uniqueInstance.modbusPalProject.getHoldingRegisters(slaveID, startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public static byte setHoldingRegisters(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return uniqueInstance.modbusPalProject.setHoldingRegisters(slaveID, startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public static boolean holdingRegistersExist(int slaveID, int startingAddress, int quantity)
    {
        return uniqueInstance.modbusPalProject.holdingRegistersExist(slaveID, startingAddress, quantity);
    }

    @Deprecated
    public static byte getCoils(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return uniqueInstance.modbusPalProject.getCoils(slaveID, startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public static byte setCoils(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return uniqueInstance.modbusPalProject.setCoils(slaveID, startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public static byte setCoil(int slaveID, int address, int value)
    {
        return uniqueInstance.modbusPalProject.setCoil(slaveID, address, value);
    }

    @Deprecated
    public static boolean coilsExist(int slaveID, int startingAddress, int quantity)
    {
        return uniqueInstance.modbusPalProject.coilsExist(slaveID, startingAddress, quantity);
    }

    /*public static void clearProject()
    {
        //TODO: put link in modbuspal instead of modbuspalgui
//        if( isRunning()==true )
//        {
//            stop();
//        }

        resetParameters();
        removeAllModbusSlaves();
        removeAllAutomations();
        GeneratorFactory.getFactory().clear();
        BindingFactory.getFactory().clear();
        removeAllScripts();
    }*/


    /*private static void resetParameters()
    {
        // save id creator:
        idGenerator.reset();
    }*/

    @Deprecated
    public static void addGeneratorInstanciator(File scriptFile)
    {
        uniqueInstance.modbusPalProject.addGeneratorInstanciator(scriptFile);
    }

    @Deprecated
    public static void addBindingInstanciator(File scriptFile)
    {
        uniqueInstance.modbusPalProject.addBindingInstanciator(scriptFile);
    }




    //==========================================================================


    private static ModbusPalPane uniqueInstance;


    public static class ModbusPalInternalFrame
    extends JInternalFrame
    implements InternalFrameListener
    {
        final ModbusPalPane modbusPal;
        
        public ModbusPalInternalFrame()
        {
            setTitle(APP_STRING);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(false);
            add( modbusPal, BorderLayout.CENTER );
            pack();
            addInternalFrameListener(this);
            uniqueInstance = modbusPal;
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setFrameIcon( new ImageIcon(image2) );
        }

        public void internalFrameOpened(InternalFrameEvent e) {
        }

        public void internalFrameClosing(InternalFrameEvent e) {
            modbusPal.exit();
        }

        public void internalFrameClosed(InternalFrameEvent e) {
        }

        public void internalFrameIconified(InternalFrameEvent e) {
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
        }

        public void internalFrameActivated(InternalFrameEvent e) {
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
        }
    }


    public static class ModbusPalFrame
    extends JFrame
    {
        final ModbusPalPane modbusPal;

        public ModbusPalFrame()
        {
            setTitle(APP_STRING);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(false); // SET TO TRUE
            add( modbusPal, BorderLayout.CENTER );
            pack();
            uniqueInstance = modbusPal;
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setIconImage(image2);
        }
    }


    public static ModbusPalFrame newFrame(String name)
    {
        ModbusPal.ModbusPalFrame frame = new ModbusPal.ModbusPalFrame();
        
        return frame;
    }





    public static void showScriptManagerDialog(int tabIndex)
    {
        uniqueInstance.scriptManagerDialog.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        uniqueInstance.scriptManagerDialog.setVisible(true);
        uniqueInstance.scriptsToggleButton.setSelected(true);
        uniqueInstance.scriptManagerDialog.setSelectedTab(tabIndex);
    }






}
