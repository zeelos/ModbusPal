/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.main.*;
import modbuspal.main.ModbusConst;
import static modbuspal.main.ModbusConst.XC_ILLEGAL_FUNCTION;
import modbuspal.master.ModbusMasterRequest;
import modbuspal.recorder.ModbusPalRecorder;
import modbuspal.slave.ModbusPduProcessor;
import modbuspal.slave.ModbusSlave;
import modbuspal.slave.ModbusSlaveAddress;


/**
 * the abstract class for processing incoming PDU 
 * @author nnovic
 */
public abstract class ModbusSlaveProcessor
implements ModbusConst
{
    /**
     * reference on the project that hold all the information
     * for the modbus slaves to simulate
     */
    protected final ModbusPalProject modbusPalProject;

    /**
     * Constructor that stores the reference of the modbuspal project
     * @param mpp 
     */
    protected ModbusSlaveProcessor(ModbusPalProject mpp)
    {
        modbusPalProject = mpp;
    }

    /**
     * The subclass will call this method in order to process the content of
     * the PDU that has been received from the master.
     * The reply is written in the same buffer where the request was transmitted.
     * @param slaveID the slave identifier of the target MODBUS device 
     * @param buffer a byte buffer containing the MODBUS PDU
     * @param offset the offset in the buffer where the PDU actually starts
     * @param pduLength the length of the PDU.
     * @return the size of the reply. if less than 1, modbuspal considers that
     * there is no reply to the request.
     */
    protected int processPDU(ModbusSlaveAddress slaveID, byte[] buffer, int offset, int pduLength)
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
            System.err.println("Unsupported function code "+functionCode);
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

    
    
    
    protected int buildPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset/*, int pduLength*/)
    {
        // record the request
        // ModbusPalRecorder.recordIncoming(slaveID,buffer,offset,pduLength);

        // check if the slave is enabled
        if( modbusPalProject.isSlaveEnabled(slaveID) == false )
        {
            System.err.println("Slave "+slaveID+" is not enabled");
            req.notifyPDUnotServiced();
            modbusPalProject.notifyPDUnotServiced();
            return -1;
        }

        
        byte functionCode = req.getFunctionCode();
        
        
        // get the slave:
        ModbusSlave slave = modbusPalProject.getModbusSlave(slaveID);

        // retrive the pdu processor for the modbus function:
        
        ModbusPduProcessor mspp = slave.getPduProcessor(functionCode);
        if( mspp == null )
        {
            System.err.println("Unsupported function code "+functionCode);
            //int length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
            //ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
            req.notifyExceptionResponse();
            modbusPalProject.notifyExceptionResponse();
            return -1;
        }

        int length = mspp.buildPDU(req, slaveID, buffer, offset, modbusPalProject.isLeanModeEnabled());
        if(length==-1)
        {
            System.err.println("Illegal function code "+functionCode);
            req.notifyPDUnotServiced();
            modbusPalProject.notifyPDUnotServiced();
            return -1;
        }

        //req.notifyPDUprocessed();
        //modbusPalProject.notifyPDUprocessed();       

        return length;
    }    
    
    
    
    
    
    /**
     * The subclass will call this method in order to process the content of
     * the PDU that has been received from the slave.
     * @param slaveID the slave identifier of the replying MODBUS device 
     * @param buffer a byte buffer containing the MODBUS PDU
     * @param offset the offset in the buffer where the PDU actually starts
     * @param pduLength the length of the PDU.
     * @return 
     */
    protected boolean processPDU(ModbusMasterRequest req, ModbusSlaveAddress slaveID, byte[] buffer, int offset, int pduLength)
    {
        // record the request
        // ModbusPalRecorder.recordIncoming(slaveID,buffer,offset,pduLength);

        // check if the slave is enabled
        if( modbusPalProject.isSlaveEnabled(slaveID) == false )
        {
            System.err.println("Slave "+slaveID+" is not enabled");
            req.notifyPDUnotServiced();
            modbusPalProject.notifyPDUnotServiced();
            return false;
        }

        // get the slave:
        ModbusSlave slave = modbusPalProject.getModbusSlave(slaveID);

        byte functionCode = buffer[offset+0];
        
        // 
        if( isExceptionResponse(buffer,offset)==true )
        {
            req.notifyExceptionResponse();
            modbusPalProject.notifyExceptionResponse();
            return false;
        }

        
        // retrive the pdu processor for the modbus function:
        
        ModbusPduProcessor mspp = slave.getPduProcessor(functionCode);
        if( mspp == null )
        {
            System.err.println("Unsupported function code "+functionCode);
            //int length = makeExceptionResponse(functionCode,XC_ILLEGAL_FUNCTION, buffer, offset);
            //ModbusPalRecorder.recordOutgoing(slaveID,buffer,offset,length);
            req.notifyExceptionResponse();
            modbusPalProject.notifyExceptionResponse();
            return false;
        }

        boolean result = mspp.processPDU(req, slaveID, buffer, offset, modbusPalProject.isLeanModeEnabled());
        if(result==false)
        {
            System.err.println("Illegal function code "+functionCode);
            req.notifyPDUnotServiced();
            modbusPalProject.notifyPDUnotServiced();
            return false;
        }

        req.notifyPDUprocessed();
        modbusPalProject.notifyPDUprocessed();       

        return true;
    }

    /**
     * Builds a standard exception response given the specified arguments.
     * @param functionCode the function code that generates the exception
     * @param exceptionCode the code that explicits the cause of the exception
     * @param buffer the buffer where to write the exception
     * @param offset the offset in the buffer where to start writing
     * @return the length of the exception response
     */
    public static int makeExceptionResponse(byte functionCode, byte exceptionCode, byte[] buffer, int offset)
    {
        buffer[offset+0] = (byte) (((byte)0x80) | functionCode);
        buffer[offset+1] = exceptionCode;
        return 2;
    }

    /**
     * Builds a standard exception response given the specified arguments.
     * The function code is OR-ed with 0x80.
     * @param exceptionCode the code that explicits the cause of the exception
     * @param buffer the buffer where to write the exception
     * @param offset the offset in the buffer where to start writing
     * @return the length of the exception response
     */
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
