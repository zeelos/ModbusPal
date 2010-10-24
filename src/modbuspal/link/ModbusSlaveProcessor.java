/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import modbuspal.toolkit.ModbusTools;
import modbuspal.main.*;
import modbuspal.main.ModbusConst;
import modbuspal.recorder.ModbusPalRecorder;


/**
 *
 * @author nnovic
 */
public abstract class ModbusSlaveProcessor
implements ModbusConst
{
    protected final ModbusPalProject modbusPalProject;

    protected ModbusSlaveProcessor(ModbusPalProject mpp)
    {
        modbusPalProject = mpp;
    }


    protected int processPDU(int slaveID, byte[] buffer, int offset, int pduLength)
    {
        // record the request
        ModbusPalRecorder.recordIncoming(slaveID,buffer,offset,pduLength);

        // check if the slave is enabled
        if( modbusPalProject.isSlaveEnabled(slaveID) == false )
        {
            System.err.println("Slave "+slaveID+" is not enabled");
            modbusPalProject.notifyPDUnotServiced();
            return 0;
        }

        // check if the function code is enabled
        byte functionCode = buffer[offset+0];
        if( modbusPalProject.isFunctionEnabled(slaveID, functionCode) == false )
        {
            int length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
            ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
            modbusPalProject.notifyExceptionResponse();
            return length;
        }

        int length;

        switch( functionCode )
        {
            case FC_READ_HOLDING_REGISTERS:
                length = readHoldingRegisters(slaveID, buffer, offset);
                break;

            case FC_WRITE_SINGLE_REGISTER:
                length = writeSingleRegister(slaveID, buffer, offset);
                break;

            case FC_WRITE_MULTIPLE_REGISTERS:
                length = writeMultipleRegisters(slaveID, buffer, offset);
                break;

            case FC_READ_WRITE_MULTIPLE_REGISTERS:
                length = readWriteMultipleRegisters(slaveID, buffer, offset);
                break;

            case FC_READ_COILS:
                length = readCoils(slaveID, buffer, offset);
                break;

            case FC_WRITE_SINGLE_COIL:
                length = writeSingleCoil(slaveID,buffer,offset);
                break;

            case FC_WRITE_MULTIPLE_COILS:
                length = writeMultipleCoils(slaveID, buffer, offset);
                break;

            default:
                System.err.println("Illegal function code "+functionCode);
                length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
                break;
        }

        if( isExceptionResponse(buffer,offset)==true )
        {
            modbusPalProject.notifyExceptionResponse();
        }
        else
        {
            modbusPalProject.notifyPDUprocessed();
        }

        ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
        return length;
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

        if( modbusPalProject.holdingRegistersExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Read holding registers: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_READ_HOLDING_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        buffer[offset+1] = (byte) (2*quantity);
        byte rc = modbusPalProject.getHoldingRegisters(slaveID,startingAddress,quantity,buffer,offset+2);
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

        if( modbusPalProject.holdingRegistersExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Write multiple registers: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = modbusPalProject.setHoldingRegisters(slaveID,startingAddress,quantity,buffer,offset+6);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, rc, buffer, offset);
        }

        return 5;
    }

    private int writeSingleRegister(int slaveID, byte[] buffer, int offset)
    {
        int address = ModbusTools.getUint16(buffer, offset+1);

        if( modbusPalProject.holdingRegistersExist(slaveID, address, 1) == false )
        {
            System.err.println("Write single register: bad address "+address);
            return makeExceptionResponse(FC_WRITE_SINGLE_REGISTER, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = modbusPalProject.setHoldingRegisters(slaveID,address,1,buffer,offset+3);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_SINGLE_REGISTER, rc, buffer, offset);
        }

        return 5;
    }



    private int readWriteMultipleRegisters(int slaveID, byte[] buffer, int offset)
    {
        int readStartingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantityToRead = ModbusTools.getUint16(buffer, offset+3);
        int writeStartingAddress = ModbusTools.getUint16(buffer, offset+5);
        int quantityToWrite = ModbusTools.getUint16(buffer, offset+7);
        int writeByteCount = ModbusTools.getUint8(buffer, offset+9);

        // verify quantity to read
        if( (quantityToRead<1) || (quantityToRead>118) )
        {
            System.err.println("Read/Write multiple registers: bad read quantity "+ quantityToRead);
            return makeExceptionResponse(FC_READ_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        // verify quantity to write
        if( (quantityToWrite<1) || (quantityToWrite>118) || (writeByteCount!=2*quantityToWrite) )
        {
            System.err.println("Read/Write multiple registers: bad write quantity "+ quantityToWrite);
            return makeExceptionResponse(FC_READ_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        // verify that registers to be read exist:
        if( modbusPalProject.holdingRegistersExist(slaveID, readStartingAddress,quantityToRead) == false )
        {
            System.err.println("Read/Write multiple registers: bad address range "+readStartingAddress+" to "+ readStartingAddress+quantityToRead);
            return makeExceptionResponse(FC_READ_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        // verify that registers to be written exist:
        if( modbusPalProject.holdingRegistersExist(slaveID, writeStartingAddress,quantityToWrite) == false )
        {
            System.err.println("Read/Write multiple registers: bad address range "+writeStartingAddress+" to "+ writeStartingAddress+quantityToWrite);
            return makeExceptionResponse(FC_READ_WRITE_MULTIPLE_REGISTERS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        // perform write operation first:
        byte rc = modbusPalProject.setHoldingRegisters(slaveID,writeStartingAddress,quantityToWrite,buffer,offset+10);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, rc, buffer, offset);
        }

        // then perform read operation:
        buffer[offset+1] = (byte) (2*quantityToRead);
        rc = modbusPalProject.getHoldingRegisters(slaveID,readStartingAddress,quantityToRead,buffer,offset+2);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_READ_WRITE_MULTIPLE_REGISTERS, rc, buffer, offset);
        }
        return 2 + (2*quantityToRead);
    }



    private int readCoils(int slaveID, byte[] buffer, int offset)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);

        if( (quantity<1) || (quantity>2000) )
        {
            System.err.println("Read coils: bad quantity "+ quantity);
            return makeExceptionResponse(FC_READ_COILS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( modbusPalProject.coilsExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Read coils: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_READ_COILS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte byteCount = (byte) (  (quantity+7)/8 );
        buffer[offset+1] = byteCount;
        byte rc = modbusPalProject.getCoils(slaveID,startingAddress,quantity,buffer,offset+2);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_READ_COILS, rc, buffer, offset);
        }
        return 2 + byteCount;
    }

    private int writeMultipleCoils(int slaveID, byte[] buffer, int offset)
    {
        int startingAddress = ModbusTools.getUint16(buffer, offset+1);
        int quantity = ModbusTools.getUint16(buffer, offset+3);
        int byteCount = ModbusTools.getUint8(buffer, offset+5);

        if( (quantity<1) || (quantity>1968) || ( byteCount!=(quantity+7)/8) )
        {
            System.err.println("Write multiple coils: bad quantity "+ quantity);
            return makeExceptionResponse(FC_WRITE_MULTIPLE_COILS, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( modbusPalProject.coilsExist(slaveID, startingAddress,quantity) == false )
        {
            System.err.println("Write multiple coils: bad address range "+startingAddress+" to "+ startingAddress+quantity);
            return makeExceptionResponse(FC_WRITE_MULTIPLE_COILS, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = modbusPalProject.setCoils(slaveID,startingAddress,quantity,buffer,offset+6);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_MULTIPLE_REGISTERS, rc, buffer, offset);
        }

        return 5;
    }


    private int writeSingleCoil(int slaveID, byte[] buffer, int offset)
    {
        int outputAddress = ModbusTools.getUint16(buffer, offset+1);
        int outputValue = ModbusTools.getUint16(buffer, offset+3);

        if( (outputValue!=0x0000) && (outputValue!=0xFF00) )
        {
            System.err.println("Write single coil: bad value "+ outputValue);
            return makeExceptionResponse(FC_WRITE_SINGLE_COIL, XC_ILLEGAL_DATA_VALUE, buffer, offset);
        }

        if( modbusPalProject.coilsExist(slaveID, outputAddress, 1) == false )
        {
            System.err.println("Write single coil: bad address "+outputAddress);
            return makeExceptionResponse(FC_WRITE_SINGLE_COIL, XC_ILLEGAL_DATA_ADDRESS, buffer, offset);
        }

        byte rc = modbusPalProject.setCoil(slaveID,outputAddress,outputValue);
        if( rc != (byte)0x00 )
        {
            return makeExceptionResponse(FC_WRITE_SINGLE_COIL, rc, buffer, offset);
        }

        return 5;
    }

    private boolean isExceptionResponse(byte[] buffer, int offset)
    {
        byte b = buffer[offset];
        return( (b&0x80) == 0x80 );
    }

}
