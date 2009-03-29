/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

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
        if( extension==null)
        {
            return null;
        }

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
        save(out,null);
    }

    public void save(OutputStream out, File projectFile)
    throws IOException
    {
        // create open tag
        String openTag = "<instanciator>";
        out.write(openTag.getBytes());

        // write absolute file projectPath
        saveAbs(out);

        // write relative file projectPath
        if( projectFile!=null)
        {
            saveRel(out,projectFile);
        }

        // create close tag
        String closeTag = "</instanciator>\r\n";
        out.write(closeTag.getBytes());
    }

    private void saveAbs(OutputStream out)
    throws IOException
    {
        // create open tag
        String openTag = "<abs>";
        out.write(openTag.getBytes());

        // write abs file projectPath
        String path = scriptFile.getPath();
        out.write(path.getBytes());

        // create close tag
        String closeTag = "</abs>\r\n";
        out.write(closeTag.getBytes());
    }

    private void saveRel(OutputStream out, File projectFile)
    throws IOException
    {
        String rel = FileTools.makeRelative(projectFile, scriptFile);

        if( rel != null )
        {
            // create open tag
            String openTag = "<rel>";
            out.write(openTag.getBytes());

            out.write( rel.getBytes() );

            // create close tag
            String closeTag = "</rel>\r\n";
            out.write(closeTag.getBytes());
        }
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
