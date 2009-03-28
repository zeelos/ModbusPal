/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import modbuspal.automation.Generator;

/**
 *
 * @author avincon
 */
public class PythonGenerator
extends Generator
{
    private PythonInstanciator instanciator;
    
    public PythonGenerator()
    {

    }

    @Override
    public String getClassName()
    {
        return instanciator.getClassName();
    }



    void install(PythonInstanciator inst)
    {
        instanciator = inst;
    }

}
