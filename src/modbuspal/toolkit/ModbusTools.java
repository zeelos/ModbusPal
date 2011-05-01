/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

/**
 * functions for handling data conversions 
 * @author nnovic
 */
public class ModbusTools
{
    /**
     * concatenates two bytes to make a 16-bit word, using the MODBUS spec 
     * conventions
     * @param data buffer containing the bytes
     * @param offset offset of the first of the two bytes
     * @return the resulting 16-bit word
     */
    public static int getUint16(byte[] data, int offset)
    {
        int msb = getUint8( data,offset );
        int lsb = getUint8( data,offset+1 );
        return msb * 256 + lsb;
    }

    /**
     * splits a 16-bit word into two bytes and write them into the provided
     * byte buffer.
     * @param data the buffer where the 2 bytes must be written
     * @param offset the position in the buffer where the bytes should be written
     * @param value the 16-bit value to split in two.
     */
    public static void setUint16(byte[] data, int offset, int value)
    {
        value = value % 65536;
        int msb = value / 256;
        int lsb = value % 256;
        data[offset] = (byte)msb;
        data[offset+1] = (byte)lsb;
    }


    /**
     * Gets the value of the specified byte as an unsigned byte.
     * @param data byte buffer
     * @param offset offset of the byte to convert
     * @return the value of the byte as an unsigned byte
     */
    public static int getUint8(byte[] data, int offset)
    {
        int rc = (int)data[offset];
        if( rc < 0 )
            return rc + 256;
        else
            return rc;
    }

    /**
     * Sets the value of a byte into a byte buffer.
     * @param buffer the byte buffer
     * @param offset the offset where to write the byte
     * @param value the value of byte (0-255)
     */
    public static void setUint8(byte[] buffer, int offset, int value)
    {
        int rc = value & 0xFF;
        buffer[offset] = (byte)rc;
    }

    /**
     * Sets the value of the specified bit.
     * @param buffer byte buffer where the bit must be set
     * @param offset the offset (in bits) of the bit in the buffer
     * @param value if 0, clears the bit. otherwise, sets the bit
     */
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

    /**
     * Get the value of the specified bit from the byte buffer
     * @param buffer the byte buffer
     * @param offset the offset (in bits) of the bit to read
     * @return 0 or 1.
     */
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
