/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator;

import modbuspal.generator.ClassInstanciator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.generator.linear.LinearGenerator;
import modbuspal.generator.random.RandomGenerator;
import modbuspal.main.FileTools;
import modbuspal.main.XMLTools;
import modbuspal.script.ScriptRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author nnovic
 */
public class GeneratorFactory
{
    private static ArrayList<InstanciatorListener> listeners = new ArrayList<InstanciatorListener>();

    /**
     * List of predefined generators:
     */
    private static final ClassInstanciator classInstanciators[] =
    {
        new ClassInstanciator(LinearGenerator.class),
        new ClassInstanciator(RandomGenerator.class)
    };

    /**scriptedInstanciators
     * List of generators that are dynimacally inserted by the user.
     */
    private static ArrayList<Instanciator> scriptedInstanciators = new ArrayList<Instanciator>();



    public static void loadInstanciators(Document doc, File projectFile)
    {
        NodeList list = doc.getElementsByTagName("instanciators");
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                Node instNode = list.item(i);
                loadScripts(instNode, projectFile);
            }
        }
    }

    private static void loadScripts(Node node, File projectFile)
    {
        NodeList list = node.getChildNodes();
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                Node scriptNode = list.item(i);
                if( scriptNode.getNodeName().compareTo("script")==0 )
                {
                    try {
                        loadScript(scriptNode, projectFile);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(GeneratorFactory.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(GeneratorFactory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }


    private static void loadScript(Node node, File projectFile)
    throws FileNotFoundException, IOException
    {
        boolean ok = false;

        // get "rel" node and try to get the file by using a relative
        // path (relative to projectFile)
        Node rel = XMLTools.findChild(node, "rel");
        if( rel != null )
        {
            ok = loadRel(rel, projectFile);
        }

        if( ok == true )
        {
            return;
        }

        // get "abs" node and try to get the file using the absolute path
        Node abs = XMLTools.findChild(node, "abs");
        if( abs != null )
        {
            loadAbs(abs);
        }
    }
    
    private static boolean loadRel(Node node, File projectFile)
    throws FileNotFoundException, IOException
    {
        // extract filename from xml file
        String filename = node.getTextContent();

        // create file object
        File scriptFile = new File(filename);

        // make it absolute
        String abs = FileTools.makeAbsolute(projectFile, scriptFile);
        if( abs==null )
        {
            return false;
        }

        scriptFile = new File(abs);
        if( scriptFile.exists()==false )
        {
            return false;
        }

        // newInstance a scripted generator handler
        ScriptRunner gen = ScriptRunner.create(scriptFile);

        // add the handler to the factory:
        GeneratorFactory.add(gen);

        return true;
    }


    private static void loadAbs(Node node)
    throws FileNotFoundException, IOException
    {
        // extract filename from xml file
        String filename = node.getTextContent();

        // create file object
        File scriptFile = new File(filename);

        // newInstance a scripted generator handler
        ScriptRunner gen = ScriptRunner.create(scriptFile);

        // add the handler to the factory:
        GeneratorFactory.add(gen);
    }


    

    /**
     * Creates a generator instance by specifying the name of the generator's class.
     * @param className
     * @return
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static Generator newInstance(String className)
    throws InstantiationException, IllegalAccessException
    {
        Instanciator gi = findInstanciator(className);
        if( gi!=null )
        {
            Generator gen = gi.newInstance();
            return gen;
        }
        return null;
    }



    /**
     * Check if the specified generator exists in the factory?.
     * @param name
     * @return
     */
    public static boolean exists(String className)
    {
        return ( findInstanciator(className)!=null );
    }

    public static void saveInstanciators(OutputStream out, File projectFile)
    throws IOException
    {
        if( scriptedInstanciators.isEmpty() )
        {
            return;
        }
        
        String openTag = "<instanciators>\r\n";
        out.write( openTag.getBytes() );

        for( Instanciator gi:scriptedInstanciators)
        {
            gi.save(out,projectFile);
        }

        String closeTag = "</instanciators>\r\n";
        out.write( closeTag.getBytes() );
    }

    public static void add(Instanciator gi)
    {
        if( scriptedInstanciators.contains(gi)==false )
        {
            scriptedInstanciators.add(gi);
            notifyGeneratorInstanciatorAdded(gi);
        }
    }

    
    public static void remove(Instanciator si)
    {
        if( scriptedInstanciators.contains(si)==true )
        {
            scriptedInstanciators.remove(si);
            notifyGeneratorInstanciatorRemoved(si);
        }
    }


    public static void addInstanciatorListener(InstanciatorListener l)
    {
        if( listeners.contains(l)==false )
            listeners.add(l);
    }

    public static void removeInstanciatorListener(InstanciatorListener l)
    {
        if( listeners.contains(l)==true )
            listeners.remove(l);
    }    

    private static Instanciator findInstanciator(String className)
    {
        // look into the list of predefined classes
        for( int i=0; i<classInstanciators.length; i++ )
        {
            Instanciator gi = classInstanciators[i];
            if( gi.getClassName().compareTo(className)==0 )
            {
                return gi;
            }
        }

        // look into the list of additional classes
        for( Instanciator gi:scriptedInstanciators )
        {
            if( gi.getClassName().compareTo(className)==0 )
            {
                return gi;
            }
        }

        return null;
    }

    public static String[] getList()
    {
        // prepare list
        int size = classInstanciators.length + scriptedInstanciators.size();
        String list[] = new String[size];
        int index=0;

        // fill list with the predefined generators
        for(index=0; index<classInstanciators.length; index++)
        {
            list[index]=classInstanciators[index].getClassName();
        }

        for(Instanciator gi:scriptedInstanciators)
        {
            list[index++] = gi.getClassName();
        }

        // return the list
        return list;
    }



    private static void notifyGeneratorInstanciatorAdded(Instanciator def)
    {
        for(InstanciatorListener l:listeners)
            l.instanciatorAdded(def);
    }

    private static void notifyGeneratorInstanciatorRemoved(Instanciator def)
    {
        for(InstanciatorListener l:listeners)
            l.instanciatorRemoved(def);
    }
}
