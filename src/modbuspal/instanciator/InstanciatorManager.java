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
import modbuspal.toolkit.FileTools;
import modbuspal.toolkit.XMLTools;
import modbuspal.script.ScriptRunner;
import modbuspal.toolkit.GUITools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author nnovic
 */
public abstract class InstanciatorManager
{
    /**
     * List of listeners that are interested in receiving events from this
     * manager.
     */
    protected ArrayList<InstanciatorListener> listeners = new ArrayList<InstanciatorListener>();

    /**
     * List of generators that are dynimacally inserted by the user.
     */
    protected ArrayList<Instanciator> scriptedInstanciators = new ArrayList<Instanciator>();


    public abstract void load(Document doc, File projectFile);

    
    protected void loadInstanciators(NodeList list, File projectFile)
    {
        //NodeList list = doc.getElementsByTagName("instanciators");
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                Node instNode = list.item(i);
                loadScripts(instNode, projectFile, true);
            }
        }
    }

    private void loadScripts(Node node, File projectFile, boolean promptUser)
    {
        NodeList list = node.getChildNodes();
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                Node scriptNode = list.item(i);
                if( scriptNode.getNodeName().compareTo("script")==0 )
                {
                    try
                    {
                        loadScript(scriptNode, projectFile, promptUser);
                    } 
                    catch (FileNotFoundException ex)
                    {
                        Logger.getLogger(InstanciatorManager.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                    catch (IOException ex)
                    {
                        Logger.getLogger(InstanciatorManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }


    private boolean loadScript(Node node, File projectFile, boolean promptUser)
    throws FileNotFoundException, IOException
    {
        // get "rel" node and try to get the file by using a relative
        // path (relative to projectFile)
        Node rel = XMLTools.findChild(node, "rel");
        if( rel != null )
        {
            if( loadRel(rel, projectFile)==true )
            {
                return true;
            }
        }

        // get "abs" node and try to get the file using the absolute path
        Node abs = XMLTools.findChild(node, "abs");
        if( abs == null )
        {
            throw new RuntimeException("malformed input");
        }

        // extract filename from xml file
        String filename = node.getTextContent();

        // create file object
        File scriptFile = new File(filename);

        // IF NO FILE FOUND, PROMPT USER:
        if( (scriptFile.exists()==false) && (promptUser==true) )
        {
            System.out.println("No file found for script "+scriptFile.getPath());
            scriptFile = GUITools.promptUserFileNotFound(null, scriptFile);
        }

        if( (scriptFile==null) || (scriptFile.exists()==false) )
        {
            return false;
        }

        // newInstance a scripted generator handler
        ScriptRunner sr = ScriptRunner.create(scriptFile);

        // add the handler to the factory:
        add(sr);

        return true;
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

/*
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
*/


 




    /**
     * Check if the specified instanciator exists in the factory.
     * @param className name of the instanciator to find.
     * @return true if the instanciator exists, false otherwise.
     */
    public boolean exists(String className)
    {
        return ( getInstanciator(className)!=null );
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
        Instanciator list[] = new Instanciator[0];
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

    protected abstract Instanciator getClassInstanciator(int index);


    public Instanciator getInstanciator(String className)
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
            l.instanciatorAdded(this, def);
    }

    protected void notifyInstanciatorRemoved(Instanciator def)
    {
        for(InstanciatorListener l:listeners)
            l.instanciatorRemoved(this, def);
    }
}
