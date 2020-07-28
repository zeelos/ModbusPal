/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import modbuspal.binding.Binding;
import modbuspal.link.ModbusSlaveProcessor;
import modbuspal.main.ModbusConst;
import static modbuspal.main.ModbusConst.FC_READ_HOLDING_REGISTERS;
import static modbuspal.main.ModbusConst.FC_READ_WRITE_MULTIPLE_REGISTERS;
import static modbuspal.main.ModbusConst.FC_WRITE_MULTIPLE_REGISTERS;
import static modbuspal.main.ModbusConst.FC_WRITE_SINGLE_REGISTER;
import static modbuspal.main.ModbusConst.FC_READ_EXTENDED_REGISTERS;
import static modbuspal.main.ModbusConst.XC_ILLEGAL_DATA_ADDRESS;
import static modbuspal.main.ModbusConst.XC_ILLEGAL_DATA_VALUE;
import static modbuspal.main.ModbusConst.XC_SUCCESSFUL;
import modbuspal.main.ModbusValuesMap;
import modbuspal.main.ModbusPalXML;
import modbuspal.master.ModbusMasterRequest;
import modbuspal.toolkit.ModbusTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Storage for the holding registers of a modbus slave
 * @author nnovic
 */
public class ModbusExtendedRegisters
implements ModbusPduProcessor, TableModel, ModbusPalXML, ModbusConst
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

    /** name of the column displaying the address of the registers in the TableModel. */
    public static final String ADDRESS_COLUMN_NAME = "Address";

    /** index of the column displaying the address of the registers in the TableModel. */
    public static final int ADDRESS_COLUMN_INDEX = 0;

    /** index of the column displaying the value of the registers in the TableModel. */
    public static final int VALUE_COLUMN_INDEX = 1;

    /** index of the column displaying the name of the registers in the TableModel. */
    public static final int NAME_COLUMN_INDEX = 2;

    /** index of the column displaying the binding of the registers in the TableModel. */
    public static final int BINDING_COLUMN_INDEX = 3;

    /** defines how should be called a single value from the TableModel. */
    protected String TXT_REGISTER = "register";

    /** defines the plural form of the TXT_REGISTER word. */
    protected String TXT_REGISTERS = "registers";

    private ModbusValuesMap values = new ModbusValuesMap();
    private HashMap<Integer,String> names = new HashMap<Integer,String>(65536);
    private ArrayList<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();
    private HashMap<Integer,Binding> bindings = new HashMap<Integer,Binding>(65536);
    private int addressOffset = 0;

    //==========================================================================
    //
    // PDU PROCESSOR
    //
    //==========================================================================

    @Override
    public int processPDU(byte functionCode, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch( functionCode )
        {
            case FC_READ_EXTENDED_REGISTERS: return processReadMultipleRegistersRequest(functionCode, buffer, offset, createIfNotExist);
        }
        return -1;
    }

    @Override
    public int buildPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch( req.getFunctionCode() )
        {
            case FC_READ_EXTENDED_REGISTERS: return buildReadMultipleRegistersRequest(req, buffer, offset, createIfNotExist);
        }
        return -1;
    }

    @Override
    public boolean processPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch(req.getFunctionCode() )
        {
            case FC_READ_EXTENDED_REGISTERS: return readMultipleRegistersReply(req, buffer, offset, createIfNotExist);
        }
        return false;
    }


    @Override
    public JPanel getPduPane()
    {
        return null;
    }

    @Override
    public String getClassName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public ModbusPduProcessor newInstance()
    throws InstantiationException, IllegalAccessException
    {
        return getClass().newInstance();
    }


    @Override
    public void init()
    {
    }

    @Override
    public void reset()
    {
    }


    private int processReadWriteMultipleRegistersRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int byteCount = ModbusTools.getUint8(buffer, offset+1);
        int readStartingAddress = ModbusTools.getUint16(buffer, offset+5);
        int quantityToRead = ModbusTools.getUint16(buffer, offset+7);

        // verify quantity to read
        if( (quantityToRead<1) || (quantityToRead>118) )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        // verify that registers to be read exist:
        if( exist(readStartingAddress,quantityToRead,createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        // then perform read operation:
        buffer[offset+1] = (byte) (2*quantityToRead);
        for(int i=0; i<quantityToRead; i++)
        {
            Integer reg = getValue(readStartingAddress+i);
            ModbusTools.setUint16(buffer, offset+2+(2*i), reg);
        }

        return 2 + (2*quantityToRead);
    }



    private int buildReadWriteMultipleRegistersRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int readStartingAddress = req.getReadAddress();
        int quantityToRead = req.getReadQuantity();

        // verify quantity to read
        if( (quantityToRead<1) || (quantityToRead>118) )
        {
            return -1;
        }

        // verify that registers to be read exist:
        if( exist(readStartingAddress,quantityToRead,createIfNotExist) == false )
        {
            return -1;
        }

        buffer[offset+0] = req.getFunctionCode();
        ModbusTools.setUint8(buffer, offset+1, 7);
        ModbusTools.setUint8(buffer, offset+2, 5);
        ModbusTools.setUint8(buffer, offset+3, 6);
        ModbusTools.setUint16(buffer, offset+4, readStartingAddress);
        ModbusTools.setUint16(buffer, offset+6, quantityToRead);

        return 10+quantityToRead;
    }


    private boolean readWriteMultipleRegistersReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int readStartingAddress = req.getReadAddress();
        int quantityToRead = req.getReadQuantity();
        int byteCount = ModbusTools.getUint8(buffer, offset+1);

        // verify quantity to read
        if( byteCount != 2*quantityToRead )
        {
            return false;
        }

        // verify that registers to be read exist:
        if( exist(readStartingAddress,quantityToRead,createIfNotExist) == false )
        {
            return false;
        }

        byte rc = setValues(readStartingAddress, quantityToRead, buffer, offset+2);
        if( rc != XC_SUCCESSFUL )
        {
            return false;
        }

        /*// then perform read operation:
        buffer[offset+1] = (byte) (2*quantityToRead);
        for(int i=0; i<quantityToRead; i++)
        {
            Integer reg = getValue(readStartingAddress+i);
            ModbusTools.setUint16(buffer, offset+2+(2*i), reg);
        }*/

        return true;
    }





    private int processWriteMultipleRegistersRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);
        int byteCount = ModbusTools.getUint8(buffer, offset+5);

        if( (quantity<1) || (quantity>123) || (byteCount!=2*quantity) )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( exist(startingAddress,quantity, createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = setValues(startingAddress, quantity, buffer, offset+6);
        if( rc != XC_SUCCESSFUL )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;
    }


    private int buildWriteMultipleRegistersRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = req.getWriteAddress();
        int quantity = req.getWriteQuantity();
        int byteCount = quantity * 2;

        if( (quantity<1) || (quantity>123) )
        {
            return -1;
        }

        if( exist(startingAddress,quantity, createIfNotExist) == false )
        {
            return -1;
        }

        buffer[offset+0] = req.getFunctionCode();
        ModbusTools.setUint16(buffer, offset+1, startingAddress);
        ModbusTools.setUint16(buffer, offset+3, quantity);
        ModbusTools.setUint8(buffer, offset+5, byteCount);

        byte rc = getValues(startingAddress, quantity, buffer, offset+6);
        if( rc != XC_SUCCESSFUL )
        {
            return -1;
        }

        return 6+byteCount;
    }

    private boolean writeMultipleRegistersReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);

        if( startingAddress != req.getWriteAddress() )
        {
            return false;
        }

        if( quantity != req.getWriteQuantity() )
        {
            return false;
        }

        if( exist(startingAddress,quantity, createIfNotExist) == false )
        {
            return false;
        }

        /*byte rc = setValues(startingAddress, quantity, buffer, offset+6);
        if( rc != XC_SUCCESSFUL )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;*/
        return true;
    }



    private int processWriteSingleRegisterRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int address = ModbusTools.getUint16(buffer, offset+1);

        if( exist(address, 1, createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        Integer reg = ModbusTools.getUint16(buffer, offset + 3);
        byte rc = setValue(address,reg);
        if( rc != XC_SUCCESSFUL )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;
    }



    private int buildWriteSingleRegisterRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int address = req.getWriteAddress();

        if( exist(address, 1, createIfNotExist) == false )
        {
            return -1;
        }

        int value = getValue(address);

        buffer[offset+0] = req.getFunctionCode();
        ModbusTools.setUint16(buffer, offset+1, address);
        ModbusTools.setUint16(buffer, offset+3, value);

        return 5;
    }

    private boolean writeSingleRegisterReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int address = req.getWriteAddress();

        if( exist(address, 1, createIfNotExist) == false )
        {
            return false;
        }

        /*Integer reg = ModbusTools.getUint16(buffer, offset + 3);
        byte rc = setValue(address,reg);
        if( rc != XC_SUCCESSFUL )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;*/
        return true;
    }





    private int processReadMultipleRegistersRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int numberOfGroups = ModbusTools.getUint8(buffer, offset+1) / 7;

        int groupFileNumber[] = new int[numberOfGroups];
        int groupStartingAddress[] = new int[numberOfGroups];
        int groupQuantity[] = new int[numberOfGroups];

        int tempOffset = 3;

        for (int i=0; i < numberOfGroups; i++) {
          groupFileNumber[i] = ModbusTools.getUint16(buffer, offset+tempOffset+(i*7));
          groupStartingAddress[i] = ModbusTools.getUint16(buffer, offset+tempOffset+(i*7)+2);
          groupQuantity[i] = ModbusTools.getUint16(buffer, offset+tempOffset+(i*7)+4);

          if( (groupQuantity[i]<1) || (groupQuantity[i]>125) )
          {
              return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
          }

          if( exist(groupStartingAddress[i],groupQuantity[i],createIfNotExist) == false )
          {
              return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
          }
        }

        int responsePDUSize = 2;
        for (int i = 0; i < numberOfGroups; i++) {
          responsePDUSize += 2 * groupQuantity[i] + 2;
        }

        if (responsePDUSize > 125) {
          return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        buffer[offset+1] = (byte) (responsePDUSize - 2);

        tempOffset = 2;
        for (int i = 0; i < numberOfGroups; i++) {
          buffer[offset+tempOffset] = (byte) (2*groupQuantity[i] + 1);
          buffer[offset+tempOffset+1] = (byte) (6);

          // write registers
          for(int j=0; j<groupQuantity[i]; j++)
          {
              Integer reg = getValue((groupFileNumber[i] - 1) * 10000 + groupStartingAddress[i] + j);
              ModbusTools.setUint16(buffer, offset+tempOffset+2+(2*j), reg);
          }
          tempOffset += 2 * groupQuantity[i] + 2;
        }

        return responsePDUSize;
    }


    private int buildReadMultipleRegistersRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        ModbusTools.setUint8(buffer, offset+0, req.getFunctionCode()); // function code
        ModbusTools.setUint16(buffer, offset+1, req.getReadAddress()); // starting address
        ModbusTools.setUint16(buffer, offset+3, req.getReadQuantity()); // quantity of registers
        return 5;
    }



    private boolean readMultipleRegistersReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = req.getReadAddress();
        int quantity = req.getReadQuantity();

        int byteCount = ModbusTools.getUint8(buffer, offset+1);
        if( byteCount != 2*quantity )
        {
            return false;
        }

        if( exist(startingAddress,quantity,createIfNotExist) == false )
        {
            return false;
        }

        // write byte count
        buffer[offset+1] = (byte) (2*quantity + 5);


        // read registers from buffer
        for(int i=0; i<quantity; i++)
        {
            int reg = ModbusTools.getUint16(buffer, offset+2+(2*i) );
            setValue(startingAddress+i, reg);
        }

        return true;
    }



    //==========================================================================
    //
    // REGISTERS
    //
    //==========================================================================


    /**
     * Creates and initialize several registers with default value.
     * @param startingIndex
     * @param quantity
     */
    public void create(int startingIndex, int quantity)
    {
        values.addIndexes(startingIndex, quantity);
        notifyTableChanged();
    }


    /**
     * @see #getValueImpl(int)
     * @param address
     * @return the value of the specified register
     * @deprecated modify method name because it is overridden by ModbusCoils
     */
    @Deprecated
    public int getRegisterImpl(int address)
    {
        return getValueImpl(address);
    }

    /**
     * @see #getValue(int)
     * @param index
     * @return the value of the specified register
     * @deprecated modify method name because it is overridden by ModbusCoils
     */
    @Deprecated
    public int getRegister(int index)
    {
        return getValue(index);
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
     * Returns the value of the register, whose index is provided
     * in argument. Note that index always starts from 0, with no
     * consideration for the implementation offset (modbus/jbus). If the
     * register is bound to an automation, returns the current value of the
     * automation.
     * @param index the index of the register, starting from 0
     * @return value of the register. If the register doesn't exist, returns 0
     * by default.
     */
    public int getValue(int index)
    {
        Binding binding = bindings.get(index);
        if( binding != null )
        {
            return getValue(binding);
        }
        else
        {
            return values.getByIndex(index);
        }
    }

    /**
     * Returns the value provided by the binding.
     * @param binding the binding that provides the value
     * @return the value given by the binding.
     */
    protected int getValue(Binding binding)
    {
        return binding.getRegister();
    }

    /**
     * Writes a range of values into the provided byte buffer.
     * @param startingIndex the starting index of the values to write
     * @param quantity the number of values to write
     * @param buffer the byte buffer where to write the values
     * @param offset the offset where to start writing in the byte buffer
     * @return XC_SUCCESSFUL if operation is successful
     */
    public byte getValues(int startingIndex, int quantity, byte[] buffer, int offset)
    {
        for(int i=0; i<quantity; i++)
        {
            Integer reg = getValue(startingIndex+i);
            ModbusTools.setUint16(buffer, offset+(2*i), reg);
        }
        return XC_SUCCESSFUL;
    }


    private void detachAllBindings()
    {
        Collection<Binding> collection = bindings.values();
        for( Binding b:collection)
        {
            b.detachExtendedRegisters();
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
     * Sets the value of the register identified by the specified index. Note that
     * indexes start from 0, whatever the slave's modbus implementation
     * is (modbus,jbus,...). If the register is bound to an automation, this method
     * has no effect on the current value of the automation.
     * If index does not exist, an error code is returned and no action is performed.
     * This method does not trigger events to refresh the GUI.
     * @param index
     * @param val
     * @return
     */
    byte setValueSilent(int index, int val)
    {
        if( values.indexExists(index) == false )
        {
            return XC_ILLEGAL_DATA_ADDRESS;
        }
        values.putByIndex(index,val);
        return XC_SUCCESSFUL;
    }


    /**
     * @see #setValueImpl(int, int)
     * @param address
     * @param val
     * @return XC_SUCCESSFUL if successful
     * @deprecated name changed because this method is overridden by ModbusCoils
     */
    @Deprecated
    public byte setRegisterImpl(int address, int val)
    {
        return setValueImpl(address,val);
    }

    /**
     * Sets the value of the register identified by the specified address.
     * Note that depending on the implementation (modbus/jbus),
     * an offset is applied to address.
     * After the offset has been applied, returns the same result as setValue()
     * @param address the address of the register
     * @param val the value of the register
     * @return see setValue() for info.
     */
    public byte setValueImpl(int address, int val)
    {
        address -= getOffset();
        return setValue(address,val);
    }

    /**
     * @see #setValue(int, int)
     * @param index
     * @param val
     * @return XC_SUCCESSFUL if successful
     * @deprecated name changed because this method is overridden by ModbusCoils
     */
    @Deprecated
    public byte setRegister(int index, int val)
    {
        return setValue(index,val);
    }

    /**
     * Sets the value of the register identified by the specified index. Note that
     * indexEs start from 0, whatever the slave's modbus implementation
     * is (modbus,jbus,...). If the register is bound to an automation, this method
     * has no effect on the current value of the automation.
     * If index does not exist, an error code is returned and no action is performed.
     * This method will trigger a TableEvent event, so that the GUI is refreshed.
     * @param index
     * @param val
     * @return the modbus error code indicating the success of the failure of the
     * action. In case of success, the returned value is XC_SUCCESSFUL (0x00).
     */
    public byte setValue(int index, int val)
    {
        byte retval = setValueSilent(index,val);
        if( retval==XC_SUCCESSFUL )
        {
            notifyTableChanged(rowIndexOf(index), VALUE_COLUMN_INDEX);
        }
        return retval;
    }

    /**
     * Reads the content of the byte buffer, which contains a range of values to update
     * @param startingAddress the starting address of the range
     * @param quantity the number of values to update
     * @param buffer the byte buffer containing the values
     * @param offset the offset in the buffer where the values start
     * @return XC_SUCCESSFUL if operation successful
     */
    public byte setValues(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        byte retval = XC_SUCCESSFUL;
        for(int i=0; i<quantity; i++)
        {
            Integer reg = ModbusTools.getUint16(buffer, offset + 2* i);
            retval = setValueSilent(startingAddress+i,reg);
            if( retval != XC_SUCCESSFUL )
            {
                break;
            }
        }
        notifyTableChanged();
        return retval;
    }

    /**
     * Convert the index of a value into the index of the row where this
     * value is displayed in the TableModel.
     * @param index index of the value
     * @return index of the row where the value is displayed
     */
    public int rowIndexOf(int index)
    {
        return values.getOrderOf(index);
    }

    private void set(int index, Integer value, String name, Binding binding)
    {
        // set the value of the register
        if( value == null )
        {
            value = 0;
        }
        values.putByIndex(index, value);

        // set the name of the register
        if( name != null )
        {
            names.put(index, name );
        }

        // set the binding of the register
        if( binding != null )
        {
            Binding old = bindings.get(index);
            if( old!=null )
            {
                old.detachExtendedRegisters();
            }
            binding.attachExtendedRegisters(this,index);
            bindings.put(index, binding );
        }

        notifyTableChanged( values.getOrderOf(index) );
    }


    /**
     * Checks if the given values are defined, but do not create them
     * if they don't exist.
     * @see #exist(int, int, boolean)
     * @param startingIndex
     * @param quantity
     * @return true if all values exist, false otherwise.
     */
    public boolean exist(int startingIndex, int quantity)
    {
        return exist(startingIndex, quantity, false);
    }

    /**
     * Check if a range of registers, defined by the starting index
     * and the quantity of registers in the range, is already defined
     * or not. This method does not take the selected implementation into
     * account. The first value is always at index 0.
     * @param startingIndex the index of the first value in the range,
     * @param quantity the number of values to check.
     * @param createIfNotExist if true, the values are created if they don't exist
     * @return true if all registers comprised in the range are already defined;
     * false if any register in the range is not defined yet.
     */
    public boolean exist(int startingIndex, int quantity, boolean createIfNotExist)
    {
        assert( startingIndex >= 0 );
        assert( quantity >= 0 );

        for( int i=0; i<quantity; i++ )
        {
            int address = startingIndex + i;
            if( exist(address) == false )
            {
                if( createIfNotExist==true )
                {
                    create(address, 1);
                }
                else
                {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Check if a particular register, defined by its index,
     * is already defined or not.
     * @param index
     * @return true if the register is already defined,
     * false otherwise
     */
    public boolean exist(int index)
    {
        assert( index >= 0 );

        if( values.indexExists(index) == false )
        {
            return false;
        }
        return true;
    }


    /**
     * Removes a value from this object. The index is not sensitive to the
     * selected implementation. The first index is always 0.
     * @param index index of the value to remove
     */
    public void remove(int index)
    {
        // check if a binding exists
        Binding binding = bindings.get( index );
        if( binding != null )
        {
            binding.detachExtendedRegisters();
            bindings.remove(index);
        }

        // delete register
        values.delete( index );
        names.remove( index );
        notifyTableChanged();
    }




    void replace(ModbusExtendedRegisters source, int srcIndex, int dstIndex)
    {
        Integer value = source.values.getByIndex(srcIndex);
        String name = source.names.get(srcIndex);
        Binding binding = source.bindings.get(srcIndex);
        set(dstIndex, value, name, binding);
    }


    void paste(int destIndex, RegisterCopy src)
    {
        try
        {
            Binding b = null;
            if( src.registerBinding!=null )
            {
                b = (Binding) src.registerBinding.clone();
            }
            set( destIndex, src.registerValue, src.registerName, b);
        }
        catch(CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
    }

    RegisterCopy copy (ModbusExtendedRegisters source, int sourceIndex)
    {
        // AddIndexes the register if necessary
        if( values.indexExists(sourceIndex) == false )
        {
            return null;
        }

        Integer value = source.values.getByIndex(sourceIndex);
        String name = source.names.get(sourceIndex);
        Binding binding = source.bindings.get(sourceIndex);
        return new RegisterCopy( sourceIndex, value, name, binding );
    }


    //==========================================================================
    //
    // BINDINGS
    //
    //==========================================================================

    /**
     * Associates the register identified by its index with the
     * provided binding. Note that indexes start from 0,
     * whatever the slave's modbus implementation is (modbus,jbus,...). If a binding
     * already exists, it is replaced by the new.
     * @param index
     * @param binding
     */
    public void bind(int index, Binding binding)
    {
        bindings.put(index, binding);
        int row = values.getOrderOf(index);
        notifyTableChanged(row);
        binding.attachExtendedRegisters(this,index);
    }

    /**
     * Removes the binding for the value (identified by its index) and whatever
     * automation it is linked to.
     * @param index index of the value from which the binding must be removed.
     */
    public void unbind(int index)
    {
        Binding removed = bindings.remove(index);
        if( removed!=null )
        {
            removed.detachExtendedRegisters();
            notifyTableChanged( values.getOrderOf(index) );
        }
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

    /**
     * Checks if the value, identified by its index, has a binding or not.
     * @param index index of the register to check
     * @return true if the register is bound to an automation.
     */
    public boolean isBound(int index)
    {
        Binding binding = bindings.get(index);
        return (binding!=null);
    }

    /**
     * Removes any bindings that is of the specified class name.
     * @param classname the class name of the bindings to remove
     */
    public void removeAllBindings(String classname)
    {
        Set<Integer> addressesSet = bindings.keySet();
        Integer addressesArray[] = new Integer[0];
        addressesArray = addressesSet.toArray(addressesArray);
        for(int i=0; i<addressesArray.length; i++)
        {
            Integer address = addressesArray[i];
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

    @Override
    public int getRowCount()
    {
        return values.getCount();
    }

    @Override
    public int getColumnCount()
    {
        return 4;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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


    /**
     * Checks if the specified value is within the allowed boundaries. For a
     * MODBUS register, the allowed range is [0-65535]. For a coil, it is [0-1].
     * @param value the value to check
     * @return true if the value is within boundaries.
     */
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



    @Override
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

    @Override
    public void addTableModelListener(TableModelListener l)
    {
        tableModelListeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l)
    {
        tableModelListeners.remove(l);
    }

    //==========================================================================
    //
    // OTHER PROPERTIES
    //
    //==========================================================================


    /**
     * Gets the name that has been defined for the specified value, identified
     * by its index.
     * @param index index of the value
     * @return name given to the specified value
     */
    public String getName(int index)
    {
        return names.get(index);
    }

    /**
     * Converts the row from the TableModel into the corresponding value index
     * @param row the row index from the table model
     * @return the value index
     */
    public int getAddressOf(int row)
    {
        int index = values.getIndexOf(row);
        return index;
    }

    /**
     * Gets a list of all the automations that are being used by this
     * object, through the declared bindings.
     * @return a collection of strings containing the classnames of the automations
     * in use in this object.
     */
    Collection<String> getRequiredAutomations()
    {
        ArrayList<String> automationNames = new ArrayList<String>();
        Collection<Binding> en = bindings.values();
        for(Binding b:en)
        {
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


    /**
     * Saves the values and settings of this object.
     * @param out the output stream where to write the values and settings
     * @param withBindings if true, the bindings of each value are also
     * described in the output stream.
     * @throws IOException
     */
    public void save(OutputStream out, boolean withBindings)
    throws IOException
    {
        for( int i=0; i<values.getCount(); i++ )
        {
            int index = values.getIndexOf(i);

            StringBuilder tag = new StringBuilder("<"+TXT_REGISTER+" "+ XML_ADDRESS_ATTRIBUTE +"=\"");
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

    /**
     * Loads the values and settings from the provided DOM node.
     * @param node the node containing the values and settings to load.
     */
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



    @Override
    public void savePduProcessorSettings(OutputStream out)
    throws IOException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void loadPduProcessorSettings(NodeList list)
    {
        throw new UnsupportedOperationException("not implemented");
    }


    //==========================================================================
    //
    // EVENTS MANAGEMENT
    //
    //==========================================================================


    /**
     * Triggers a "tableChanged" event for the registered TableModelListeners.
     */
    public void notifyTableChanged()
    {
        TableModelEvent event = new TableModelEvent(this);
        for(TableModelListener l:tableModelListeners)
        {
            l.tableChanged(event);
        }
    }

    /**
     * Triggers a "tableChanged" event for the registered TableModelListeners.
     * The event specifically addresses the row where the modified value is
     * displayed.
     * @param valueIndex index of the modified value (register/coil)
     */
    public void notifyRegisterChanged(int valueIndex)
    {
        int index = rowIndexOf(valueIndex);
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
