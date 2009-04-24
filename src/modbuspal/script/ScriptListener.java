/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

/**
 *
 * @author nnovic
 */
public interface ScriptListener
{
    public void scriptAdded(ScriptRunner runner);

    public void scriptRemoved(ScriptRunner runner);
    
    public void startupScriptAdded(ScriptRunner runner);

    public void startupScriptRemoved(ScriptRunner runner);
}
