/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.main.*;
import modbuspal.main.ModbusConst;
import modbuspal.recorder.ModbusPalRecorder;
import modbuspal.slave.ModbusPduProcessor;
import modbuspal.slave.ModbusSlave;


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

        // get the slave:
        ModbusSlave slave = modbusPalProject.getModbusSlave(slaveID);

        // process the "no reply" error rate of the slave:
        if( Math.random() < slave.getNoReplyErrorRate() )
        {
            System.err.println("Slave "+slaveID+" will no reply (check value error rate)" );
            modbusPalProject.notifyPDUnotServiced();
            return 0;
        }

        byte functionCode = buffer[offset+0];
        ModbusPduProcessor mspp = slave.getPduProcessor(functionCode);
        if( mspp == null )
        {
            int length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
            ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
            modbusPalProject.notifyExceptionResponse();
            return length;
        }

        int length = mspp.processPDU(functionCode, slaveID, buffer, offset, modbusPalProject.isLeanModeEnabled());
        if(length<0)
        {
            System.err.println("Illegal function code "+functionCode);
            length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
        }

        if( isExceptionResponse(buffer,offset)==true )
        {
            modbusPalProject.notifyExceptionResponse();
        }
        else
        {
            modbusPalProject.notifyPDUprocessed();
        }

        // delay the reply
        try {
            Thread.sleep(slave.getReplyDelay());
        } catch (InterruptedException ex) {
            Logger.getLogger(ModbusSlaveProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
        return length;
    }

    public static int makeExceptionResponse(byte functionCode, byte exceptionCode, byte[] buffer, int offset)
    {
        buffer[offset+0] = (byte) (((byte)0x80) | functionCode);
        buffer[offset+1] = exceptionCode;
        return 2;
    }

    public static int makeExceptionResponse(byte exceptionCode, byte[] buffer, int offset)
    {
        buffer[offset+0] |= (byte)0x80;
        buffer[offset+1] = exceptionCode;
        return 2;
    }

    private boolean isExceptionResponse(byte[] buffer, int offset)
    {
        byte b = buffer[offset];
        return( (b&0x80) == 0x80 );
    }

}
