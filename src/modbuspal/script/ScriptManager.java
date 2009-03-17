/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.main.XMLTools;
import org.python.util.PythonInterpreter;
import org.w3c.dom.Node;

/**
 *
 * @author nnovic
 */
public class ScriptManager
implements Runnable
{
    private File scriptFile;
    private String status = "stopped";
    private ArrayList<ScriptListener> scriptListeners = new ArrayList<ScriptListener>();

    public ScriptManager(File source)
    {
        scriptFile = source;
    }

    public ScriptManager(Node node)
    {
        // find "language"
        String language = XMLTools.getAttribute("language", node);
        if( language!=null )
        {

        }

        // find "src"
        String src = XMLTools.getAttribute("src", node);
        scriptFile = new File(src);
    }

    public String getName()
    {
        return scriptFile.getName();
    }

    public String getFullFilename()
    {
        return scriptFile.getPath();
    }

    public String getStatus()
    {
        return status;
    }

    public void save(OutputStream out)
    throws IOException
    {
        StringBuffer tag = new StringBuffer("<script language=\"python\" ");
        tag.append("src=\"" + scriptFile.getPath() + "\" ");
        tag.append("/>\r\n");
        out.write( tag.toString().getBytes() );
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.setName("ScriptManager "+getName());
        thread.start();
    }

    public void run()
    {
        System.out.println("Starting script " + scriptFile.getName());
        PythonInterpreter interp = new PythonInterpreter();

        FileInputStream input = null;
        try
        {
            input = new FileInputStream(scriptFile);
            interp.execfile(input, scriptFile.getName() );
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ScriptManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        if( input!= null )
        {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(ScriptManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Script " + scriptFile.getName() + " finished.");
        notifyScriptHasEnded();
    }


    public void addScriptListener(ScriptListener l)
    {
        if( scriptListeners.contains(l)==false )
        {
            scriptListeners.add(l);
        }
    }

    public void removeScriptListener(ScriptListener l)
    {
        if( scriptListeners.contains(l)==true )
        {
            scriptListeners.remove(l);
        }
    }


    private void notifyScriptHasEnded()
    {
        for(ScriptListener l:scriptListeners)
        {
            l.scriptHasEnded(this);
        }
    }
}
