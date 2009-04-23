/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.binding.Binding;
import modbuspal.generator.Generator;


/**
 * The ClassInstanciator is an implementation of the
 * "Instanciator" interface which is designed to
 * instanciate the built-in classes.
 * @author nnovic
 */
public class ClassInstanciator
implements Instanciator
{
    private Class clazz;

    /**
     * Constructor a the ClassInstanciator.
     * @param cl the class definition of the generator that will be instanciated.
     */
    public ClassInstanciator(Class cl)
    {
        clazz = cl;
    }

    @Override
    public String getClassName()
    {
        return clazz.getSimpleName();
    }

    @Override
    public Generator newGenerator()
    {
        try
        {
            return (Generator)clazz.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(ClassInstanciator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Binding newBinding()
    {
        try
        {
            return (Binding)clazz.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(ClassInstanciator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void save(OutputStream out, File projectFile)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
