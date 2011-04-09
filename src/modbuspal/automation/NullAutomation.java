/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 *
 * @author nnovic
 */
public class NullAutomation
extends Automation
{
    public static final String NAME = "Null";

    private static NullAutomation instance = new NullAutomation();

    public static NullAutomation getInstance()
    {
        return instance;
    }

    private NullAutomation()
    {
        super(NAME);
    }
}
