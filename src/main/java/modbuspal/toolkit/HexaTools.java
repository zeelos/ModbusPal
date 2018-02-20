/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

/**
 * various functions to handle hexadecimal strings
 * @author nnovic
 */
public class HexaTools
{
    /**
     * Converts the content of the byte buffer into a hexadecimal human-readable
     * string.
     * @param buffer raw byte buffer to convert
     * @param offset offset where the data starts
     * @param length length of the data
     * @return an hexadecimal representation of the content of buffer
     */
    public static String toHexa(byte[] buffer, int offset, int length)
    {
        String output = "";
        for(int i=0;i<length;i++)
        {
            output += String.format("%02x", buffer[offset+i]);
        }
        return output;
    }


    /**
     * Scans the provided string, which is supposed to contain hexadecimal
     * data, into a raw byte buffer
     * @param data the string containing the data
     * @return the resulting byte buffer
     */
    public static byte[] toByte(String data)
    {
        int nbBytes = ( data.length()+1 ) / 2;
        byte output[] = new byte[nbBytes];
        return toByte(data,output);
    }



    /**
     * Scans the provided string, which is supposed to contain hexadecimal
     * data, into a raw byte buffer. The output buffer is provided in argument,
     * so that the method does not create a new one.
     * @param data the string containing the data
     * @param output the buffer where the result must be written
     * @return same as output.
     */
    public static byte[] toByte(String data, byte[] output)
    {
        int nbBytes = ( data.length()+1 ) / 2;
        for( int i=0; i<nbBytes; i++ )
        {
            byte b = Byte.parseByte( data.substring(i*2, i*2+2), 16 );
            output[i] = b;
        }
        return output;
    }
}
