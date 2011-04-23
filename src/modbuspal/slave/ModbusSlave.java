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
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalXML;
import modbuspal.toolkit.InstanceCounter;
import modbuspal.toolkit.XMLTools;
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
    private long minReplyDelay = 0L;
    private long maxReplyDelay = 0L;
    private float noReplyRate = 0f;
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
        load(mpp, slaveNode, false);
    }

    public void clear()
    {
        // remove listeners
        // listeners.clear();
        
        // propage clear
        holdingRegisters.clear();
        coils.clear();
        clearFunctions();
        clearTuning();
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
     * duplicates. Also, note that the instances returned are only those modified
     * by the user, by means of scripts. It won't return the predefined standard
     * functions, such holding registers and coils.
     * @return
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

    private void loadHoldingRegisters(Node node)
    {
        if( node == null )
        {
            return;
        }
        holdingRegisters.load(node);
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

    public boolean isEnabled()
    {
        return enabled;
    }

    @Deprecated
    public void load(Node node)
    {
        load(null,node);
    }

    @Deprecated
    public void load(Node node, boolean importMode)
    {
        load(null,node,importMode);
    }

    /**
     * Loads the parameters of a ModbusSlave object by reading the content
     * of the given DOM.
     * @param node
     */
    public void load(ModbusPalProject mpp, Node node)
    {
        load(mpp, node,true);
    }

    /**
     *
     * @param slave
     * @param node
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
            String id = XMLTools.getAttribute(XML_SLAVE_ID_ATTRIBUTE, node);
            slaveId = Integer.valueOf(id);

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


    public final void save(OutputStream out, boolean withBindings)
    throws IOException
    {
        String openTag = xmlOpenTag();
        out.write( openTag.getBytes() );

        saveHoldingRegisters(out, withBindings);
        saveCoils(out,withBindings);
        saveFunctions(out);
        saveTuning(out);
        
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
    }

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
    public long getMaxReplyDelay()
    {
        return maxReplyDelay;
    }

    public long getMinReplyDelay()
    {
        return minReplyDelay;
    }

    public float getNoReplyErrorRate()
    {
        return noReplyRate;
    }
}
