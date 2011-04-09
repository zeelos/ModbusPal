/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

import modbuspal.instanciator.Instanciator;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import modbuspal.instanciator.InstanciatorManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 *
 * @author nnovic
 */
public class BindingFactory
extends InstanciatorManager
{

    public BindingFactory()
    {
        add( modbuspal.binding.Binding_SINT32.class );
        add( modbuspal.binding.Binding_FLOAT32.class);
    }

    public Binding newBinding(String classname)
    throws InstantiationException, IllegalAccessException
    {
        Instanciator is = getInstanciator(classname);
        return is.newBinding();
    }


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

}
