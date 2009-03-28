/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.python;

import java.io.FileNotFoundException;
import java.io.IOException;
import modbuspal.automation.*;
import java.io.File;
import java.io.OutputStream;
import modbuspal.main.FileTools;

/**
 
 * @author avincon
 */
public abstract class ScriptInstanciator
implements GeneratorInstanciator
{

    public static ScriptInstanciator create(File scriptFile)
    throws FileNotFoundException, IOException
    {
        String extension = FileTools.getExtension(scriptFile);

        if( extension.compareToIgnoreCase("py")==0 )
        {
            return new PythonInstanciator(scriptFile);
        }

        return null;
    }


    
    protected File scriptFile = null;

    
    public ScriptInstanciator(File file)
    {
        scriptFile = file;
    }


    public void save(OutputStream out)
    throws IOException
    {
        // create open tag
        String openTag = "<instanciator>";
        out.write(openTag.getBytes());

        // write file path
        String path = scriptFile.getPath();
        out.write(path.getBytes());

        // create close tag
        String closeTag = "</instanciator>\r\n";
        out.write(closeTag.getBytes());

    }


    public abstract String getFileExtension();


    @Override
    public String getClassName()
    {
        String filename = scriptFile.getName();
        if( filename!=null )
        {
            int index = filename.lastIndexOf('.');
            if( index!=-1 )
            {
                String ext = filename.substring(index);
                if( ext.compareToIgnoreCase("."+ getFileExtension())==0 )
                {
                    return filename.substring(0,index);
                }
            }
        }
        return filename;
    }
}
