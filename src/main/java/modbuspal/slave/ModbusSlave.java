/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.automation.NullAutomation;
import modbuspal.instanciator.InstantiableManager;
import modbuspal.main.ModbusConst;
import static modbuspal.main.ModbusConst.FC_READ_COILS;
import static modbuspal.main.ModbusConst.FC_READ_DISCRETE_INPUTS;
import static modbuspal.main.ModbusConst.FC_READ_HOLDING_REGISTERS;
import static modbuspal.main.ModbusConst.FC_READ_WRITE_MULTIPLE_REGISTERS;
import static modbuspal.main.ModbusConst.FC_WRITE_MULTIPLE_COILS;
import static modbuspal.main.ModbusConst.FC_WRITE_MULTIPLE_REGISTERS;
import static modbuspal.main.ModbusConst.FC_WRITE_SINGLE_COIL;
import static modbuspal.main.ModbusConst.FC_READ_FILE_RECORD;
import static modbuspal.main.ModbusConst.FC_WRITE_FILE_RECORD;
import static modbuspal.main.ModbusConst.FC_WRITE_SINGLE_REGISTER;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalXML;
import static modbuspal.main.ModbusPalXML.XML_SLAVE_ID_ATTRIBUTE;
import modbuspal.toolkit.InstanceCounter;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Modelization of a modbus slave
 * @author nnovic
 */
