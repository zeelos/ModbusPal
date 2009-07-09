/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.Automation;
import modbuspal.main.ModbusConst;
import modbuspal.main.ModbusPal;
import modbuspal.main.ModbusPalXML;
import modbuspal.main.ModbusTools;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    private String customName;
    private ArrayList<ModbusSlaveListener> listeners = new ArrayList<ModbusSlaveListener>();
    private int modbusImplementation = IMPLEMENTATION_MODBUS;


    public ModbusSlave(int id)
    {
        slaveId = id;
        customName = "Slave " + id;
        enabled = true;
    }

    public ModbusSlave(NamedNodeMap attributes)
    {
        Node idNode = attributes.getNamedItem(XML_SLAVE_ID_ATTRIBUTE);
        String id = idNode.getNodeValue();
        slaveId = Integer.valueOf(id);

        Node enNode = attributes.getNamedItem("enabled");
        String en = enNode.getNodeValue();
        enabled = Boolean.parseBoolean(en);

        Node namNode = attributes.getNamedItem("name");
        String nam = namNode.getNodeValue();
        customName = nam;

        loadAttributes(attributes);
    }

    public void clear()
    {
        // remove listeners
        listeners.clear();
        
        // propage clear
        holdingRegisters.clear();
    }


    public String getName()
    {
        return customName;
    }

    //=========================================================================

    /*public boolean holdingRegistersExist(int startingAddress, int quantity)
    {
        assert( holdingRegisters != null );
        return holdingRegisters.exist(startingAddress, quantity);
    }*/


    /*public void createHoldingRegisters(int startingAddress, int quantity)
    {
        assert( holdingRegisters != null );
        holdingRegisters.create(startingAddress, quantity);
    }*/

    public byte getHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        for(int i=0; i<quantity; i++)
        {
            Integer reg = holdingRegisters.getRegister(startingAddress+i);
            ModbusTools.setUint16(buffer, offset + 2* i, reg);
        }
        return (byte)0x00;
    }

    public byte setHoldingRegisters(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        for(int i=0; i<quantity; i++)
        {
            Integer reg = ModbusTools.getUint16(buffer, offset + 2* i);
            byte rc = holdingRegisters.setRegisterSilent(startingAddress+i,reg);
            if( rc != (byte)0x00 )
            {
                return rc;
            }
        }
        return (byte)0x00;
    }


    public ModbusRegisters getHoldingRegisters()
    {
        return holdingRegisters;
    }

    public int getImplementation()
    {
        return modbusImplementation;
    }

    String[] getRequiredAutomations()
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

        String retval[] = new String[0];
        return automationNames.toArray(retval);
    }

    boolean hasBindings()
    {
        boolean retval = false;
        retval |= holdingRegisters.hasBindings();
        return retval;
    }

/*    public void bindHoldingRegister(int row, Binding binding)
    {
        int address = holdingRegisters.getAddressAt(row);
        holdingRegisters.bind(address,binding);
    } */

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

    //=========================================================================

    public boolean isEnabled()
    {
        return enabled;
    }

    private void loadAttributes(NamedNodeMap attributes)
    {
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
        }
    }


    public void load(Node node)
    {
        loadAttributes( node.getAttributes() );
        NodeList nodes = node.getChildNodes();
        loadHoldingRegisters(nodes);
        notifyModbusImplChanged();
    }


    void changeName(String name)
    {
        customName = name;
    }

    public void setName(String name)
    {
        changeName(name);
        notifyNameChanged();
    }





    private String xmlOpenTag()
    {
        StringBuffer openTag = new StringBuffer();
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


    public void exportSlave(File exportFile, boolean withBindings, boolean withAutomations)
    throws FileNotFoundException, IOException
    {
        OutputStream out = new FileOutputStream(exportFile);

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


    public void importSlave(File importFile, int index, boolean withBindings, boolean withAutomations)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(importFile);

        // normalize text representation
        doc.getDocumentElement().normalize();

        // how many slaves in the file?
        NodeList slaves = doc.getElementsByTagName("slave");

        Node slaveNode = slaves.item(index);

        if( withAutomations==true )
        {
            ModbusPal.loadAutomations(doc);
        }

        load(slaveNode);

        if( withBindings==true )
        {
            ModbusPal.loadBindings(doc);
        }
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
                break;
            case IMPLEMENTATION_JBUS:
                holdingRegisters.setOffset(0);
                break;
        }
    }

}
