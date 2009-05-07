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
    private PythonRunner instanciator;

    
    void install(PythonRunner inst)
    {
        instanciator = inst;
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
    public int getSize()
    {
        return 0;
    }

    @Override
    public int getRegister(int rank, double value)
    {
        return 0;
    }

    @Override
    public boolean getCoil(int rank, double value)
    {
        return super.getCoil(rank,value);
    }

    @Override
    protected String getClassName()
    {
        return instanciator.getClassName();
    }
}
