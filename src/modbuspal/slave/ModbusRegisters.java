/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import modbuspal.binding.Binding;
import modbuspal.main.ModbusConst;
import modbuspal.main.ModbusValuesMap;
import modbuspal.main.ModbusPalXML;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
public class ModbusRegisters
implements TableModel, ModbusPalXML, ModbusConst
{
    class RegisterCopy
    {
        int registerAddress;
        Integer registerValue;
        String registerName;
        Binding registerBinding;
        RegisterCopy(int address, Integer value, String name, Binding b)
        {
            registerAddress = address;
            registerValue = value;
            registerName = name;
            registerBinding = b;
        }
    }

    public static final String ADDRESS_COLUMN_NAME = "Address";

    public static final int ADDRESS_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;
    public static final int NAME_COLUMN_INDEX = 2;
    public static final int BINDING_COLUMN_INDEX = 3;

    protected String TXT_REGISTER = "register";
    protected String TXT_REGISTERS = "registers";

    //private Vector<Integer> registers = new Vector<Integer>(65536);
    //private Hashtable<Integer,Integer> values = new Hashtable<Integer,Integer>(65536);
    private ModbusValuesMap values = new ModbusValuesMap();
    private Hashtable<Integer,String> names = new Hashtable<Integer,String>(65536);
    private ArrayList<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();
    private Hashtable<Integer,Binding> bindings = new Hashtable<Integer,Binding>(65536);
    private int addressOffset = 1;

    //==========================================================================
    //
    // REGISTERS
    //
    //==========================================================================


    /**
     * Creates and initialize several registers with default value.
     * @see #add creates one register with the specified settings.
     * @param startingAddress
     * @param quantity
     */
    public void create(int startingAddress, int quantity)
    {
        values.addIndexes(startingAddress, quantity);
        notifyTableChanged();
    }

    @Deprecated
    public int getRegisterImpl(int address)
    {
        return getValueImpl(address);
    }

    @Deprecated
    public int getRegister(int address)
    {
        return getValueImpl(address);
    }

    /**
     * Returns the value of the register, whose address is provided
     * in argument. Note that depending on the implementation (modbus/jbus),
     * an offset is applied to address.
     * After the offset has been applied, returns the same result as getValue()
     * @param address the address of the register
     * @return see getValue() for info.
     */
    public int getValueImpl(int address)
    {
        address -= getOffset();
        return getValue(address);
    }

    /**
     * Returns the value of the register, whose address is provided
     * in argument. Note that address is indexed starting from 0, with no
     * consideration for the implementation offset (modbus/jbus). If the
     * register is bound to an automation, returns the current value of the
     * automation.
     * @param address the address of the register, indexed starting from 0
     * @return value of the register. If the register doesn't exist, returns 0
     * by default.
     */
    public int getValue(int address)
    {
        Binding binding = bindings.get(address);
        if( binding != null )
        {
            return getValue(binding);
        }
        else
        {
            return values.getByIndex(address);
        }
    }

    protected int getValue(Binding binding)
    {
        return binding.getRegister();
    }


    private void detachAllBindings()
    {
        Collection<Binding> collection = bindings.values();
        for( Binding b:collection)
        {
            b.detach();
        }
    }

    void clear()
    {
        values.clear();
        names.clear();
        detachAllBindings();
        bindings.clear();
        notifyTableChanged();
    }


    /**
     * Sets the value of the register identified by the specified address. Note that
     * address is indexed starting from 0, whatever the slave's modbus implementation
     * is (modbus,jbus,...). If the register is bound to an automation, this method
     * has no effect on the current value of the automation.
     * If address does not exist, an error code is returned and no action is performed.
     * This method does not trigger events to refresh the GUI.
     * @param address
     * @param val
     * @return
     */
    byte setValueSilent(int address, int val)
    {
        if( values.indexExists(address) == false )
        {
            return XC_ILLEGAL_DATA_ADDRESS;
        }
        values.putByIndex(address,val);
        return XC_SUCCESSFUL;
    }


    @Deprecated
    public byte setRegisterImpl(int address, int val)
    {
        return setValueImpl(address,val);
    }

    /**
     * * Sets the value of the register identified by the specified address.
     * Note that depending on the implementation (modbus/jbus),
     * an offset is applied to address.
     * After the offset has been applied, returns the same result as setValue()
     * @param address the address of the register
     * @return see setValue() for info.
     */
    public byte setValueImpl(int address, int val)
    {
        address -= getOffset();
        return setValue(address,val);
    }

    @Deprecated
    public byte setRegister(int address, int val)
    {
        return setValue(address,val);
    }

    /**
     * Sets the value of the register identified by the specified address. Note that
     * address is indexed starting from 0, whatever the slave's modbus implementation
     * is (modbus,jbus,...). If the register is bound to an automation, this method
     * has no effect on the current value of the automation.
     * If address does not exist, an error code is returned and no action is performed.
     * This method will trigger a TableEvent event, so that the GUI is refreshed.
     * @param address
     * @param val
     * @return the modbus error code indicating the success of the failure of the
     * action. In case of success, the returned value is XC_SUCCESSFUL (0x00).
     */
    public byte setValue(int address, int val)
    {
        byte retval = setValueSilent(address,val);
        notifyTableChanged(rowIndexOf(address), VALUE_COLUMN_INDEX);
        return retval;
    }


    public int rowIndexOf(int address)
    {
        return values.getOrderOf(address);
    }

    private void set(int address, Integer value, String name, Binding binding)
    {
        // set the value of the register
        if( value == null )
        {
            value = 0;
        }
        values.putByIndex(address, value);

        // set the name of the register
        if( name != null )
        {
            names.put(address, name );
        }

        // set the binding of the register
        if( binding != null )
        {
            Binding old = bindings.get(address);
            if( old!=null )
            {
                old.detach();
            }
            binding.attach(this,address);
            bindings.put(address, binding );
        }

        notifyTableChanged( values.getOrderOf(address) );
    }


    /**
     * Check if a range of registers, defined by the starting address
     * and the quantity of registers in the range, is already defined
     * or not.
     * @param startingAddress
     * @param quantity
     * @return true if all registers comprised in the range are already defined;
     * @return false if any register in the range is not defined yet.
     */
    public boolean exist(int startingAddress, int quantity)
    {
        assert( startingAddress >= 0 );
        assert( quantity >= 0 );

        for( int i=0; i<quantity; i++ )
        {
            int address = startingAddress + i;
            if( exist(address) == false )
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Check if a particular register, defined by its address,
     * is already defined or not.
     * @param address
     * @return true if the register is already defined
     * @return false otherwise
     */
    public boolean exist(int address)
    {
        assert( address >= 0 );

        if( values.indexExists(address) == false )
        {
            return false;
        }
        return true;
    }


    public void remove(int address)
    {
        // check if a binding exists
        Binding binding = bindings.get( address );
        if( binding != null )
        {
            binding.detach();
            bindings.remove(address);
        }

        // delete register
        values.delete( address );
        names.remove( address );
        notifyTableChanged();
    }




    void replace(ModbusRegisters source, int srcAddress, int dstAddress)
    {
        Integer value = source.values.getByIndex(srcAddress);
        String name = source.names.get(srcAddress);
        Binding binding = source.bindings.get(srcAddress);
        set(dstAddress, value, name, binding);
    }


    void paste(int destAddress, RegisterCopy src)
    {
        try
        {
            Binding b = (Binding) src.registerBinding.clone();
            set( destAddress, src.registerValue, src.registerName, b);
        }
        catch(CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
    }

    RegisterCopy copy (ModbusRegisters source, int sourceAddress)
    {
        // AddIndexes the register if necessary
        if( values.indexExists(sourceAddress) == false )
        {
            return null;
        }

        Integer value = source.values.getByIndex(sourceAddress);
        String name = source.names.get(sourceAddress);
        Binding binding = source.bindings.get(sourceAddress);
        return new RegisterCopy( sourceAddress, value, name, binding );
    }


    //==========================================================================
    //
    // BINDINGS
    //
    //==========================================================================

    /**
     * Associates the register identified by its address with the
     * provided binding. Note that address is indexed starting from 0,
     * whatever the slave's modbus implementation is (modbus,jbus,...). If a binding
     * already exists, it is replaced by the new.
     * @param address
     * @param binding
     */
    public void bind(int address, Binding binding)
    {
        bindings.put(address, binding);
        int row = values.getOrderOf(address);
        notifyTableChanged(row);
        binding.attach(this,address);
    }

    public void unbind(int address)
    {
        Binding removed = bindings.remove(address);
        removed.detach();
        notifyTableChanged( values.getOrderOf(address) );
    }


    /**
     * Checks if any of the registers has a binding definition.
     * @return true if any of the registers has a binding defined, false if none
     * of the register has a binding.
     */
    public boolean hasBindings()
    {
        return (bindings.size()>0);
    }

    public boolean isBound(int address)
    {
        Binding binding = bindings.get(address);
        return (binding!=null);
    }

    public void removeAllBindings(String classname)
    {
        Enumeration<Integer> addresses = bindings.keys();
        while( addresses.hasMoreElements() )
        {
            Integer address = addresses.nextElement();
            Binding b = bindings.get(address);
            if( b.getClassName().compareTo(classname)==0 )
            {
                bindings.remove(address);
            }
        }
        notifyTableChanged();
    }

    //==========================================================================
    //
    // TABLE MODEL IMPLEMENTATION
    //
    //==========================================================================

    public int getRowCount()
    {
        return values.getCount();
    }

    public int getColumnCount()
    {
        return 4;
    }

    public String getColumnName(int columnIndex)
    {
        switch(columnIndex)
        {
            case ADDRESS_COLUMN_INDEX: return ADDRESS_COLUMN_NAME;
            case VALUE_COLUMN_INDEX: return "Value";
            case NAME_COLUMN_INDEX: return "Name";
            case BINDING_COLUMN_INDEX: return "Binding";
            default: return "Column " + String.valueOf(columnIndex);
        }
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        switch(columnIndex)
        {
            case ADDRESS_COLUMN_INDEX: return Integer.class;
            case VALUE_COLUMN_INDEX: return String.class;
            case NAME_COLUMN_INDEX: return String.class;
            case BINDING_COLUMN_INDEX: return String.class;
            default: return Object.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case ADDRESS_COLUMN_INDEX: return false;
            case VALUE_COLUMN_INDEX: return !isBound( getAddressOf(rowIndex) );
            case NAME_COLUMN_INDEX: return true;
            case BINDING_COLUMN_INDEX: return false;
            default: return false;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        int index = values.getIndexOf(rowIndex);
        switch(columnIndex)
        {
            case ADDRESS_COLUMN_INDEX:
            {
                return addressOffset+index;
            }

            case VALUE_COLUMN_INDEX:
            {
                int reg = getValue(index);
                return String.valueOf( reg );
            }
            case NAME_COLUMN_INDEX:
            {
                return names.get(index);
            }
            case BINDING_COLUMN_INDEX:
            {
                Binding binding = bindings.get(index);
                if( binding!=null )
                {
                    return binding.toString();
                }
                else
                {
                    return null;
                }
            }
        }
        return null;
    }


    protected Integer checkValueBoundaries(Integer value)
    {
        if( value<0 )
        {
            return 0;
        }
        if( value>65535 )
        {
            return 65535;
        }
        return value;
    }



    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        int index = values.getIndexOf(rowIndex);
        switch( columnIndex )
        {
            case VALUE_COLUMN_INDEX:
            {
                if( aValue instanceof String )
                {
                    Integer val = Integer.parseInt((String)aValue);
                    values.putByIndex(index, checkValueBoundaries(val));
                    notifyTableChanged(rowIndex,columnIndex);
                }
                break;
            }

            case NAME_COLUMN_INDEX:
            {
                if( aValue instanceof String )
                {
                    String val = (String)aValue;
                    names.put(index, val);
                    notifyTableChanged(rowIndex,columnIndex);
                }
                break;
            }
        }
    }

    public void addTableModelListener(TableModelListener l)
    {
        tableModelListeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        tableModelListeners.remove(l);
    }

    //==========================================================================
    //
    // OTHER PROPERTIES
    //
    //==========================================================================


    public String getName(int address)
    {
        return names.get(address);
    }

    public int getAddressOf(int row)
    {
        int index = values.getIndexOf(row);
        return index;
    }

    Collection<String> getRequiredAutomations()
    {
        ArrayList<String> automationNames = new ArrayList<String>();
        Enumeration<Binding> en = bindings.elements();
        while( en.hasMoreElements() )
        {
            Binding b = en.nextElement();
            String n = b.getAutomationName();
            if( automationNames.contains(n)==false )
            {
                automationNames.add(n);
            }
        }
        return automationNames;
    }

    void setOffset(int offset)
    {
        if( offset != addressOffset )
        {
            addressOffset = offset;
            notifyTableChanged();
        }
    }

    int getOffset()
    {
        return addressOffset;
    }


    //==========================================================================
    //
    // FILE INPUTS/OUTPUTS
    //
    //==========================================================================

    public void save(OutputStream out, boolean withBindings)
    throws IOException
    {
        for( int i=0; i<values.getCount(); i++ )
        {
            int index = values.getIndexOf(i);

            StringBuffer tag = new StringBuffer("<"+TXT_REGISTER+" "+ XML_ADDRESS_ATTRIBUTE +"=\"");
            tag.append(String.valueOf(index));
            tag.append("\" value=\"");
            
            int val = values.getByIndex(index);
            tag.append(String.valueOf(val));
            tag.append("\"");

            String name= names.get(index);
            if( name != null )
            {
                tag.append(" name=\"");
                tag.append(name);
                tag.append("\"");
            }

            Binding binding = bindings.get(index);
            if( (binding==null) || (withBindings==false) )
            {
                tag.append("/>\r\n");
                out.write( tag.toString().getBytes() );
            }
            else
            {
                tag.append(">\r\n");
                out.write( tag.toString().getBytes() );
                binding.save(out);
                String closeTag = "</"+TXT_REGISTER+">\r\n";
                out.write( closeTag.getBytes() );
            }
        }
    }

    public void load(Node node)
    {
        NodeList nodes = node.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++ )
        {
            Node child = nodes.item(i);
            if( child.getNodeName().compareTo(TXT_REGISTER)==0 )
            {
                loadRegister(child);
            }
        }
        notifyTableChanged();
    }

    private void loadRegister(Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        Integer address = null;
        Integer value = null;
        String name = null;

        for(int i=0; i<attributes.getLength(); i++)
        {
            Node attr = attributes.item(i);
            String nodeName = attr.getNodeName();

            if( nodeName.compareTo(XML_ADDRESS_ATTRIBUTE)==0 )
            {
                address = new Integer( attr.getNodeValue() );
            }
            else if( nodeName.compareTo("value")==0 )
            {
                value = new Integer( attr.getNodeValue() );
            }
            else if( nodeName.compareTo("name")==0 )
            {
                name = attr.getNodeValue();
            }
        }

        // TODO: throw an exception if those assertions are false.
        assert( address != null );
        assert( value != null );

        if( values.indexExists(address)==false )
        {
            values.addIndex(address);
        }

        values.putByIndex( address, value );

        if( name != null )
        {
            names.put(address, name);
        }

        // NOTE: bindings are loaded later, see MainGui::loadBindings()
    }





    //==========================================================================
    //
    // EVENTS MANAGEMENT
    //
    //==========================================================================


    public void notifyTableChanged()
    {
        TableModelEvent event = new TableModelEvent(this);
        for(TableModelListener l:tableModelListeners)
        {
            l.tableChanged(event);
        }
    }

    public void notifyRegisterChanged(int registerAddress)
    {
        int index = rowIndexOf(registerAddress);
        notifyTableChanged(index);
    }

    private void notifyTableChanged(int rowIndex)
    {
        TableModelEvent event = new TableModelEvent(this,rowIndex);
        for(TableModelListener l:tableModelListeners)
        {
            l.tableChanged(event);
        }
    }


    private void notifyTableChanged(int rowIndex, int columnIndex)
    {
        TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
        for(TableModelListener l:tableModelListeners)
        {
            l.tableChanged(event);
        }
    }

}
