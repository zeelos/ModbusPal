/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.toolkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import modbuspal.help.HelpViewer;

/**
 *
 * @author nnovic
 */
public class SystemTools 
{
    public static boolean IsWindowsHost()
    {
        // retrieve path to JRE
        String osName = System.getProperty("os.name");
        System.out.printf("os.name returns \"%s\"\r\n", osName);
        
        if( osName.startsWith("Windows")==true )
        {
            return true;
        }
        return false;
    }
    
    
    public static boolean IsWindows64bits()
    {
        boolean is64bit = false;
        is64bit = (System.getenv("ProgramFiles(x86)") != null);
        //} 
        //else 
        //{
        //    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
        //}
        return is64bit;
    }

    
    public static void Install(String src, File dst) throws IOException 
    {
        System.out.printf("Installing \"%s\" to \"%s\"\r\n", src, dst.getName());
        URL url = ClassLoader.getSystemResource(src);
        
        InputStream is = url.openStream();
        try
        {
            FileTools.copyTo(is, dst);
            System.out.println("   OK.");
        }
        finally
        {
            is.close();
        }
        
    }
}
