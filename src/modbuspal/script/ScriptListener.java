/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

/**
 *
 * @author avincon
 */
public interface ScriptListener
{

    public void scriptAdded(ScriptRunner runner);
    
    public void startupScriptAdded(ScriptRunner runner);
}
