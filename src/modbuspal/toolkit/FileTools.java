/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * various file-related utilities
 * @author nnovic
 */
public class FileTools
{
    /**
     * Returns the extension of the file.
     * @param file file from which you want to get the extension
     * @return the extension or null in case of failure
     */
    public static String getExtension(File file)
    {
        // check if its a real file !
        if( file.isFile()==false )
        {
            return null;
        }

        // get only the filename
        String filename = file.getName();
        if( filename==null )
        {
            return null;
        }

        // find the extension delimiter
        int index = filename.lastIndexOf('.');
        if( index!= -1 )
        {
            // if found, return the extension
            return filename.substring(index+1);
        }

        return null;
    }


    /**
     * Creates an absolute path name for the "target" file. The "target" file
     * is a relative path, and it is made absolute by resolving the "target" relative
     * path against the "reference" absolute path.
     * @param reference a file with absolute path that is the root for building
     * an absolute pathname for "target"
     * @param target a file with a relative path.
     * @return a string containing the absolute pathname for "target", using
     * "reference" as the root directory.
     */
    public static String makeAbsolute(File reference, File target)
    {
        return makeAbsolute(reference, target.getPath() );
    }


    /**
     * Creates an absolute path name for the "target" file. The "target" string
     * is a relative path, and it is made absolute by resolving the "target" relative
     * path against the "reference" absolute path.
     * @param reference a file with absolute path that is the root for building
     * an absolute pathname for "target"
     * @param target a string with a relative path.
     * @return a string containing the absolute pathname for "target", using
     * "reference" as the root directory.
     */
    public static String makeAbsolute(File reference, String target)
    {
        // if reference is a file, extract the directory of that file:
        if( reference.isDirectory()==false )
        {
            String directory = reference.getParent();
            reference = new File(directory);
        }

        // make an absolute uri
        URI referenceURI = reference.toURI();

        // make an absolute uri from the reference + the target
        URI result = referenceURI.resolve(target);
        result.normalize();

        // check that the resulting uri is absolute
        if( result.isAbsolute()==false )
        {
            // if not, return null;
            return null;
        }

        // return the resulting absolute path
        return result.getPath();
    }


    /**
     * This method will return the path of "target" relatively to "reference".
     * @param reference the reference 
     * @param target the file for which a relative path is requested
     * @return a string containing the relative path of target in comparison
     * with reference.
     */
    public static String makeRelative(File reference, File target)
    {
        // if reference is a file, extract the directory of that file:
        if( reference.isDirectory()==false )
        {
            String directory = reference.getParent();
            reference = new File(directory);
        }

        // make absolute URIs
        URI referenceURI = reference.toURI();
        URI targetURI = target.toURI();

        // create a relative uri from two absolute uris
        URI result = referenceURI.relativize(targetURI);
        result.normalize();

        // if the resulting uri still absolute, return null
        if( result.isAbsolute() )
        {
            return null;
        }

        return result.getPath();
    }

    /**
     * Appends the provided string into the specified file, by creating
     * a temporary FileWriter. 
     * @param file the file to write into
     * @param s the string to write into the file
     * @throws IOException 
     */
    public static void append(File file, String s)
    throws IOException
    {
        FileWriter fw = new FileWriter(file);
        fw.append(s);
        fw.close();
    }

    /**
     * Scans the content of a file and look for the specified string.
     * @param file the file to scan
     * @param line the string to look for
     * @return true if the string was found in the file. false otherwise
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static boolean containsLine(File file, String line)
    throws FileNotFoundException, IOException
    {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        boolean rc = false;
        
        String l = br.readLine();
        while( l!=null )
        {
            if( l.contains(line)==true )
            {
                rc = true;
            }
            l = br.readLine();
        }
        fr.close();
        return rc;
    }
    
    
    public static void copyTo(InputStream src, File dst) 
    throws FileNotFoundException, IOException
    {
        FileOutputStream fos = new FileOutputStream(dst);
        try
        {
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while(  (bytesRead=src.read(buffer)) != -1 )
            {
                fos.write(buffer, 0, bytesRead);
            }
        }
        finally
        {
            fos.close();
        }
    }
}
