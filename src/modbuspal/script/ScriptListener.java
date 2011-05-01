/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

/**
 * objects interested in receiving notifications about scripts
 * must implement this interface
 * @author nnovic
 */
public interface ScriptListener
{
    /**
     * Notifies the listener that a script has been added to the project
     * @param runner the object encapsulating the script
     */
    public void scriptAdded(ScriptRunner runner);

    /**
     * Notifies the listener that a script has been removed from the project
     * @param runner the object encapsulating the script
     */
    public void scriptRemoved(ScriptRunner runner);
}