public final class ModbusSlave
implements ModbusPalXML, ModbusConst
{
    private ModbusSlaveAddress slaveId;
    private boolean enabled;
    private ModbusRegisters holdingRegisters = new ModbusRegisters();
    private ModbusExtendedRegisters extendedRegisters = new ModbusExtendedRegisters();
    private ModbusCoils coils = new ModbusCoils();
    private String customName;
    private ArrayList<ModbusSlaveListener> listeners = new ArrayList<ModbusSlaveListener>();
    private int modbusImplementation = IMPLEMENTATION_MODBUS;
    private long minReplyDelay = 0L;
    private long maxReplyDelay = 0L;
    private float noReplyRate = 0f;
    private ModbusPduProcessor pduProcessors[] = new ModbusPduProcessor[128];
    private InstanceCounter<ModbusPduProcessor> pduProcessorInstances = new InstanceCounter<ModbusPduProcessor>();

    private ModbusSlave()
    {
        clearFunctions();
    }

    /**
     * Creates a new ModbusSlave, with the specified slave address.
     * @param id slave address assigned to this ModbusSlave.
     */
    public ModbusSlave(ModbusSlaveAddress a)
    {
        this();
        slaveId = a;
        customName = "Slave " + a.toString();
        enabled = true;

    }


    /**
     * Creates a new instance of ModbusSlave, by loading the settings
     * in the provided DOM node.
     * @param mpp the modbuspal project containing the current project data
     * @param slaveNode the DOM node where the settings to load are stored.
     */
    public ModbusSlave(ModbusPalProject mpp, Node slaveNode)
    {
        this();
        load(mpp, slaveNode, false);
    }

    /**
     * Clears all the content of this slave.
     */
    public void clear()
    {
        // remove listeners
        // listeners.clear();

        // propage clear
        holdingRegisters.clear();
        extendedRegisters.clear();
        coils.clear();
        clearFunctions();
        clearTuning();
    }


    /**
     * Gets the name defined for this slave
     * @return the name of this slave.
     */
    public String getName()
    {
        return customName;
    }


    /**
     * Gets the ModbusPduProcessor associated with the specified function code.
     * @param functionCode the function code
     * @return the ModbusPduProcessor associated with the specified funcion code.
     */
    public ModbusPduProcessor getPduProcessor(byte functionCode)
    {
        if( functionCode>=0x80)
        {
            throw new ArrayIndexOutOfBoundsException(functionCode);
        }
        return pduProcessors[functionCode];
    }

    /**
     * Checks if a ModbusPduProcessor is used by this modbus slave.
     * @param mpp the ModbusPduProcessor to check
     * @return true if the ModbusPduProcessor is used by thsi modbus slave.
     */
    public boolean containsPduProcessorInstance(ModbusPduProcessor mpp)
    {
        return pduProcessorInstances.contains(mpp);
    }


    /**
     * return an array with all the instances of ModbusPduProcessor that are
     * use by this slave. Note that an instance of ModubsPduProcessor may be
     * used for more than one MODBUS function code, but this list returns no
     * duplicates. Also, note that the instances returned are only those modified
     * by the user, by means of scripts. It won't return the predefined standard
     * functions, such holding registers and coils.
     * @return an array containing the ModbusPduProcessor defined in this slave
     */
    public ModbusPduProcessor[] getPduProcessorInstances()
    {
        Set<ModbusPduProcessor> instances = pduProcessorInstances.getInstanceSet();
        ModbusPduProcessor output[] = new ModbusPduProcessor[0];
        return instances.toArray(output);
    }

    void clearFunctions()
    {
        for(int i=0; i<pduProcessors.length; i++)
        {
            setPduProcessor( (byte)i,null);
        }
        /*for(byte i : USER_DEFINED_FUNCTION_CODES)
        {
            setPduProcessor(i,null);
        }*/
        pduProcessors[FC_READ_COILS] = coils;
        pduProcessors[FC_READ_DISCRETE_INPUTS] = coils;
        pduProcessors[FC_READ_HOLDING_REGISTERS] = holdingRegisters;
        pduProcessors[FC_WRITE_SINGLE_COIL] = coils;
        pduProcessors[FC_WRITE_SINGLE_REGISTER] = holdingRegisters;
        pduProcessors[FC_WRITE_MULTIPLE_COILS] = coils;
        pduProcessors[FC_WRITE_MULTIPLE_REGISTERS] = holdingRegisters;
        pduProcessors[FC_READ_WRITE_MULTIPLE_REGISTERS] = holdingRegisters;
        pduProcessors[FC_READ_FILE_RECORD] = extendedRegisters;
        pduProcessors[FC_WRITE_FILE_RECORD] = extendedRegisters;
    }


    /**
     * Defines a new ModbusPduProcessor for the specified function code.
     * @param functionCode the function code
     * @param mspp the new ModbusPduProcessor for the specified function code.
     * @return the old ModbusPduProcessor that was defined for the specified function code.
     */
    public ModbusPduProcessor setPduProcessor(byte functionCode, ModbusPduProcessor mspp)
    {
        // reject if function code is is the range reserverd for exception codes
        if( functionCode>=0x80)
        {
            throw new ArrayIndexOutOfBoundsException(functionCode);
        }

        //
        // clear the old reference for the specified functionCode:
        //

        ModbusPduProcessor old = pduProcessors[functionCode];
        if( old!=null )
        {
            // remove reference for the given function code
            pduProcessors[functionCode]=null;
            pduProcessorInstances.removeInstance(old);

            // if there is no more instances of the old processor
            // being used, its "reset" method must be called:
            if( pduProcessorInstances.getInstanceCount(old)==0 )
            {
                old.reset();
            }
        }

        // if the new processor is not null, and there is no known
        // instance of this processor yet, then it is important to
        // initialize the instance now.
        // the main reason for that is when notifyPduProcessorChanged() is
        // triggered, the ModbusSlaveDialog will call the getPduPane() function,
        // but the panel is most likely to be created in the init() function.
        if( mspp!=null )
        {
            if( pduProcessorInstances.getInstanceCount(mspp)==0 )
            {
                mspp.init();
            }
        }

        // notify the listeners of the modification. It is important
        // to do it now, when the old processor has been removed and
        // the new one has not been set yet, so thath listeners that
        // rely on counting the references are not misguided.
        notifyPduProcessorChanged(functionCode, old, mspp);

        //
        // set the new processor for the specified function code
        //

        pduProcessors[functionCode]=mspp;

        // if the new processor is not null, and if the new processor
        // is a new instance, then its 'init' method must be called.
        if( mspp!=null )
        {
            pduProcessorInstances.addInstance(mspp);
        }

        return old;
    }

    /**
     * Remove all pdu processors with the specified class name from this
     * slave.
     * @param classname
     */
    public void removeAllFunctions(String classname) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * @param startingAddress
     * @param quantity
     * @param buffer
     * @param offset
     * @return XC_SUCCESSFUL if successful
     * @deprecated use getHoldingRegisters().getValues() instead
     */
    @Deprecated
    public byte getHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return holdingRegisters.getValues(startingAddress, quantity, buffer, offset);
    }

    /**
     * @param startingAddress
     * @param quantity
     * @param buffer
     * @param offset
     * @return XC_SUCCESSFUL if successful
     * @deprecated use getHoldingRegisters().setValues() instead
     */
    @Deprecated
    public byte setHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return holdingRegisters.setValues(startingAddress, quantity, buffer, offset);
    }

    /**
     * Returns the object that stores the holding registers for this modbus slave.
     * @return the object that stores the holding registers for this modbus slave.
     */
    public ModbusRegisters getHoldingRegisters()
    {
        return holdingRegisters;
    }

    /**
     * Returns the object that stores the extended registers for this modbus slave.
     * @return the object that stores the extended registers for this modbus slave.
     */
    public ModbusExtendedRegisters getExtendedRegisters()
    {
        return extendedRegisters;
    }

    /**
     * @param startingAddress
     * @param quantity
     * @param buffer
     * @param offset
     * @return XC_SUCCESSFUL if successful
     * @deprecated use getCoils().getValues() instead
     */
    @Deprecated
    public byte getCoils(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return coils.getValues(startingAddress, quantity, buffer, offset);
    }

    /**
     * @param startingAddress
     * @param quantity
     * @param buffer
     * @param offset
     * @return XC_SUCCESSFUL if successful
     * @deprecated use getCoils().setValues() instead
     */
    @Deprecated
    public byte setCoils(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return coils.setValues(startingAddress, quantity, buffer, offset);
    }

    /**
     * @param address
     * @param value
     * @return XC_SUCCESSFUL if successful
     * @deprecated use getCoils().setValue() instead
     */
    @Deprecated
    public byte setCoil(int address, int value)
    {
        byte rc = coils.setValue(address,value);
        return rc;
    }

    /**
     * Returns the object that stores the coils for this modbus slave.
     * @return the object that stores the coils for this modbus slave.
     */
    public ModbusCoils getCoils()
    {
        return coils;
    }

    /**
     * Gets the implementation defined for this slave.
     * @return one of IMPLEMENTATION_MODBUS or IMPLEMENTATION_JBUS
     */
    public int getImplementation()
    {
        return modbusImplementation;
    }

    /**
     * Returns the list of automations that are used by this modbus slave.
     * @return array of strings containing the classnames of the automations
     * used by this slave.
     */
    public String[] getRequiredAutomations()
    {
        ArrayList<String> automationNames = new ArrayList<String>();
        Collection<String> tmpNames;

        // get the names of the automations that are required for
        // the binding in holding registers:
        tmpNames = holdingRegisters.getRequiredAutomations();
        // remove the names of the bindings that are already in the final list:
        tmpNames.removeAll(automationNames);
        // add the rest to the final list:
        automationNames.addAll(tmpNames);

        // get the names of the automations that are required for
        // the binding in coils:
        tmpNames = coils.getRequiredAutomations();
        // remove the names of the bindings that are already in the final list:
        tmpNames.removeAll(automationNames);
        // add the rest to the final list:
        automationNames.addAll(tmpNames);

        // get the names of the automations that are required for
        // the binding in holding registers:
        tmpNames = extendedRegisters.getRequiredAutomations();
        // remove the names of the bindings that are already in the final list:
        tmpNames.removeAll(automationNames);
        // add the rest to the final list:
        automationNames.addAll(tmpNames);

        // remove name of Null automation:
        automationNames.remove(NullAutomation.NAME);

        String retval[] = new String[0];
        return automationNames.toArray(retval);
    }

    boolean hasBindings()
    {
        boolean retval = false;
        retval |= holdingRegisters.hasBindings();
        retval |= extendedRegisters.hasBindings();
        retval |= coils.hasBindings();
        return retval;
    }


    /**
     * Clears all the bindings defined in this slave and matching the specified
     * class name
     * @param classname the class name of the bindings to remove
     */
    public void removeAllBindings(String classname)
    {
        holdingRegisters.removeAllBindings(classname);
        extendedRegisters.removeAllBindings(classname);
        coils.removeAllBindings(classname);
    }

    private void loadHoldingRegisters(Node node)
    {
        if( node == null )
        {
            return;
        }
        holdingRegisters.load(node);
    }

    private void loadExtendedRegisters(Node node)
    {
        if( node == null )
        {
            return;
        }
        extendedRegisters.load(node);
    }


    private void loadCoils(Node node)
    {
        if( node == null )
        {
            return;
        }
        coils.load(node);
    }

    private void loadFunctions(
            InstantiableManager<ModbusPduProcessor> ffactory,
            Node node)
    {
        if(node==null)
        {
            return;
        }

        NodeList list = node.getChildNodes();
        for(int i=0; i<list.getLength(); i++)
        {
            Node fNode = list.item(i);
            if( fNode.getNodeName().compareToIgnoreCase(XML_FUNCTION_INSTANCE_TAG)==0 )
            {
                try {
                    loadFunctionInstance(ffactory, fNode);
                } catch (InstantiationException ex) {
                    Logger.getLogger(ModbusSlave.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ModbusSlave.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


    private void notifyModbusSlaveEnabled(boolean enabled)
    {
       for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlaveEnabled(this,enabled);
       }
    }

    private void notifyModbusImplChanged()
    {
       for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlaveImplChanged(this,modbusImplementation);
       }
    }

    private void notifyNameChanged()
    {
       for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlaveNameChanged(this,customName);
       }
    }

    private void notifyPduProcessorChanged(byte functionCode, ModbusPduProcessor old, ModbusPduProcessor mspp)
    {
         for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlavePduProcessorChanged(this,functionCode,old,mspp);
       }
    }

    private void saveHoldingRegisters(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = "<"+XML_HOLDING_REGISTERS_TAG+">\r\n";
        out.write( openTag.getBytes() );

        holdingRegisters.save(out, withBindings);

        String closeTag = "</"+XML_HOLDING_REGISTERS_TAG+">\r\n";
        out.write( closeTag.getBytes() );
    }

    private void saveExtendedRegisters(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = "<"+XML_EXTENDED_REGISTERS_TAG+">\r\n";
        out.write( openTag.getBytes() );

        extendedRegisters.save(out, withBindings);

        String closeTag = "</"+XML_EXTENDED_REGISTERS_TAG+">\r\n";
        out.write( closeTag.getBytes() );
    }


    private void saveCoils(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = "<"+XML_COILS_TAG+">\r\n";
        out.write( openTag.getBytes() );

        coils.save(out, withBindings);

        String closeTag = "</"+XML_COILS_TAG+">\r\n";
        out.write( closeTag.getBytes() );
    }

    private void saveFunctions(OutputStream out)
    throws IOException
    {
        ModbusPduProcessor instances[] = getPduProcessorInstances();

        if( instances!=null )
        {
            if( instances.length>0)
            {
                String openTag = "<"+XML_FUNCTIONS_TAG+">\r\n";
                out.write( openTag.getBytes() );

                for(int i=0; i<instances.length; i++)
                {
                    StringBuilder tag = new StringBuilder("<instance");
                    tag.append(" class=\"").append(instances[i].getClassName()).append("\"");
                    tag.append(">\r\n");
                    out.write( tag.toString().getBytes() );

                    for(int j=0; j<pduProcessors.length;j++)
                    {
                        if( pduProcessors[j]==instances[i])
                        {
                            tag = new StringBuilder("<function ");
                            tag.append("code=\"").append( String.valueOf(j) ).append("\" />\r\n");
                            out.write( tag.toString().getBytes() );
                        }
                    }

                    tag = new StringBuilder("<settings>\r\n");
                    out.write( tag.toString().getBytes() );
                    instances[i].savePduProcessorSettings(out);
                    tag = new StringBuilder("</settings>\r\n");
                    out.write( tag.toString().getBytes() );

                    tag = new StringBuilder("</instance>\r\n");
                    out.write( tag.toString().getBytes() );
                }

                String closeTag = "</"+XML_FUNCTIONS_TAG+">\r\n";
                out.write( closeTag.getBytes() );
            }
        }
    }


    //=========================================================================

    /**
     * Checks if this slave is enabled or disabled
     * @return true if enabled, false if disabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Overwrites the settings and values of this slave with those defined in
     * the specified DOM node. The existing settings for: slave id, enabled status,
     * name, or not modified, though. Functions and tuning settings won't be loaded.
     * @param node the DOM node where the settings to load are stored
     * @see #load(modbuspal.main.ModbusPalProject, org.w3c.dom.Node)
     * @deprecated some settings can't be loaded by this method
     */
    @Deprecated
    public void load(Node node)
    {
        load(null,node);
    }

    /**
     * Overwrites the settings and values of this slave with those defined in
     * the specified DOM node. Functions and tuning settings won't be loaded.
     * @param node the DOM node where the settings to load are stored
     * @param importMode if true, the loading from the DOM will take care not to
     * overwrite the existing values of: slave id, enabled status, name.
     * @see #load(modbuspal.main.ModbusPalProject, org.w3c.dom.Node, boolean)
     * @deprecated some settings can't be loaded by this method
     */
    @Deprecated
    public void load(Node node, boolean importMode)
    {
        load(null,node,importMode);
    }

    /**
     * Overwrites the settings and values of this slave with those defined in
     * the specified DOM node. The existing settings for: slave id, enabled status,
     * name, or not modified, though.
     * @param mpp the modbuspal project from which additional information can be retrieved.
     * if null, functions and tuning settings won't be loaded.
     * @param node the DOM node where the settings to load are stored
     */
    public void load(ModbusPalProject mpp, Node node)
    {
        load(mpp, node,true);
    }

    /**
     * Overwrites the settings and values of this slave with those defined in
     * the specified DOM node.
     * @param mpp the modbuspal project from which additional information can be retrieved.
     * if null, functions and tuning settings won't be loaded.
     * @param node the DOM node where the settings to load are stored
     * @param importMode if true, the loading from the DOM will take care not to
     * overwrite the existing values of: slave id, enabled status, name.
     */
    public void load(ModbusPalProject mpp, Node node, boolean importMode)
    {
        // in case the slave contains old data, clear it:
        clear();

        //
        // load the attributes of the slave
        //

        if( importMode==false )
        {
            String id2 = XMLTools.getAttribute(XML_SLAVE_ID2_ATTRIBUTE, node);
            if( id2 != null )
            {
                //slaveId = Integer.valueOf(id2);
                throw new UnsupportedOperationException("not yet implemented");
            }
            else
            {
                String id = XMLTools.getAttribute(XML_SLAVE_ID_ATTRIBUTE, node);
                slaveId = new ModbusSlaveAddress(Integer.valueOf(id));
            }

            String en = XMLTools.getAttribute(XML_SLAVE_ENABLED_ATTRIBUTE, node);
            enabled = Boolean.parseBoolean(en);

            String nam = XMLTools.getAttribute(XML_SLAVE_NAME_ATTRIBUTE, node);
            setName(nam);
        }

        String impl = XMLTools.getAttribute(XML_SLAVE_IMPLEMENTATION_ATTRIBUTE, node);
        if( impl != null )
        {

            if( impl.compareToIgnoreCase(XML_SLAVE_IMPLEMENTATION_MODBUS_VALUE)==0 )
            {
                setImplementation(IMPLEMENTATION_MODBUS);
            }
            else if( impl.compareToIgnoreCase(XML_SLAVE_IMPLEMENTATION_JBUS_VALUE)==0 )
            {
                setImplementation(IMPLEMENTATION_JBUS);
            }
        }

        NodeList nodes = node.getChildNodes();

        //
        // load holding registers
        //

        Node holdingRegistersNode = XMLTools.getNode(nodes, XML_HOLDING_REGISTERS_TAG);
        loadHoldingRegisters(holdingRegistersNode);

        //
        // load extended registers
        //

        Node extendedRegistersNode = XMLTools.getNode(nodes, XML_EXTENDED_REGISTERS_TAG);
        loadExtendedRegisters(extendedRegistersNode);

        //
        // load coils
        //

        Node coilsNode = XMLTools.getNode(nodes, XML_COILS_TAG);
        loadCoils(coilsNode);

        //
        // ensure compatibility with old format:
        //

        if(mpp==null) {
            return;
        }

        //
        // load functions
        //

        Node functionsNode = XMLTools.getNode(nodes, XML_FUNCTIONS_TAG);
        loadFunctions(mpp.getFunctionFactory(), functionsNode);

        //
        // load tuning
        //

        Node tuningNode = XMLTools.getNode(nodes, XML_TUNING_TAG);
        loadTuning(tuningNode);
    }

    /**
     * Changes the name of the modbus slave.
     * @param name new name of the modbus slave
     */
    public void setName(String name)
    {
        customName = name;
        notifyNameChanged();
    }





    private String xmlOpenTag()
    {
        StringBuilder openTag = new StringBuilder();
        openTag.append( "<slave "+ XML_SLAVE_ID_ATTRIBUTE +"=\"" );
        openTag.append( String.valueOf(slaveId) );

        openTag.append("\" enabled=\"");
        if( enabled )
        {
            openTag.append("true");
        }
        else
        {
            openTag.append("false");
        }

        openTag.append("\" name=\"");
        openTag.append(customName);

        openTag.append("\" implementation=\"");
        switch(modbusImplementation)
        {
            default:
            case IMPLEMENTATION_MODBUS:
                openTag.append("modbus");
                break;
            case IMPLEMENTATION_JBUS:
                openTag.append("j-bus");
                break;
        }

        openTag.append("\">\r\n");
        return openTag.toString();
    }


    /**
     * Saves the values and settings of this modbus slave into the provided
     * output stream, using XML format.
     * @param out the output stream
     * @param withBindings if true, the description of the bindings will be written, too
     * @throws IOException
     */
    public final void save(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = xmlOpenTag();
        out.write( openTag.getBytes() );

        saveHoldingRegisters(out, withBindings);
        saveExtendedRegisters(out, withBindings);
        saveCoils(out,withBindings);
        saveFunctions(out);
        saveTuning(out);

        String closeTag = "</slave>\r\n";
        out.write( closeTag.getBytes() );
    }


    /**
     * Enables or disables this modbus slave.
     * @param b true to enable, false to disable
     */
    public void setEnabled(boolean b)
    {
        enabled = b;
        notifyModbusSlaveEnabled(enabled);
    }

    /**
     * returns the MODBUS id associated with this slave
     * @return id of this slave.
     */
    public ModbusSlaveAddress getSlaveId()
    {
        return slaveId;
    }



    /**
     * Adds a ModbusSlaveListener to the list of listeners
     * @param l the listener to add
     */
    public void addModbusSlaveListener(ModbusSlaveListener l)
    {
        if(listeners.contains(l)==false)
        {
            listeners.add(l);
        }
    }

    /**
     * Removes a Modbus
     * @param l
     */
    public void removeModbusSlaveListener(ModbusSlaveListener l)
    {
        if(listeners.contains(l)==true )
        {
            listeners.remove(l);
        }
    }

    /**
     * Changes the implementation for this slave.
     * @param impl one of IMPLEMENTATION_MODBUS or IMPLEMENTATION_JBUS
     */
    public void setImplementation(int impl)
    {
        modbusImplementation = impl;
        switch(impl)
        {
            default:
            case IMPLEMENTATION_MODBUS:
                holdingRegisters.setOffset(1);
                extendedRegisters.setOffset(1);
                coils.setOffset(1);
                break;
            case IMPLEMENTATION_JBUS:
                holdingRegisters.setOffset(0);
                extendedRegisters.setOffset(0);
                coils.setOffset(0);
                break;
        }
        notifyModbusImplChanged();
    }

    private void loadFunctionInstance(
            InstantiableManager<ModbusPduProcessor> ffactory,
            Node node)
    throws InstantiationException, IllegalAccessException
    {
        //
        // load attributes
        //

        String clazz = XMLTools.getAttribute("class", node);

        //
        // instanciate
        //

        ModbusPduProcessor mpp = ffactory.newInstance(clazz);

        //
        // look for all associations
        //

        List<Node> list = XMLTools.getNodes(node.getChildNodes(), XML_FUNCTION_TAG);
        for(Node iNode:list)
        {
            String code = XMLTools.getAttribute(XML_FUNCTION_CODE_ATTRIBUTE, iNode);
            setPduProcessor(Byte.valueOf(code), mpp);
        }

        // load settings
        Node settings = XMLTools.getNode(node.getChildNodes(), XML_FUNCTION_SETTINGS_TAG);
        mpp.loadPduProcessorSettings(settings.getChildNodes());
    }


    //==========================================================================
    //
    // TUNING
    //
    //==========================================================================

    /**
     * Gets a random duration (in milliseconds) within the boundaries
     * defined by the "min reply delay" and "max reply delay" parameters.
     * The duration will be applied between the moment ModbusPal receives
     * a request and the moment the reply (if any) is sent
     * @return a duration in milliseconds, delay between a modbus request
     * and the reply.
     */
    public long getReplyDelay()
    {
        double range = maxReplyDelay-minReplyDelay;
        if(range<0)
        {
            return minReplyDelay;
        }
        return minReplyDelay + (long)(Math.random()*range);
    }

    private void saveTuning(OutputStream out)
    throws IOException
    {
        StringBuilder tag = new StringBuilder();
        tag.append("<").append(XML_TUNING_TAG).append(">\r\n");
        out.write( tag.toString().getBytes() );

        tag = new StringBuilder();
        tag.append("<").append(XML_REPLYDELAY_TAG);
        tag.append(" min=\"").append(String.valueOf(minReplyDelay));
        tag.append("\" max=\"").append( String.valueOf(maxReplyDelay) );
        tag.append("\" />\r\n");
        out.write( tag.toString().getBytes() );

        tag = new StringBuilder();
        tag.append("<").append(XML_ERRORRATES_TAG);
        tag.append(" "+XML_ERRORRATES_NOREPLY_ATTRIBUTE+"=\"").append(String.valueOf(noReplyRate)).append("\"");
        tag.append(" />\r\n");
        out.write( tag.toString().getBytes() );

        tag = new StringBuilder();
        tag.append("</").append(XML_TUNING_TAG).append(">\r\n");
        out.write( tag.toString().getBytes() );
    }

    private void loadTuning(Node node)
    {
        NodeList list = node.getChildNodes();

        // look for "reply delay"
        Node rdNode = XMLTools.getNode(list, XML_REPLYDELAY_TAG);
        if(rdNode!=null)
        {
            // look for min
            long min = 0;
            String minValue = XMLTools.getAttribute(XML_REPLYDELAY_MIN_ATTRIBUTE, rdNode);
            if(minValue!=null)
            {
                min = Long.parseLong(minValue);
            }

            // look for max
            long max = 0;
            String maxValue = XMLTools.getAttribute(XML_REPLYDELAY_MAX_ATTRIBUTE, rdNode);
            if(maxValue!=null)
            {
                max = Long.parseLong(maxValue);
            }
            setReplyDelay(min, max);
        }

        // look for "error rates"
        Node erNode = XMLTools.getNode(list, XML_ERRORRATES_TAG);
        if(erNode!=null)
        {
            // look for no_reply
            float no_reply = 0f;
            String noReplyValue = XMLTools.getAttribute(XML_ERRORRATES_NOREPLY_ATTRIBUTE, erNode);
            if(noReplyValue!=null)
            {
                no_reply = Float.parseFloat(noReplyValue);
            }
            setErrorRates(no_reply);
        }    }

    private void clearTuning()
    {
        setReplyDelay(0,0);
        setErrorRates(0f);
    }

    /**
     * Sets up the min and max values for the "reply delay".
     * @param min minimum delay in milliseconds
     * @param max maximum delay in milliseconds
     * @throws IllegalArgumentException
     */
    public void setReplyDelay(long min, long max)
    throws IllegalArgumentException
    {
        if( (min<0) || (max<min) )
        {
            throw new IllegalArgumentException();
        }
        minReplyDelay = min;
        maxReplyDelay = max;
        notifyReplyDelayChanged();
    }

    /**
     * Sets up the rates of the errors that will be simulated for this slave.
     * For now, only the "no reply" error is defined. Each rate is a float
     * value between 0f and 1f. 0f means "no error", or 0% of error. 1f means
     * "always in error", or 100% of error. 0.5f will generate errors randomly,
     * grossly 50% of the time.
     * @param noReply the rate at which this slave will fail to reply to a modbus request
     * @throws IllegalArgumentException
     */
    public void setErrorRates(float noReply)
    throws IllegalArgumentException
    {
        if( (noReply<0) || (noReply>1) )
        {
            throw new IllegalArgumentException("no reply rate must be a value between 0 and 1");
        }
        noReplyRate = noReply;
        notifyErrorRatesChanged();
    }

    private void notifyReplyDelayChanged()
    {
         for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlaveReplyDelayChanged(this,minReplyDelay,maxReplyDelay);
       }
    }

    private void notifyErrorRatesChanged()
    {
         for(ModbusSlaveListener l:listeners )
       {
           l.modbusSlaveErrorRatesChanged(this,noReplyRate);
       }
    }

    /**
     * Gets the maximum reply delay for this slave
     * @return maximmum reply delay, in milliseconds
     */
    public long getMaxReplyDelay()
    {
        return maxReplyDelay;
    }

    /**
     * Gets the minimum reply delay for this slave
     * @return minimum reply delay, in milliseconds
     */
    public long getMinReplyDelay()
    {
        return minReplyDelay;
    }

    /**
     * Gets the error rate for the "no reply" error.
     * @return "no reply" error rate, float value between 0f and 1f.
     */
    public float getNoReplyErrorRate()
    {
        return noReplyRate;
    }
}
