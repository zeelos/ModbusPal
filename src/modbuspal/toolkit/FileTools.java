/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

/**
 *
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



    public static String makeAbsolute(File reference, File target)
    {
        return makeAbsolute(reference, target.getPath() );
    }


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

    public static void append(File file, String s)
    throws IOException
    {
        FileWriter fw = new FileWriter(file);
        fw.append(s);
        fw.close();
    }

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
}
