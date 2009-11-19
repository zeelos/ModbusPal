/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Antony Miguel aem@themoog.net
 */
public class ClassPathHacker
{
    private static final Class[] parameters = new Class[]{URL.class};

    public static void addFile(String s)
    throws IOException
    {
	File f = new File(s);
	addFile(f);
    }

    public static void addFile(File f)
    throws IOException
    {
	addURL(f.toURL());
    }


    public static void addURL(URL u)
    throws IOException
    {
        // get the class loader:
	URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();

        // check if specified url is already in defined in the class loader:
        URL urls[] = sysloader.getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            if( urls[i].sameFile(u) == true )
            {
                return;
            }
        }

        System.out.println("Add "+u+" to classpath");

	Class sysclass = URLClassLoader.class;

	try {
		Method method = sysclass.getDeclaredMethod("addURL",parameters);
		method.setAccessible(true);
		method.invoke(sysloader,new Object[]{ u });
	} catch (Throwable t) {
		t.printStackTrace();
		throw new IOException("Error, could not add URL to system classloader");
	}//end try catch
    }//end method
    
}
