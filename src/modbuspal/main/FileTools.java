/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.io.File;

/**
 *
 * @author avincon
 */
public class FileTools
{
    public static String getExtension(File file)
    {
        String filename = file.getName();
        if( filename==null )
        {
            return null;
        }

        int index = filename.lastIndexOf('.');
        if( index!= -1 )
        {
            return filename.substring(index+1);
        }

        return filename;
    }

}
