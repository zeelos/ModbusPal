/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator;

import modbuspal.instanciator.Instanciator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import modbuspal.instanciator.ClassInstanciator;
import modbuspal.instanciator.InstanciatorManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 *
 * @author nnovic
 */
public class GeneratorFactory
extends InstanciatorManager
{
    private static GeneratorFactory factory = new GeneratorFactory();


    public static GeneratorFactory getFactory()
    {
        return factory;
    }

    
    public static Generator newGenerator(String classname)
    throws InstantiationException, IllegalAccessException
    {
        Instanciator is = factory.getInstanciator(classname);
        return is.newGenerator();
    }

    /**
     * List of predefined generators:
     */
    private final ClassInstanciator classInstanciators[] =
    {
        new ClassInstanciator(modbuspal.generator.linear.LinearGenerator.class),
        new ClassInstanciator(modbuspal.generator.random.RandomGenerator.class)
    };



    @Override
    public void load(Document doc, File projectFile)
    {
        NodeList list = doc.getElementsByTagName("generators");
        loadInstanciators(list,projectFile);
    }



    @Override
    public void save(OutputStream out, File projectFile)
    throws IOException
    {
        if( scriptedInstanciators.isEmpty() )
        {
            return;
        }
        
        String openTag = "<generators>\r\n";
        out.write( openTag.getBytes() );

        for( Instanciator gi:scriptedInstanciators)
        {
            gi.save(out,projectFile);
        }

        String closeTag = "</generators>\r\n";
        out.write( closeTag.getBytes() );
    }

    @Override
    protected int getClassInstanciatorsCount()
    {
        return classInstanciators.length;
    }

    @Override
    protected Instanciator getClassInstanciator(int index)
    {
        return classInstanciators[index];
    }
}
