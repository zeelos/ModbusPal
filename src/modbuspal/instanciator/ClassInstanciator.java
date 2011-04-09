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
import modbuspal.slave.ModbusSlavePduProcessor;


/**
 * The ClassInstanciator is an implementation of the
 * "Instanciator" interface which is designed to
 * instanciate the built-in classes.
 * @author nnovic
 */
public class ClassInstanciator
implements Instanciator
{
    private final Class clazz;
    private final String clazzName;

    /**
     * Constructor a the ClassInstanciator.
     * @param cl the class definition of the generator that will be instanciated.
     */
    public ClassInstanciator(Class cl)
    {
        clazz = cl;
        clazzName = null;
    }

    /**
     * Constructor a the ClassInstanciator.
     * @param cl the class definition of the generator that will be instanciated.
     */
    public ClassInstanciator(Class cl, String name)
    {
        clazz = cl;
        clazzName = name;
    }
    @Override
    public String getClassName()
    {
        if(clazzName!=null)
        {
            return clazzName;
        }
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
            Object obj = clazz.newInstance();
            return (Binding)obj;
        }
        catch (Exception ex)
        {
            Logger.getLogger(ClassInstanciator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public ModbusSlavePduProcessor newFunction()
    {
        try
        {
            Object obj = clazz.newInstance();
            return (ModbusSlavePduProcessor)obj;
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

    @Override
    public String getPath()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
