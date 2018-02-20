/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.recorder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modbuspal.slave.ModbusSlaveAddress;
import modbuspal.toolkit.HexaTools;

/**
 * This class represents an entry from a recorded MODBUS session. Basically,
 * a record is either in incoming MODBUS request, or an outgoind reply.
 * @author nnovic
 */
public class ModbusPalRecord
{
    /** indicates that a record is incoming, issued from an external device toward modbuspal. */
    public static final int RECORD_IN = 1;
    
    /** indicates that a record is outgoing, issued by modbuspal toward an external device. */
    public static final int RECORD_OUT = 2;
    
    private int recordType = 0;
    private long recordTimestamp = 0;
    private int recordSlave = 0;
    private byte recordData[] = null;
    private int recordDataLength = 0;

    
    /**
     * Creates a new record, with the data provided in argument.
     * The object will parse the String in order to extract the meaningful
     * data.
     * @param line entry from the record file, containing the data of ther record.
     */
    public ModbusPalRecord(String line)
    {
        line = line.trim();
        Pattern p = Pattern.compile("^<(\\p{Alpha}+)\\p{Space}+timestamp=(\\p{Digit}+)\\p{Space}+slave=(\\p{Digit}+)>([0-9a-fA-F]+)</(\\p{Alpha}+)>$");
        Matcher m = p.matcher(line);
        boolean b = m.matches();

        // in or out
        String tag = m.group(1);
        String end = m.group(5);
        if( tag.compareTo(end)!=0 )
        {
            throw new RuntimeException();
        }
        else if( tag.compareTo("in")==0 )
        {
            recordType = RECORD_IN;
        }
        else if( tag.compareTo("out")==0 )
        {
            recordType = RECORD_OUT;
        }

        // timestamp
        String timestamp = m.group(2);
        recordTimestamp = Long.parseLong(timestamp);

        // slave
        String slave = m.group(3);
        recordSlave = Integer.parseInt(slave);

        // data
        String data = m.group(4);
        recordData = new byte[256];
        recordData = HexaTools.toByte(data,recordData);
        recordDataLength = ( data.length()+1 ) / 2;
    }


    /**
     * Gets the type of this record. One of RECORD_IN or RECORD_OUT.
     * @return the type of the record.
     */
    public int getType()
    {
        return recordType;
    }

    /**
     * Gets the date/time (in POSIX format) associated with this record
     * @return timestamp of this record
     */
    public long getTimestamp()
    {
        return recordTimestamp;
    }

    /**
     * returns the slave id associated with this record. for a record of type 
     * RECORD_IN, it is the slave id of the target of the request. for a record
     * a type RECORD_OUT, it is the slave id of the slave that replied to the
     * request.
     * @return slave id associated with this record
     */
    public ModbusSlaveAddress getSlaveID()
    {
        throw new UnsupportedOperationException("not yet implemented");
        //return recordSlave;
    }

    /**
     * Returns the data contained in the record. for records of type RECORD_IN,
     * the data is an incoming PDU containing a MODBUS request. for records of
     * type RECORD_OUT, the data is an outgoing reply to a request.
     * @return the data contained in the record
     */
    public byte[] getData()
    {
        return recordData;
    }

    /**
     * Returns the size of the data contained in the record
     * @return size of the data contained in the record.
     */
    public int getDataLength()
    {
        return recordDataLength;
    }
}
