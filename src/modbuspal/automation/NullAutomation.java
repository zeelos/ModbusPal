/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 * An automation that does nothing, to be used when the binding
 * does the job a generating values.
 * @author nnovic
 */
public class NullAutomation
extends Automation
{
    /** name of the null automation. */
    public static final String NAME = "Null";

    private static NullAutomation instance = new NullAutomation();

    /**
     * returns the unique instance of the NullAutomation.
     * @return the unique instance of the NullAutomation
     */
    public static NullAutomation getInstance()
    {
        return instance;
    }

    private NullAutomation()
    {
        super(NAME);
    }
}
