/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

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

    public static void setBit(byte[] buffer, int offset, int value)
    {
        // get byte
        int bitOffset = offset%8;
        int byteOffset = offset / 8;
        int val = (int)buffer[byteOffset];

        // clear bit
        if( value==0 )
        {
            int mask = ~(1<<bitOffset);
            val &= mask;
        }

        // set bit
        else
        {
            int mask = (1<<bitOffset);
            val |= mask;
        }
        // put changed value
        buffer[byteOffset] = (byte)(0xFF&val);
    }

    public static int getBit(byte[] buffer, int offset)
    {
        // get byte
        int bitOffset = offset%8;
        int byteOffset = offset / 8;
        int val = (int)buffer[byteOffset];

        int mask = (1<<bitOffset);
        val &= mask;

        val = (val>>bitOffset);
        return val;
    }

}
