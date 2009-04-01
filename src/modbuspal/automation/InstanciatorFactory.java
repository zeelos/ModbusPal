/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import modbuspal.automation.linear.LinearGenerator;
import modbuspal.automation.random.RandomGenerator;
import modbuspal.main.FileTools;
import modbuspal.main.ModbusPal;
import modbuspal.main.XMLTools;
import modbuspal.script.ScriptInstanciator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author nnovic
 */
public class InstanciatorFactory
{


    private static final String REGISTRY_KEY = ModbusPal.BASE_REGISTRY_KEY + "/instanciators";
    private static ArrayList<InstanciatorFactoryListener> listeners = new ArrayList<InstanciatorFactoryListener>();

    /**
     * List of predefined generators:
     */
    private static final ClassInstanciator classInstanciators[] =
    {
        new ClassInstanciator(LinearGenerator.class),
        new ClassInstanciator(RandomGenerator.class)
    };

    /**
     * List of generators that are dynimacally inserted by the user.
     */
    private static ArrayList<ScriptInstanciator> scriptInstanciators = new ArrayList<ScriptInstanciator>();



    public static void loadInstanciators(Document doc, File projectFile)
    {
        NodeList list = doc.getElementsByTagName("instanciator");
        if( list!=null )
        {
            for( int i=0; i<list.getLength(); i++ )
            {
                try
                {
                    Node genNode = list.item(i);
                    loadInstanciator(genNode, projectFile);
                }

                catch (FileNotFoundException ex)
                {
                    Logger.getLogger(InstanciatorFactory.class.getName()).log(Level.SEVERE, null, ex);
                }                catch (IOException ex)
                {
                    Logger.getLogger(InstanciatorFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }



    private static void loadInstanciator(Node node, File projectFile)
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
        ScriptInstanciator gen = ScriptInstanciator.create(scriptFile);

        // add the handler to the factory:
        InstanciatorFactory.add(gen);

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
        ScriptInstanciator gen = ScriptInstanciator.create(scriptFile);

        // add the handler to the factory:
        InstanciatorFactory.add(gen);
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
        GeneratorInstanciator gi = findInstanciator(className);
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
        for( ScriptInstanciator gi:scriptInstanciators)
        {
            gi.save(out,projectFile);
        }
    }

    static void add(ScriptInstanciator gi)
    {
        if( scriptInstanciators.contains(gi)==false )
        {
            scriptInstanciators.add(gi);
            notifyGeneratorInstanciatorAdded(gi);
        }
    }

    
    public static void remove(ScriptInstanciator si)
    {
        if( scriptInstanciators.contains(si)==true )
        {
            scriptInstanciators.remove(si);
            notifyGeneratorInstanciatorRemoved(si);
        }
    }


    public static void addGeneratorFactoryListener(InstanciatorFactoryListener l)
    {
        if( listeners.contains(l)==false )
            listeners.add(l);
    }

    public static void removeGeneratorFactoryListener(InstanciatorFactoryListener l)
    {
        if( listeners.contains(l)==true )
            listeners.remove(l);
    }    

    private static GeneratorInstanciator findInstanciator(String className)
    {
        // look into the list of predefined classes
        for( int i=0; i<classInstanciators.length; i++ )
        {
            GeneratorInstanciator gi = classInstanciators[i];
            if( gi.getClassName().compareTo(className)==0 )
            {
                return gi;
            }
        }

        // look into the list of additional classes
        for( GeneratorInstanciator gi:scriptInstanciators )
        {
            if( gi.getClassName().compareTo(className)==0 )
            {
                return gi;
            }
        }

        return null;
    }

    static String[] getList()
    {
        // prepare list
        int size = classInstanciators.length + scriptInstanciators.size();
        String list[] = new String[size];
        int index=0;

        // fill list with the predefined generators
        for(index=0; index<classInstanciators.length; index++)
        {
            list[index]=classInstanciators[index].getClassName();
        }

        for(GeneratorInstanciator gi:scriptInstanciators)
        {
            list[index++] = gi.getClassName();
        }

        // return the list
        return list;
    }


    public static File chooseScriptFile(Component parent)
    {
        // get last used directory
        Preferences prefs = Preferences.userRoot();
        Preferences appPrefs = prefs.node(REGISTRY_KEY);
        String prev_dir = appPrefs.get("prev_dir", null);

        // newInstance the dialog
        JFileChooser fileChooser = new JFileChooser();

        // setup current directory if available
        if( prev_dir != null )
        {
            File cwd = new File(prev_dir);
            if( (cwd.isDirectory()==true) && (cwd.exists()==true) )
            {
                fileChooser.setCurrentDirectory(cwd);
            }
        }

        // newInstance a Python/Jython extension filter
        FileNameExtensionFilter pythonFilter = new FileNameExtensionFilter("Python file", "py");
        fileChooser.setFileFilter(pythonFilter);

        // display file chooser
        int choice = fileChooser.showDialog(parent, "Add");
        if( choice == JFileChooser.APPROVE_OPTION)
        {
            // get the directory that has been chosen
            File chosen = fileChooser.getCurrentDirectory();
            appPrefs.put("prev_dir", chosen.getPath());
            try {
                appPrefs.flush();
            } catch (BackingStoreException ex) {
                Logger.getLogger(InstanciatorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    private static void notifyGeneratorInstanciatorAdded(GeneratorInstanciator def)
    {
        for(InstanciatorFactoryListener l:listeners)
            l.generatorInstanciatorAdded(def);
    }

    private static void notifyGeneratorInstanciatorRemoved(GeneratorInstanciator def)
    {
        for(InstanciatorFactoryListener l:listeners)
            l.generatorInstanciatorRemoved(def);
    }
}
