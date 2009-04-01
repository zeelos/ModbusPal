/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.Automation;
import modbuspal.automation.InstanciatorFactory;
import modbuspal.binding.Binding;
import modbuspal.slave.ModbusSlave;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author nnovic
 */
public class ModbusPal
implements ModbusPalXML
{
    public static final String BASE_REGISTRY_KEY = "modbuspal";
    private static ModbusSlave[] knownSlaves = new ModbusSlave[256];
    private static Vector<Automation> automations = new Vector<Automation>();
    private static IdGenerator idGenerator = new IdGenerator();
    private static ArrayList<ModbusPalListener> listeners = new ArrayList<ModbusPalListener>();
    private static boolean learnModeEnabled = false;


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
    public static ModbusSlave getModbusSlave(int address)
    {
        return knownSlaves[address];
    }

    /**
     * Returns an array of ModbusSlave objects, which hold all the
     * information on the slaves with the specified name. In ModbusPal, multiple
     * slaves can have the same name.
     * @param name the name of the slaves you want to get
     * @return an array of ModbusSlave objects, each slave in the array having the
     * specified name.
     */
    public static ModbusSlave[] findModbusSlaves(String name)
    {
        ArrayList<ModbusSlave> found = new ArrayList<ModbusSlave>();

        for( int i=0; i<knownSlaves.length; i++ )
        {
            ModbusSlave slave = knownSlaves[i];
            if( slave != null )
            {
                if( slave.getName().compareTo(name)==0 )
                {
                    found.add(slave);
                }
            }
        }

        if( found.size()==0 )
        {
            return null;
        }
        else
        {
            ModbusSlave retval[] = new ModbusSlave[0];
            retval = found.toArray(retval);
            return retval;
        }
    }

    /**
     * Count how many modbus slaves are defined in the current project.
     * @return the number of modbus slaves defined in the project.
     */
    public static int getModbusSlaveCount()
    {
        int count = 0;
        for(int i=0; i<knownSlaves.length; i++)
        {
            if( knownSlaves[i]!=null )
            {
                count++;
            }
        }
        return count;
    }


    /**
     * Get all the slaves indexed by their modbus address.
     * @return an array containing the modbus slaves currently defined in the
     * application. 
     */
    public static ModbusSlave[] getModbusSlaves()
    {
        return knownSlaves;
    }

    /**
     * This method adds a modbus slave into the application. If a modbus slave
     * with the same id already exists, a dialog will popup and invite the user
     * to choose between keeping the existing slave or replacing it by the new.
     * @param slave the new modbus slave to add
     * @return a reference on the new or existing modbus slave, depending on the
     * user's choice. null if an error occured.
     */
    public static ModbusSlave submitModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();

        // check if slaveID is already assigned:
        if( knownSlaves[slaveID] != null )
        {
            // show a dialog to let the user decide
            // what to do in order to resolve the conflict:
            ErrorMessage conflict = new ErrorMessage(2);
            conflict.setTitle("Address conflict");
            conflict.append("You are trying to add a new slave with address " + slaveID + ".");
            conflict.append("An existing slave already uses this address. What do you want to do ?");
            conflict.setButton(0, "Keep existing");
            conflict.setButton(1, "Replace with new");
            conflict.setVisible(true);

            // if "Keep existing" is chosen:
            if( conflict.getButton()==0 )
            {
                return knownSlaves[slaveID];
            }

            else
            {
                // before replacing with new, remove old:
                removeModbusSlave(knownSlaves[slaveID]);
            }
        }

        if( addModbusSlave(slave)==true )
        {
            return slave;
        }
        return null;
    }

    /**
     * This method adds a modbus slave into the application. The addition will
     * fail a modbus slave with the same id already exists in thecurrent project.
     * @param slave the new modbus slave to add
     * @return true if added successfully, false otherwise.
     */
    public static boolean addModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();

        // check if slaveID is already assigned:
        if( knownSlaves[slaveID] != null )
        {
            return false;
        }

        knownSlaves[slaveID] = slave;
        notifySlaveAdded(slave);
        return true;
    }


    public static void removeModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();
        slave.removeAllListeners();
        // delete slave from list
        knownSlaves[slaveID] = null;
        notifySlaveRemoved(slave);
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
    public static boolean isSlaveEnabled(int slaveID)
    {

        if( knownSlaves[slaveID] == null )
        {
            if( learnModeEnabled == true )
            {
                // create a new modbus slave
                ModbusSlave slave = new ModbusSlave(slaveID);
                addModbusSlave( slave );
                return slave.isEnabled();
            }
            else
            {
                return false;
            }
        }

        else
        {
            return knownSlaves[slaveID].isEnabled();
        }
    }

    //
    //
    // FUNCTIONS
    //
    //

    public static boolean isFunctionEnabled(int slaveID, byte functionCode)
    {
        //TODO: implement isFunctionEnabled
        return true;
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
    public static Automation getAutomation(String name)
    {
        for(int i=0; i<automations.size(); i++)
        {
            Automation automation = automations.get(i);
            if( automation.getName().compareTo(name)==0 )
            {
                return automation;
            }
        }
        return null;
    }


    public static boolean automationExists(String name)
    {
        return (getAutomation(name)!=null);
    }


    /**
     * the goal of this method is to verify that each automation in the project
     * has a unique name.
     * @param auto
     * @param name
     * @return
     */
    static String checkAutomationNewName(Automation auto, String name)
    {
        // TODO: is synchronization required??
        //synchronized(this)
        {
            Automation already = getAutomation(name);
            if( (already != null) && (already != auto) )
            {
                // check if the name to alter already end with "#n"
                if( name.matches(".*#(\\d+)$")==true )
                {
                    int pos = name.lastIndexOf('#');
                    name = name.substring(0, pos);
                }
                else
                {
                    name = name.trim() + " ";
                }
                name = name + "#" + String.valueOf( createID() );
            }
            auto.setName(name);
        }
        return name;
    }



    // TODO: is it safe ???
    /**
     * Get all the automations that are defined in the current project.
     * @return an array containing all the automations that are defined in
     * the project.
     */
    public static Automation[] getAutomations()
    {
        Automation[] out = new Automation[0];
        return automations.toArray(out);
    }


    /**
     * This method adds an automation slave into the application. If an automation
     * with the same name already exists, a dialog will popup and invite the user
     * to choose between keeping the existing automation or replacing it by the new.
     * @param automation the new automation to add
     * @return a reference on the new or existing automation, depending on the
     * user's choice. null if an error occured.
     */
    public static Automation submitAutomation(Automation automation)
    {
        // check if an automation already exists with the same name
        String name = automation.getName();
        Automation existing = getAutomation(name);

        if( existing != null )
        {
            // show a dialog to let the user decide
            // what to do in order to resolve the conflict:
            ErrorMessage conflict = new ErrorMessage(2);
            conflict.setTitle("Address conflict");
            conflict.append("You are trying to add a new automation with name \"" + name + "\".");
            conflict.append("An existing automation already exists with this name. What do you want to do ?");
            conflict.setButton(0, "Keep existing");
            conflict.setButton(1, "Replace with new");
            conflict.setVisible(true);

            // if "Keep existing" is chosen:
            if( conflict.getButton()==0 )
            {
                return existing;
            }

            else
            {
                // before replacing with new, remove old:
                removeAutomation(existing);
            }
        }

        if( addAutomation(automation)==true )
        {
            return automation;
        }
        return null;
    }


    /**
     * Add the provided automation into the current application. Please note that
     * each automation in the project must have a unique name. The addition will
     * fail if an existing automation already uses the same name as the automation
     * you want to add.
     * @param automation
     * @return true if the automation is added successfully, false otherwise.
     */
    public static boolean addAutomation(Automation automation)
    {
        // check if an automation already exists with the same name
        String name = automation.getName();
        if( getAutomation(name) != null )
        {
            return false;
        }

        automations.add(automation);
        int index = automations.indexOf(automation);
        notifyAutomationAdded(automation, index);
        return true;
    }


    public static void removeAllGenerators(String classname)
    {
        int max = automations.size();
        for(int i=0; i<max; i++)
        {
            Automation auto = automations.get(i);
            auto.removeAllGenerators(classname);
        }
    }


    static void removeAutomation(Automation automation)
    {
        // the panel will be delete, so remove it from the listeners
        automation.removeAllListeners();
        // delete slave from list
        automations.remove(automation);
        notifyAutomationRemoved(automation);
    }




    //TODO: is this method really necessary???
    static long createID()
    {
        return idGenerator.createID();
    }


    //
    //
    // LISTENERS
    //
    //

    public static void removeAllListeners()
    {
        listeners.clear();
    }

    public static void addModbusPalListener(ModbusPalListener l)
    {
        if( listeners.contains(l)==false )
        {
            listeners.add(l);
        }
    }
    
    public static void removeModbusPalListener(ModbusPalListener l)
    {
        if( listeners.contains(l)==true )
        {
            listeners.remove(l);
        }
    }


    //
    //
    // EVENTS
    //
    //


    public static void tilt()
    {
        for(ModbusPalListener l:listeners)
        {
            l.tilt();
        }
    }

    private static void notifySlaveAdded(ModbusSlave slave)
    {
        for(ModbusPalListener l:listeners)
        {
            l.modbusSlaveAdded(slave);
        }
    }

    private static void notifySlaveRemoved(ModbusSlave slave)
    {
        for(ModbusPalListener l:listeners)
        {
            l.modbusSlaveRemoved(slave);
        }
    }

    private static void notifyAutomationAdded(Automation automation, int index)
    {
        for(ModbusPalListener l:listeners)
        {
            l.automationAdded(automation, index);
        }
    }

    private static void notifyAutomationRemoved(Automation automation)
    {
        for(ModbusPalListener l:listeners)
        {
            l.automationRemoved(automation);
        }
    }

    //
    //
    // SAVE PROJECT
    //
    //

    public static void saveProject(File target)
    throws FileNotFoundException, IOException
    {
        // create output stream
        FileOutputStream out = new FileOutputStream(target);
        saveProject(out, target);
    }

    private static void saveProject(OutputStream out, File projectFile)
    throws IOException
    {
        String openTag = "<modbuspal_project>\r\n";
        out.write( openTag.getBytes() );

        saveParameters(out);
        InstanciatorFactory.saveInstanciators(out, projectFile);
        saveAutomations(out);
        saveSlaves(out);

        String closeTag = "</modbuspal_project>\r\n";
        out.write( closeTag.getBytes() );
    }

    /**
     * Saves the project's parameters that do not fall into the "slave" or "automation"
     * categories, like id generator and link settings. The parameters are written
     * into the output stream as XML tags.
     * @param out
     * @throws IOException
     */
    private static void saveParameters(OutputStream out)
    throws IOException
    {
        // save id creator:
        idGenerator.save(out);

        // save link parameters
        ModbusPalGui.saveLinks(out);
    }





    private static void saveAutomations(OutputStream out)
    throws IOException
    {
        for(int i=0; i<automations.size(); i++)
        {
            Automation automation = automations.get(i);
            automation.save(out);
        }
    }

    private static void saveSlaves(OutputStream out)
    throws IOException
    {
        for( int i=0; i<256; i++ )
        {
            ModbusSlave slave = knownSlaves[i];
            if( slave != null )
            {
                slave.save(out,true);
            }
        }
    }



    //
    //
    // LOAD PROJECT
    //
    //


    public static void loadProject(File source)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(source);

        // normalize text representation
         doc.getDocumentElement().normalize();

         loadProject(doc, source);
    }


    private static void loadProject(Document doc, File projectFile)
    throws InstantiationException, IllegalAccessException
    {
        // get the root node
        String name = doc.getDocumentElement().getNodeName();
        System.out.println("load "+name);

        loadParameters(doc);
        InstanciatorFactory.loadInstanciators(doc, projectFile);
        loadAutomations(doc);
        loadSlaves(doc);
        loadBindings(doc);
    }


    /**
     * looks for all occurences of the "slave" openTag in the provided document
     * and create a modbus slave for each.
     * @param doc
     */
    private static void loadSlaves(Document doc)
    {
        NodeList slavesList = doc.getElementsByTagName("slave");
        for(int i=0; i<slavesList.getLength(); i++)
        {
            Node slaveNode = slavesList.item(i);
            NamedNodeMap attributes = slaveNode.getAttributes();
            ModbusSlave slave = new ModbusSlave( attributes );
            slave.load( slaveNode );
            addModbusSlave( slave );
        }
    }


    /**
     * this method  will parse the content of doc in order to find and parse
     * elements from the doc that are related to the static part of the Automation
     * class.
     * @param doc
     */
    private static void loadParameters(Document doc)
    {
        idGenerator.load(doc);
        ModbusPalGui.loadLinks(doc);
    }



    public static void loadAutomations(Document doc) throws InstantiationException, IllegalAccessException
    {
        NodeList automationsList = doc.getElementsByTagName("automation");
        for(int i=0; i<automationsList.getLength(); i++)
        {
            Node automationNode = automationsList.item(i);
            NamedNodeMap attributes = automationNode.getAttributes();

            // Get the name of the automation we want to load
            String name = attributes.getNamedItem("name").getNodeValue();

            // Check if an automation already exists with the same name:
            Automation automation = getAutomation( name );

            // If already exists:
            if( automation!=null )
            {
                // display a dialog and ask the user what to do:
                ErrorMessage dialog = new ErrorMessage(2);
                dialog.append("An automation with the same name already exists. Do you want to overwrite the existing automation or to keep it ?");
                dialog.setButton(0,"Overwrite");
                dialog.setButton(1,"Keep existing");
                dialog.setVisible(true);

                // if the user does not want to overwrite the existing
                // automation, skip and continue with the other automations
                if( dialog.getButton() != 0 )
                {
                    continue;
                }

                // otherwise, replace the content of the existing automation
                // with the new settings:
                automation.loadAttributes(attributes);

                // remove the existing generators before loading
                // the new ones:
                automation.removeAllGenerators();
            }

            // no automation with this name exists:
            else
            {
                automation = new Automation( attributes );
            }

            // finally, load the generators (whether a new automation is created
            // an existing automation is overwritten).
            automation.loadGenerators( automationNode.getChildNodes() );
            addAutomation(automation);
        }
    }



    /**
     * This method will examine the content of a "<binding>" tag in order to
     * parse the attributes it conains, and also the child tags that may exist.
     * @param node reference on the Node that represents a "<binding>" tag in the
     * project.
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    private static void loadBinding(Node node)
    throws InstantiationException, IllegalAccessException
    {
        // get the attributes of the "binding" openTag
        NamedNodeMap attributes = node.getAttributes();

        // extract the "automation" attribute
        Node automationNode = attributes.getNamedItem("automation");
        String automationName = automationNode.getNodeValue();

        // retrieve the reference to the automation with id "automationID"
        Automation automation = getAutomation(automationName);
        if( automation == null )
        {
            // TODO: display an error message,
            // the binding cannot be achieved if no automation!
            return;
        }

        // extract the "class" attribute
        Node classNode = attributes.getNamedItem("class");
        String className = classNode.getNodeValue();
        Class bindingClass = Binding.getClass(className);

        // extract the "order" attribute.
        Node orderNode = attributes.getNamedItem("order");
        String orderValue = orderNode.getNodeValue();
        int wordOrder = Integer.parseInt(orderValue);

        // retrieve the register that is the parent of this node
        Node parentRegister = XMLTools.findParent(node,"register");
        String parentAddress = XMLTools.getAttribute(XML_REGISTER_ADDRESS_ATTRIBUTE, parentRegister);
        int registerAddress = Integer.parseInt( parentAddress );

        // retrieve the slave that is the parent of this register
        Node parentSlave = XMLTools.findParent(parentRegister, "slave");
        String slaveAddress = XMLTools.getAttribute(XML_SLAVE_ID_ATTRIBUTE, parentSlave);
        int slaveId = Integer.parseInt(slaveAddress);

        // Instanciate the binding:
        Binding binding = (Binding)bindingClass.newInstance();
        binding.setup(automation, wordOrder);

        // bind the register and the automation
        ModbusSlave slave = knownSlaves[slaveId];
        slave.getHoldingRegisters().bind(registerAddress, binding);
    }





    // TODO: why is it public ???
    /**
     * This method scans the content of the document in order to find all
     * "<binding>" tags, and then call the loadBinding(Node) method for each
     * of them.
     * @param doc
     */
    public static void loadBindings(Document doc)
    {
        NodeList list = doc.getElementsByTagName("binding");
        for(int i=0; i<list.getLength(); i++ )
        {
            Node bindingNode = list.item(i);
            try
            {
                loadBinding(bindingNode);
            }

            catch (InstantiationException ex)
            {
                Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex)
            {
                Logger.getLogger(ModbusPalGui.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //
    //
    // GETTERS AND SETTERS
    //
    //


    public static void setLearnModeEnabled(boolean en)
    {
        learnModeEnabled = en;
    }

    public static byte getHoldingRegisters(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        assert( knownSlaves[slaveID] != null );
        assert( startingAddress >= 0 );
        assert( quantity >= 0 );
        return knownSlaves[slaveID].getHoldingRegisters(startingAddress, quantity, buffer, offset);
    }

    public static byte setHoldingRegisters(int slaveID, int startingAddress, int quantity, byte[] buffer, int offset)
    {
        assert( knownSlaves[slaveID] != null );
        assert( startingAddress >= 0 );
        assert( quantity >= 0 );
        return knownSlaves[slaveID].setHoldingRegisters(startingAddress, quantity, buffer, offset);
    }

    public static boolean holdingRegistersExist(int slaveID, int startingAddress, int quantity)
    {
        assert( knownSlaves[slaveID] != null );

        if( knownSlaves[slaveID].getHoldingRegisters().exist(startingAddress,quantity) == true )
        {
            return true;
        }
        else if( learnModeEnabled )
        {
            knownSlaves[slaveID].getHoldingRegisters().create(startingAddress,quantity);
            return true;
        }
        else
        {
            return false;
        }
    }


}
