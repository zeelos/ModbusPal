/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import modbuspal.main.*;


/**
 *
 * @author avincon
 */
public abstract class ModbusSlaveDispatcher
implements ModbusConst
{
    private ModbusPalGui mainGui;

    protected ModbusSlaveDispatcher(ModbusPalGui parent)
    {
        mainGui = parent;
    }

    protected int processPDU(int slaveID, byte[] buffer, int offset, int pduLength)
    {
        mainGui.tilt();

        // check if the slave is enabled
        if( ModbusPal.isSlaveEnabled(slaveID) == false )
        {
            System.err.println("Slave "+slaveID+" is not enabled");
            return 0;
        }

        // check if the function code is enabled
        byte functionCode = buffer[offset+0];
        if( mainGui.isFunctionEnabled(slaveID, functionCode) == false )
        {
            return makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
        }

        switch( functionCode )
        {
            case FC_READ_HOLDING_REGISTERS:
                return readHoldingRegisters(slaveID, buffer, offset);

            case FC_WRITE_MULTIPLE_REGISTERS:
                return writeMultipleRegisters(slaveID, buffer, offset);

            default:
                System.err.println("Illegal function code "+functionCode);
                return makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
        }
    }

    private int makeExceptionResponse(byte functionCode, byte exceptionCode, byte[] buffer, int offset)
    {
        buffer[offset+0] = (byte) (((byte)0x80) | functionCode);
        buffer[offset+1] = exceptionCode;
        return 2;
    }

    int makeExceptionResponse(byte exceptionCode, byte[] buffer, int offset)
    {
        buffer[offset+0] |= (byte)0x80;
        buffer[offset+1] = exceptionCode;
        return 2;
    }

    private int readHoldingRegisters(int slaveID, byte[] buffer, int offset)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);

        if( (quantity<1) || (quantity>125) )
        {
            System.err.println("Read holding registers: bad quantity "+ quantity);
            return makeExceptionResponse(FC_READ_HOLDING_REGISTERS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( ModbusPal.holdingRegistersExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Read holding registers: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_READ_HOLDING_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        buffer[offset+1] = (byte) (2*quantity);
        byte rc = ModbusPal.getHoldingRegisters(slaveID,startingAddress,quantity,buffer,offset+2);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_READ_HOLDING_REGISTERS, rc, buffer, offset);
        }
        return 2 + (2*quantity);
    }

    private int writeMultipleRegisters(int slaveID, byte[] buffer, int offset)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);
        int byteCount = ModbusTools.getUint8(buffer, offset+5);

        if( (quantity<1) || (quantity>123) || (byteCount!=2*quantity) )
        {
            System.err.println("Write multiple registers: bad quantity "+ quantity);
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( ModbusPal.holdingRegistersExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Write multiple registers: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = ModbusPal.setHoldingRegisters(slaveID,startingAddress,quantity,buffer,offset+6);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, rc, buffer, offset);
        }

        return 5;
    }

}
