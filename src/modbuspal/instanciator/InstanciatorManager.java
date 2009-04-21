/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.instanciator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public abstract class InstanciatorManager<T>
{
    protected ArrayList<InstanciatorListener> listeners = new ArrayList<InstanciatorListener>();

    /**
     * List of generators that are dynimacally inserted by the user.
     */
    protected ArrayList<Instanciator<T>> scriptedInstanciators = new ArrayList<Instanciator<T>>();


    public abstract void load(Document doc, File projectFile);

    
    protected void loadInstanciators(NodeList list, File projectFile)
    {
        //NodeList list = doc.getElementsByTagName("instanciators");
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                Node instNode = list.item(i);
                loadScripts(instNode, projectFile);
            }
        }
    }

    private void loadScripts(Node node, File projectFile)
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
                        Logger.getLogger(InstanciatorManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(InstanciatorManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }


    private void loadScript(Node node, File projectFile)
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
    
    private boolean loadRel(Node node, File projectFile)
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
        ScriptRunner sr = ScriptRunner.create(scriptFile);

        // add the handler to the factory:
        add(sr);

        return true;
    }


    private void loadAbs(Node node)
    throws FileNotFoundException, IOException
    {
        // extract filename from xml file
        String filename = node.getTextContent();

        // create file object
        File scriptFile = new File(filename);

        // newInstance a scripted generator handler
        ScriptRunner sr = ScriptRunner.create(scriptFile);

        // add the handler to the factory:
        add(sr);
    }


    

    /**
     * Creates a generator instance by specifying the name of the generator's class.
     * @param className
     * @return
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public T newInstance(String className)
    throws InstantiationException, IllegalAccessException
    {
        Instanciator<T> gi = findInstanciator(className);
        if( gi!=null )
        {
            T obj = gi.newInstance();
            return obj;
        }
        return null;
    }



    /**
     * Check if the specified generator exists in the factory?.
     * @param name
     * @return
     */
    public boolean exists(String className)
    {
        return ( findInstanciator(className)!=null );
    }


    public abstract void save(OutputStream out, File projectFile)
    throws IOException;
    /*{
        if( scriptedInstanciators.isEmpty() )
        {
            return;
        }
        
        //String openTag = "<instanciators>\r\n";
        //out.write( openTag.getBytes() );

        for( Instanciator gi:scriptedInstanciators)
        {
            gi.save(out,projectFile);
        }

        //String closeTag = "</instanciators>\r\n";
        //out.write( closeTag.getBytes() );
    }*/

    public void add(Instanciator gi)
    {
        if( scriptedInstanciators.contains(gi)==false )
        {
            scriptedInstanciators.add(gi);
            notifyInstanciatorAdded(gi);
        }
    }

    
    public void remove(Instanciator si)
    {
        if( scriptedInstanciators.contains(si)==true )
        {
            scriptedInstanciators.remove(si);
            notifyInstanciatorRemoved(si);
        }
    }

    public void clear()
    {
        Instanciator<T> list[] = new Instanciator[0];
        list=scriptedInstanciators.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            remove( list[i] );
        }
    }



    public void addInstanciatorListener(InstanciatorListener l)
    {
        if( listeners.contains(l)==false )
            listeners.add(l);
    }

    public void removeInstanciatorListener(InstanciatorListener l)
    {
        if( listeners.contains(l)==true )
            listeners.remove(l);
    }    



    protected abstract int getClassInstanciatorsCount();

    protected abstract Instanciator<T> getClassInstanciator(int index);


    private Instanciator<T> findInstanciator(String className)
    {
        // look into the list of predefined classes
        for( int i=0; i<getClassInstanciatorsCount(); i++ )
        {
            Instanciator gi = getClassInstanciator(i);
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

    public String[] getList()
    {
        // prepare list
        int size = getClassInstanciatorsCount() + scriptedInstanciators.size();
        String list[] = new String[size];
        int index=0;

        // fill list with the predefined generators
        for(index=0; index<getClassInstanciatorsCount(); index++)
        {
            list[index]=getClassInstanciator(index).getClassName();
        }

        for(Instanciator gi:scriptedInstanciators)
        {
            list[index++] = gi.getClassName();
        }

        // return the list
        return list;
    }



    protected void notifyInstanciatorAdded(Instanciator def)
    {
        for(InstanciatorListener l:listeners)
            l.instanciatorAdded(def);
    }

    protected void notifyInstanciatorRemoved(Instanciator def)
    {
        for(InstanciatorListener l:listeners)
            l.instanciatorRemoved(def);
    }
}
