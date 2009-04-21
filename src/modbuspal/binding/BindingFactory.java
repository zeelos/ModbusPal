/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

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
public class BindingFactory
extends InstanciatorManager<Binding>
{
    private static BindingFactory factory = new BindingFactory();


    public static BindingFactory getFactory()
    {
        return factory;
    }

    
    public static Binding newBinding(String classname)
    throws InstantiationException, IllegalAccessException
    {
        return factory.newInstance(classname);
    }

    /**
     * List of predefined bindings:
     */
    private final ClassInstanciator classInstanciators[] =
    {
        new ClassInstanciator<Binding>(modbuspal.binding.Binding_SINT32.class),
        new ClassInstanciator<Binding>(modbuspal.binding.Binding_FLOAT32.class)
    };



    @Override
    public void load(Document doc, File projectFile)
    {
        NodeList list = doc.getElementsByTagName("bindings");
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
        
        String openTag = "<bindings>\r\n";
        out.write( openTag.getBytes() );

        for( Instanciator gi:scriptedInstanciators)
        {
            gi.save(out,projectFile);
        }

        String closeTag = "</bindings>\r\n";
        out.write( closeTag.getBytes() );
    }

    @Override
    protected int getClassInstanciatorsCount()
    {
        return classInstanciators.length;
    }

    @Override
    protected Instanciator<Binding> getClassInstanciator(int index)
    {
        return classInstanciators[index];
    }

}
