/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.python.util.PythonInterpreter;

/**
 *
 * @author avincon
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
