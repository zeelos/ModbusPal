/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

/**
 *
 * @author avincon
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
}
