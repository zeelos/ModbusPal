/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

/**
 *
 * @author nnovic
 */
public class HexaTools
{
    public static String toHexa(byte[] buffer, int offset, int length)
    {
        String output = "";
        for(int i=0;i<length;i++)
        {
            output += String.format("%02x", buffer[offset+i]);
        }
        return output;
    }


    public static byte[] toByte(String data)
    {
        int nbBytes = ( data.length()+1 ) / 2;
        byte output[] = new byte[nbBytes];
        return toByte(data,output);
    }



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
