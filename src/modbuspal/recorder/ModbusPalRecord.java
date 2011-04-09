/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.recorder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modbuspal.toolkit.HexaTools;

/**
 *
 * @author nnovic
 */
public class ModbusPalRecord
{
    public static final int RECORD_IN = 1;
    public static final int RECORD_OUT = 2;
    private int recordType = 0;
    private long recordTimestamp = 0;
    private int recordSlave = 0;
    private byte recordData[] = null;
    private int recordDataLength = 0;

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


    public int getType()
    {
        return recordType;
    }

    public long getTimestamp()
    {
        return recordTimestamp;
    }

    public int getSlaveID()
    {
        return recordSlave;
    }

    public byte[] getData()
    {
        return recordData;
    }


    public int getDataLength()
    {
        return recordDataLength;
    }
}
