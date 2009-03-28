/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.python;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.automation.Generator;
import org.w3c.dom.NodeList;

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
