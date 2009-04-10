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
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import modbuspal.binding.Binding;
import modbuspal.main.ModbusConst;
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
    public static final String ADDRESS_COLUMN_NAME = "Address";

    public static final int ADDRESS_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;
    public static final int NAME_COLUMN_INDEX = 2;
    public static final int BINDING_COLUMN_INDEX = 3;

    private Vector<Integer> registers = new Vector<Integer>(65536);
    private Hashtable<Integer,Integer> values = new Hashtable<Integer,Integer>(65536);
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
        assert( startingAddress >= 0 );
        assert( quantity >= 0 );

        // TODO: optimize! because creating all registers from 0 to 65536
        // takes a long time.
        for( int i=0; i<quantity; i++ )
        {
            int address = startingAddress + i;
            if( registers.contains(address) == false)
            {
                registers.add(address);
                values.put( address, new Integer(0) );
            }
        }
        notifyTableChanged();
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
    public int getRegister(int address)
    {
        Binding binding = bindings.get(address);
        if( binding != null )
        {
            return binding.getRegister();
        }
        else
        {
            return values.get(address);
        }
    }

    void clear()
    {
        registers.clear();
        values.clear();
        names.clear();
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
    byte setRegisterSilent(int address, int val)
    {
        if( registers.contains(address) == false )
        {
            return XC_ILLEGAL_DATA_ADDRESS;
        }
        values.put(address,val);
        return XC_SUCCESSFUL;
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
    public byte setRegister(int address, int val)
    {
        byte retval = setRegisterSilent(address,val);
        notifyTableChanged(indexOf(address), VALUE_COLUMN_INDEX);
        return retval;
    }


    public int indexOf(int address)
    {
        return registers.indexOf(address);
    }

    private void set(int address, Integer value, String name, Binding binding)
    {
        // set the value of the register
        if( value == null )
        {
            value = 0;
        }
        values.put(address, value);

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

        notifyTableChanged( registers.indexOf(address) );
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
            int index = startingAddress + i;
            if( values.get(index) == null )
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

        if( values.get(address) == null )
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
            bindings.remove(binding);
        }

        // delete register
        registers.remove( (Integer)address );
        values.remove( address );
        names.remove( address );
        notifyTableChanged();
    }




    void replace(ModbusRegisters source, int srcAddress, int dstAddress)
    {
        Integer value = source.values.get(srcAddress);
        String name = source.names.get(srcAddress);
        Binding binding = source.bindings.get(srcAddress);
        set(dstAddress, value, name, binding);
    }



    void add(ModbusRegisters source, int sourceAddress)
    {
        // create the register if necessary
        if( registers.contains(sourceAddress) == false )
        {
            registers.add(sourceAddress);
        }

        Integer value = source.values.get(sourceAddress);
        String name = source.names.get(sourceAddress);
        Binding binding = source.bindings.get(sourceAddress);

        set( sourceAddress, value, name, binding);
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
        int index = registers.indexOf(address);
        notifyTableChanged(index);
        binding.attach(this,address);
    }

    public void unbind(int address)
    {
        Binding removed = bindings.remove(address);
        removed.detach();
        notifyTableChanged( registers.indexOf(address) );
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

    //==========================================================================
    //
    // TABLE MODEL IMPLEMENTATION
    //
    //==========================================================================

    public int getRowCount()
    {
        return values.size();
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
            case VALUE_COLUMN_INDEX: return !isBound( getAddressAt(rowIndex) );
            case NAME_COLUMN_INDEX: return true;
            case BINDING_COLUMN_INDEX: return false;
            default: return false;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        int addr = registers.get(rowIndex);
        switch(columnIndex)
        {
            case ADDRESS_COLUMN_INDEX:
            {
                return addressOffset+addr;
            }

            case VALUE_COLUMN_INDEX:
            {
                return String.valueOf( getRegister(addr) );
            }
            case NAME_COLUMN_INDEX:
            {
                return names.get(addr);
            }
            case BINDING_COLUMN_INDEX:
            {
                Binding binding = bindings.get(addr);
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

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        int reg = registers.get(rowIndex);
        switch( columnIndex )
        {
            case VALUE_COLUMN_INDEX:
            {
                if( aValue instanceof String )
                {
                    Integer val = Integer.parseInt((String)aValue);
                    values.put(reg, val);
                    notifyTableChanged(rowIndex,columnIndex);
                }
                break;
            }

            case NAME_COLUMN_INDEX:
            {
                if( aValue instanceof String )
                {
                    String val = (String)aValue;
                    names.put(reg, val);
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

    public int getAddressAt(int row)
    {
        Integer address = registers.get(row);
        return address;
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
        for( int i=0; i<registers.size(); i++ )
        {
            int index = registers.get(i);

            StringBuffer tag = new StringBuffer("<register "+ XML_REGISTER_ADDRESS_ATTRIBUTE +"=\"");
            tag.append(String.valueOf(index));
            tag.append("\" value=\"");
            
            int val = values.get(index);
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
                String closeTag = "</register>\r\n";
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
            if( child.getNodeName().compareTo("register")==0 )
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

            if( nodeName.compareTo(XML_REGISTER_ADDRESS_ATTRIBUTE)==0 )
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

        if( registers.contains(address)==false )
        {
            registers.add(address);
        }

        values.put( address, value );

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
        int index = indexOf(registerAddress);
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
