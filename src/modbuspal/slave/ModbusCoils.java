/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import modbuspal.binding.Binding;
import modbuspal.link.ModbusSlaveProcessor;
import static modbuspal.main.ModbusConst.FC_READ_COILS;
import static modbuspal.main.ModbusConst.FC_READ_DISCRETE_INPUTS;
import static modbuspal.main.ModbusConst.FC_WRITE_MULTIPLE_COILS;
import static modbuspal.main.ModbusConst.FC_WRITE_SINGLE_COIL;
import static modbuspal.main.ModbusConst.XC_ILLEGAL_DATA_ADDRESS;
import static modbuspal.main.ModbusConst.XC_ILLEGAL_DATA_VALUE;
import modbuspal.master.ModbusMasterRequest;
import modbuspal.toolkit.ModbusTools;

/**
 * Storage for the coils of a modbus slave
 * @author nnovic
 */
public class ModbusCoils
extends ModbusRegisters
{
    /**
     * Creates a new instance of ModbusCoils
     */
    public ModbusCoils()
    {
        super();
        TXT_REGISTER = "coil";
        TXT_REGISTERS = "coils";
    }

    @Override
    public int processPDU(byte functionCode, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch( functionCode )
        {
            case FC_READ_COILS: return processReadMultipleCoilsRequest(functionCode, buffer, offset, createIfNotExist);
            case FC_READ_DISCRETE_INPUTS: return processReadMultipleCoilsRequest(functionCode, buffer, offset, createIfNotExist);
            case FC_WRITE_SINGLE_COIL: return processWriteSingleCoilRequest(functionCode, buffer, offset, createIfNotExist);
            case FC_WRITE_MULTIPLE_COILS: return processWriteMultipleCoilsRequest(functionCode, buffer, offset, createIfNotExist);
        }
        return -1;
    }

    @Override
    public int buildPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch( req.getFunctionCode() )
        {
            case FC_READ_COILS: return buildReadMultipleCoilsRequest(req, buffer, offset, createIfNotExist);
            case FC_READ_DISCRETE_INPUTS: return buildReadMultipleCoilsRequest(req, buffer, offset, createIfNotExist);
            case FC_WRITE_SINGLE_COIL: return buildWriteSingleCoilRequest(req, buffer, offset, createIfNotExist);
            case FC_WRITE_MULTIPLE_COILS: return buildWriteMultipleCoilsRequest(req, buffer, offset, createIfNotExist);
        }
        return -1;
    }

    
    
    @Override
    public boolean processPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch(req.getFunctionCode() )
        {
            case FC_READ_COILS: return readMultipleCoilsReply(req, buffer, offset, createIfNotExist);
            case FC_READ_DISCRETE_INPUTS: return readMultipleCoilsReply(req, buffer, offset, createIfNotExist);
            case FC_WRITE_SINGLE_COIL: return writeSingleCoilReply(req, buffer, offset, createIfNotExist);
            case FC_WRITE_MULTIPLE_COILS: return writeMultipleCoilsReply(req, buffer, offset, createIfNotExist);
        }
        return false;
    }
    
    private int processWriteMultipleCoilsRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);
        int byteCount = ModbusTools.getUint8(buffer, offset+5);

        if( (quantity<1) || (quantity>1968) || ( byteCount!=(quantity+7)/8) )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( exist(startingAddress,quantity,createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = setValues(startingAddress,quantity,buffer,offset+6);
        if( rc != (byte)0x00 )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;
    }

    
    
    private int buildWriteMultipleCoilsRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = req.getWriteAddress();
        int quantity = req.getWriteQuantity();
        int byteCount = (quantity+7)/8;

        if( (quantity<1) || (quantity>1968) )
        {
            return -1;
        }

        if( exist(startingAddress,quantity,createIfNotExist) == false )
        {
            return -1;
        }

        buffer[offset+0] = req.getFunctionCode();
        ModbusTools.setUint16(buffer, offset+1, startingAddress);
        ModbusTools.setUint16(buffer, offset+3, quantity);
        ModbusTools.setUint8(buffer, offset+5, byteCount);
        
        byte rc = getValues(startingAddress,quantity,buffer,offset+6);
        if( rc != (byte)0x00 )
        {
            return -1;
        }

        return 6+byteCount;
    }
    
    

    private int processWriteSingleCoilRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int outputAddress = ModbusTools.getUint16(buffer, offset+1);
        int outputValue = ModbusTools.getUint16(buffer, offset+3);

        if( (outputValue!=0x0000) && (outputValue!=0xFF00) )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( exist(outputAddress, 1, createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = setValue(outputAddress,outputValue);
        if( rc != (byte)0x00 )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }

        return 5;
    }


    private int buildWriteSingleCoilRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int outputAddress = req.getWriteAddress();

        if( exist(outputAddress, 1, createIfNotExist) == false )
        {
            return -1;
        }

        buffer[offset+0] = req.getFunctionCode();
        ModbusTools.setUint16(buffer, offset+1, outputAddress);
        
        int outputValue = getValue(outputAddress);
        if(outputValue == 0)
        {
            ModbusTools.setUint16(buffer, offset+3, 0x0000);
        }
        else
        {
            ModbusTools.setUint16(buffer, offset+3, 0xFF00);
        }

        return 5;
    }

    
    
    

    private int processReadMultipleCoilsRequest(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);

        if( (quantity<1) || (quantity>2000) )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( exist(startingAddress,quantity, createIfNotExist) == false )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte byteCount = (byte) (  (quantity+7)/8 );
        buffer[offset+1] = byteCount;
        byte rc = getValues(startingAddress,quantity,buffer,offset+2);
        if( rc != (byte)0x00 )
        {
            return ModbusSlaveProcessor.makeExceptionResponse(functionCode, rc, buffer, offset);
        }
        return 2 + byteCount;
    }


    private int buildReadMultipleCoilsRequest(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist)
    {
        int startingAddress = req.getReadAddress();
        int quantity = req.getReadQuantity();

        if( (quantity<1) || (quantity>2000) )
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
        
        return 5;
    }

    
    
    
    
    
    private boolean readMultipleCoilsReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist) 
    {
        int startingAddress = req.getReadAddress();
        int quantity = req.getReadQuantity();
        
        int byteCount = ModbusTools.getUint8(buffer, offset+1);
        if( byteCount != (quantity+7)/8 )
        {
            return false;
        }

        if( exist(startingAddress,quantity,createIfNotExist) == false )
        {
            return false;
        }

        // read registers from buffer
        for(int i=0; i<quantity; i++)
        {
            int reg = ModbusTools.getBit(buffer, (offset+2)*8 + i );
            setValue(startingAddress+i, reg);            
        }

        return true;
    }

    @Override
    protected int getValue(Binding binding)
    {
        if( binding.getCoil()==true )
        {
            return 1;
        }
        return 0;
    }

    @Override
    public byte getValues(int startingAddress, int quantity, byte[] buffer, int offset)
    {

        for(int i=0; i<quantity; i++)
        {
            Integer reg = getValue(startingAddress+i);
            ModbusTools.setBit(buffer, (offset*8)+i, reg);
        }
        return XC_SUCCESSFUL;
    }

    @Override
    byte setValueSilent(int address, int val)
    {
        if( val==0 )
        {
            return super.setValueSilent(address,0);
        }
        return super.setValueSilent(address,1);
    }

    @Override
    public byte setValues(int startingAddress, int quantity, byte[] buffer, int offset)
    {
        byte retval = XC_SUCCESSFUL;
        for(int i=0; i<quantity; i++)
        {
            Integer reg = ModbusTools.getBit(buffer, (offset*8) + i);
            retval = setValueSilent(startingAddress+i,reg);
            if( retval != XC_SUCCESSFUL )
            {
                break;
            }
        }
        notifyTableChanged();
        return retval;
    }


    @Override
    protected Integer checkValueBoundaries(Integer value)
    {
        if( value<0 )
        {
            return 0;
        }
        if( value>1 )
        {
            return 1;
        }
        return value;
    }



    private boolean writeSingleCoilReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean writeMultipleCoilsReply(ModbusMasterRequest req, byte[] buffer, int offset, boolean createIfNotExist) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
