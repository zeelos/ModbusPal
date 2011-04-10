/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import modbuspal.binding.Binding;

/**
 *
 * @author nnovic
 */
public class PythonBinding
extends Binding
{
    @Override
    public int getSize()
    {
        return 0;
    }

    @Override
    public int getRegister(int rank, double value)
    {
        return 0;
    }

    /**
     * the script should override this method and put
     * its initialization commands in here.
     */
    public void init()
    {
        return;
    }

    @Override
    public Binding newInstance()
    throws InstantiationException, IllegalAccessException
    {
        PythonBinding pb = (PythonBinding)super.newInstance();
        pb.init();
        return pb;
    }


}
