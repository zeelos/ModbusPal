/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The ClassInstanciator is an implementation of the
 * "GeneratorInstanciator" interface which is designed to
 * instanciate the predefined generators, like LinearGenerator
 * and RandomGenerator.
 * @author nnovic
 */
class ClassInstanciator
implements GeneratorInstanciator
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
}
