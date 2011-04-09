/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

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
public class FunctionFactory
extends InstanciatorManager
{
    public FunctionFactory()
    {

    }
    
    public static String makeInstanceName(ModbusSlavePduProcessor mspp)
    {
        return mspp.getClassName() + '@' + Integer.toHexString(mspp.hashCode());
    }

    public ModbusSlavePduProcessor newFunction(String classname)
    throws InstantiationException, IllegalAccessException
    {
        Instanciator is = getInstanciator(classname);
        return is.newFunction();
    }


    @Override
    public void load(Document doc, File projectFile)
    {
        NodeList list = doc.getElementsByTagName("functions");
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
        
        String openTag = "<functions>\r\n";
        out.write( openTag.getBytes() );

        for( Instanciator gi:scriptedInstanciators)
        {
            gi.save(out,projectFile);
        }

        String closeTag = "</functions>\r\n";
        out.write( closeTag.getBytes() );
    }

}
