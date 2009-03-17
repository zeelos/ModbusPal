/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 *
 * @author nnovic
 */
public class ModbusTools
{
    public static int getUint16(byte[] data, int offset)
    {
        int msb = getUint8( data,offset );
        int lsb = getUint8( data,offset+1 );
        return msb * 256 + lsb;
    }

    public static void setUint16(byte[] data, int offset, int value)
    {
        value = value % 65536;
        int msb = value / 256;
        int lsb = value % 256;
        data[offset] = (byte)msb;
        data[offset+1] = (byte)lsb;
    }


    public static int getUint8(byte[] data, int offset)
    {
        int rc = (int)data[offset];
        if( rc < 0 )
            return rc + 256;
        else
            return rc;
    }



}
