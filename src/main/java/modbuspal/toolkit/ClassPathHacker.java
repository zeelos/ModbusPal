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
 * tools for adding files and directories dynamically to the classpath
 * @author Antony Miguel aem@themoog.net
 */
public class ClassPathHacker
{
    private static final Class[] parameters = new Class[]{URL.class};

    /**
     * Adds a file (usually a JAR file then) or a directory to the classpath
     * of the java virtual machine running the application.
     * @param s the full path and name of the file or directory to add into the classpath
     * @throws IOException 
     */
    public static void addFile(String s)
    throws IOException
    {
	File f = new File(s);
	addFile(f);
    }

    /**
     * Adds a file (usually a JAR file then) or a directory to the classpath
     * of the java virtual machine running the application.
     * @param f the file or directory to add into the classpath
     * @throws IOException 
     */
    public static void addFile(File f)
    throws IOException
    {
	addURL(f.toURI().toURL());
    }

    /**
     * Adds a file (usually a JAR file then) or a directory to the classpath
     * of the java virtual machine running the application.
     * @param u the url of the file or directory to add into the classpath
     * @throws IOException 
     */
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
