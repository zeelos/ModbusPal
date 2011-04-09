/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import modbuspal.automation.NullAutomation;
import modbuspal.main.ModbusConst;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalXML;
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
    private ModbusSlavePduProcessor pduProcessors[] = new ModbusSlavePduProcessor[128];

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


    public ModbusSlavePduProcessor getPduProcessor(byte functionCode)
    {
        if( functionCode>=0x80)
        {
            throw new ArrayIndexOutOfBoundsException(functionCode);
        }
        return pduProcessors[functionCode];
    }

    public ModbusSlavePduProcessor[] getPduProcessorInstances()
    {
        ArrayList<ModbusSlavePduProcessor> instances = new ArrayList<ModbusSlavePduProcessor>();
        for(int i=0; i<pduProcessors.length;i++)
        {
            if(pduProcessors[i]!=null)
            {
                if( instances.contains(pduProcessors[i])==false )
                {
                    instances.add(pduProcessors[i]);
                }
            }
        }
        
        ModbusSlavePduProcessor output[] = new ModbusSlavePduProcessor[0];
        return instances.toArray(output);
    }

    public ModbusSlavePduProcessor setPduProcessor(byte functionCode, ModbusSlavePduProcessor mspp)
    {
        if( functionCode>=0x80)
        {
            throw new ArrayIndexOutOfBoundsException(functionCode);
        }
        ModbusSlavePduProcessor old = pduProcessors[functionCode];
        pduProcessors[functionCode]=mspp;
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


/*    public void exportSlave(File exportFile, boolean withBindings, boolean withAutomations)
    throws FileNotFoundException, IOException
    {
        OutputStream out = new FileOutputStream(exportFile);

        String xmlTag = "<?xml version=\"1.0\"?>\r\n";
        out.write( xmlTag.getBytes() );

        String docTag = "<!DOCTYPE modbuspal_slave SYSTEM \"modbuspal.dtd\">\r\n";
        out.write( docTag.getBytes() );

        String openTag = "<modbuspal_slave>\r\n";
        out.write( openTag.getBytes() );

        // if needed, first exportSlave automations (they need to be imported first!)
        if( withAutomations == true )
        {
            String names[] = getRequiredAutomations();
            for(int i=0; i<names.length; i++)
            {
                Automation automation = ModbusPal.getAutomation( names[i] );
                automation.save(out);
            }
        }
        save(out,withBindings);

        String closeTag = "</modbuspal_slave>\r\n";
        out.write( closeTag.getBytes() );
        out.close();
    }


    public void importSlave(File importFile, int index, ModbusPalProject modbusPalProject, boolean withBindings, boolean withAutomations)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        Document doc = XMLTools.ParseXML(importFile);

        // normalize text representation
        doc.getDocumentElement().normalize();

        // how many slaves in the file?
        NodeList slaves = doc.getElementsByTagName("slave");

        Node slaveNode = slaves.item(index);

        if( withAutomations==true )
        {
            modbusPalProject.loadAutomations(doc);
        }

        load(slaveNode,true);

        if( withBindings==true )
        {
            modbusPalProject.loadBindings(doc, this);
        }
    }
*/





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
