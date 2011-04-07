/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import modbuspal.binding.Binding;
import modbuspal.link.ModbusSlaveProcessor;
import modbuspal.toolkit.ModbusTools;

/**
 *
 * @author avincon
 */
public class ModbusCoils
extends ModbusRegisters
{
    public ModbusCoils()
    {
        super();
        TXT_REGISTER = "coil";
        TXT_REGISTERS = "coils";
    }

    @Override
    public int processPDU(byte functionCode, int slaveID, byte[] buffer, int offset, boolean createIfNotExist)
    {
        switch( functionCode )
        {
            case FC_READ_COILS: return readMultipleCoils(functionCode, buffer, offset, createIfNotExist);
            case FC_WRITE_SINGLE_COIL: return writeSingleCoil(functionCode, buffer, offset, createIfNotExist);
            case FC_WRITE_MULTIPLE_COILS: return writeMultipleCoils(functionCode, buffer, offset, createIfNotExist);
        }
        return -1;
    }


    private int writeMultipleCoils(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
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


    private int writeSingleCoil(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
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



    private int readMultipleCoils(byte functionCode, byte[] buffer, int offset, boolean createIfNotExist)
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
}
