/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import modbuspal.automation.NullAutomation;
import modbuspal.main.ModbusConst;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalXML;
import modbuspal.toolkit.InstanceCounter;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
public final class ModbusSlave
implements ModbusPalXML, ModbusConst
{
    private int slaveId;
    private boolean enabled;
    private ModbusRegisters holdingRegisters = new ModbusRegisters();
    private ModbusCoils coils = new ModbusCoils();
    private String customName;
    private ArrayList<ModbusSlaveListener> listeners = new ArrayList<ModbusSlaveListener>();
    private int modbusImplementation = IMPLEMENTATION_MODBUS;
    
    private ModbusPduProcessor pduProcessors[] = new ModbusPduProcessor[128];
    private InstanceCounter<ModbusPduProcessor> pduProcessorInstances = new InstanceCounter<ModbusPduProcessor>();

    private ModbusSlave()
    {
        pduProcessors[FC_READ_COILS] = coils;
        pduProcessors[FC_READ_HOLDING_REGISTERS] = holdingRegisters;
        pduProcessors[FC_WRITE_SINGLE_COIL] = coils;
        pduProcessors[FC_WRITE_SINGLE_REGISTER] = holdingRegisters;
        pduProcessors[FC_WRITE_MULTIPLE_COILS] = coils;
        pduProcessors[FC_WRITE_MULTIPLE_REGISTERS] = holdingRegisters;
        pduProcessors[FC_READ_WRITE_MULTIPLE_REGISTERS] = holdingRegisters;
    }

    public ModbusSlave(int id)
    {
        this();
        slaveId = id;
        customName = "Slave " + id;
        enabled = true;

    }


    public ModbusSlave(ModbusPalProject mpp, Node slaveNode)
    {
        this();
        load(slaveNode, false);
    }

    public void clear()
    {
        // remove listeners
        // listeners.clear();
        
        // propage clear
        holdingRegisters.clear();
        coils.clear();
    }


    public String getName()
    {
        return customName;
    }


    public ModbusPduProcessor getPduProcessor(byte functionCode)
    {
        if( functionCode>=0x80)
        {
            throw new ArrayIndexOutOfBoundsException(functionCode);
        }
        return pduProcessors[functionCode];
    }

    public boolean containsPduProcessorInstance(ModbusPduProcessor mpp)
    {
        return pduProcessorInstances.contains(mpp);
    }


    /**
     * return an array with all the instances of ModbusPduProcessor that are
     * use by this slave. Note that an instance of ModubsPduProcessor may be
     * used for more than one MODBUS function code, but this list returns no
     * duplicates.
     * @return
     */
    public ModbusPduProcessor[] getPduProcessorInstances()
    {
        Set<ModbusPduProcessor> instances = pduProcessorInstances.getInstanceSet();
        ModbusPduProcessor output[] = new ModbusPduProcessor[0];
        return instances.toArray(output);
    }

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


    @Deprecated
    public byte getHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return holdingRegisters.getValues(startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public byte setHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return holdingRegisters.setValues(startingAddress, quantity, buffer, offset);
    }


    public ModbusRegisters getHoldingRegisters()
    {
        return holdingRegisters;
    }

    @Deprecated
    public byte getCoils(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return coils.getValues(startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public byte setCoils(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        return coils.setValues(startingAddress, quantity, buffer, offset);
    }

    @Deprecated
    public byte setCoil(int address, int value)
    {
        byte rc = coils.setValue(address,value);
        return rc;
    }

    public ModbusCoils getCoils()
    {
        return coils;
    }

    public int getImplementation()
    {
        return modbusImplementation;
    }

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

        // remove name of Null automation:
        automationNames.remove(NullAutomation.NAME);

        String retval[] = new String[0];
        return automationNames.toArray(retval);
    }

    boolean hasBindings()
    {
        boolean retval = false;
        retval |= holdingRegisters.hasBindings();
        retval |= coils.hasBindings();
        return retval;
    }


    public void removeAllBindings(String classname)
    {
        holdingRegisters.removeAllBindings(classname);
        coils.removeAllBindings(classname);
    }

    private void loadHoldingRegisters(NodeList nodes)
    {
        Node node = XMLTools.getNode(nodes,"holding_registers");
        if( node == null )
        {
            return;
        }
        else
        {
            holdingRegisters.load(node);
        }
    }


    private void loadCoils(NodeList nodes)
    {
        Node node = XMLTools.getNode(nodes,XML_COILS_TAG);
        if( node == null )
        {
            return;
        }
        else
        {
            coils.load(node);
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
        String openTag = "<holding_registers>\r\n";
        out.write( openTag.getBytes() );

        holdingRegisters.save(out, withBindings);

        String closeTag = "</holding_registers>\r\n";
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


    //=========================================================================

    public boolean isEnabled()
    {
        return enabled;
    }

    private void loadAttributes(NamedNodeMap attributes, boolean importMode)
    {
        if( importMode!=true )
        {
            Node idNode = attributes.getNamedItem(XML_SLAVE_ID_ATTRIBUTE);
            String id = idNode.getNodeValue();
            slaveId = Integer.valueOf(id);

            Node enNode = attributes.getNamedItem("enabled");
            String en = enNode.getNodeValue();
            enabled = Boolean.parseBoolean(en);

            Node namNode = attributes.getNamedItem("name");
            String nam = namNode.getNodeValue();
            setName(nam);
        }
        
        Node imNode = attributes.getNamedItem("implementation");
        if( imNode != null )
        {
            String impl = imNode.getNodeValue();
            if( impl.compareTo("modbus")==0 )
            {
                setImplementation(IMPLEMENTATION_MODBUS);
            }
            else if( impl.compareTo("j-bus")==0 )
            {
                setImplementation(IMPLEMENTATION_JBUS);
            }
            notifyModbusImplChanged();
        }
    }


    public void load(Node node)
    {
        load(node,true);
    }

    public void load(Node node, boolean importMode)
    {
        clear();
        loadAttributes( node.getAttributes(), importMode );
        NodeList nodes = node.getChildNodes();
        loadHoldingRegisters(nodes);
        loadCoils(nodes);
        
    }


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


    public void save(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = xmlOpenTag();
        out.write( openTag.getBytes() );

        saveHoldingRegisters(out, withBindings);
        saveCoils(out,withBindings);

        String closeTag = "</slave>\r\n";
        out.write( closeTag.getBytes() );
    }

    public void setEnabled(boolean b)
    {
        enabled = b;
        notifyModbusSlaveEnabled(enabled);
    }

    public int getSlaveId()
    {
        return slaveId;
    }







    public void addModbusSlaveListener(ModbusSlaveListener l)
    {
        if(listeners.contains(l)==false)
        {
            listeners.add(l);
        }
    }

    public void removeModbusSlaveListener(ModbusSlaveListener l)
    {
        if(listeners.contains(l)==true )
        {
            listeners.remove(l);
        }
    }

    public void setImplementation(int impl)
    {
        modbusImplementation = impl;
        switch(impl)
        {
            default:
            case IMPLEMENTATION_MODBUS:
                holdingRegisters.setOffset(1);
                coils.setOffset(1);
                break;
            case IMPLEMENTATION_JBUS:
                holdingRegisters.setOffset(0);
                coils.setOffset(0);
                break;
        }
    }


}
