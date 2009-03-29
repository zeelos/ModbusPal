/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.io.File;
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
        // if reference is a file, extract the directory of that file:
        if( reference.isDirectory()==false )
        {
            String directory = reference.getParent();
            reference = new File(directory);
        }

        // make an absolute uri
        URI referenceURI = reference.toURI();

        // make an absolute uri from the reference + the target
        URI result = referenceURI.resolve(target.getPath());
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
}
