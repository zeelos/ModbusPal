/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import modbuspal.generator.Generator;

/**
 *
 * @author nnovic
 */
public class PythonGenerator
extends Generator
{
    private PythonRunner instanciator;
    
    public PythonGenerator()
    {

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
    public String getClassName()
    {
        return instanciator.getClassName();
    }

    /**
     * The setIcon method has to be exposed with the "public" modifier,
     * because Jython doesn't handle protected methods.
     * @param iconUrl
     */
    @Override
    public boolean setIcon(String iconUrl)
    {
        // try the standard method:
        if( super.setIcon(iconUrl)==false )
        {
            // if standard method failed, try this
            String fullpath = instanciator.scriptFile.getAbsolutePath();
            String filename = instanciator.scriptFile.getName();
            iconUrl = fullpath.replace(filename, iconUrl);
            return super.setIcon(iconUrl);
        }
        return false;
    }


    void install(PythonRunner inst)
    {
        instanciator = inst;
    }

}
