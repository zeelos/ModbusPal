/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator;

import modbuspal.automation.*;
import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The ClassInstanciator is an implementation of the
 * "Instanciator" interface which is designed to
 * instanciate the predefined generators, like LinearGenerator
 * and RandomGenerator.
 * @author nnovic
 */
class ClassInstanciator
implements Instanciator
{
    private Class clazz;

    /**
     * Constructor a the ClassInstanciator.
     * @param cl the class definition of the generator that will be instanciated.
     */
    ClassInstanciator(Class cl)
    {
        clazz = cl;
    }

    @Override
    public String getClassName()
    {
        return clazz.getSimpleName();
    }

    @Override
    public Generator newInstance()
    {
        try
        {
            return (Generator) clazz.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(GeneratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void save(OutputStream out, File projectFile)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
