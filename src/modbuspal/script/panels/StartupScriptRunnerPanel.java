/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script.panels;

import modbuspal.main.ModbusPalProject;
import modbuspal.script.ScriptRunner;
import modbuspal.script.ScriptRunnerPanel;

/**
 *
 * @author nnovic
 */
public class StartupScriptRunnerPanel
extends ScriptRunnerPanel
{
    private final ModbusPalProject modbusPalProject;
    public StartupScriptRunnerPanel(ScriptRunner def, ModbusPalProject p)
    {
        super(def, true);
        modbusPalProject = p;
    }

    @Override
    protected void deleteScript()
    {
        ScriptRunner runner = getScriptRunner();
        System.out.println("Deleting " + runner.getClassName() + " startup script..." );
        modbusPalProject.removeStartupScript( runner );
    }
}

