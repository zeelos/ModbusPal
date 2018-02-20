/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.script;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusPalXML;
import modbuspal.toolkit.FileTools;
import modbuspal.toolkit.GUITools;
import modbuspal.toolkit.XMLTools;
import org.python.util.PythonInterpreter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * an object encapsulating the script file
 * @author nnovic
 */
public class ScriptRunner
implements ModbusPalXML
{
    public static final int SCRIPT_TYPE_ANY = -1;
    public static final int SCRIPT_TYPE_ON_DEMAND = 0;
    public static final int SCRIPT_TYPE_AFTER_INIT = 1;
    public static final int SCRIPT_TYPE_BEFORE_INIT = 2;
    public static final int SCRIPT_TYPE_OLD_BINDINGS = 3;
    public static final int SCRIPT_TYPE_OLD_GENERATORS = 4;

    private static final String SCRIPT_UPDATED_COMMENT ="#---Added by MODBUSPAL, please do not remove---";
    
    /**
     * Creates a ScriptRunner instance by analyzing the settings provided in
     * the DOM node. If the script type cannot be determined from
     * the settings, "ON DEMAND" is used as a default value.
     * @param node the DOM node from which the settings must be loaded
     * @param mpp the modbuspal project that is currently running
     * @param projectFile the file associated with the modbuspal project, necessary
     * to resolve relative paths.
     * @param promptUser if true, when the script cannot be found then a dialog
     * will appear to let the user locate the missing script.
     * @return A ScriptRunner object encapsulating the script.
     */    
    public static ScriptRunner create(Node node, ModbusPalProject mpp, File projectFile, boolean promptUser)
    {
        return create(node,mpp,projectFile,promptUser,SCRIPT_TYPE_ON_DEMAND);
    }
    
    
    static int stringToType(String s, int d)
    {
        if( s.compareToIgnoreCase(XML_SCRIPT_TYPE_ONDEMAND)==0)
        {
            return SCRIPT_TYPE_ON_DEMAND;
        }
        else if( s.compareToIgnoreCase(XML_SCRIPT_TYPE_AFTERINIT)==0 )
        {
            return SCRIPT_TYPE_AFTER_INIT;
        }
        else if( s.compareToIgnoreCase(XML_SCRIPT_TYPE_BEFOREINIT)==0 )
        {
            return SCRIPT_TYPE_BEFORE_INIT;
        }
       return d;
    }

    static String typeToString(int t)
    {
        switch(t)
        {
            case SCRIPT_TYPE_ON_DEMAND: return XML_SCRIPT_TYPE_ONDEMAND;
            case SCRIPT_TYPE_AFTER_INIT: return XML_SCRIPT_TYPE_AFTERINIT;
            case SCRIPT_TYPE_BEFORE_INIT: return XML_SCRIPT_TYPE_BEFOREINIT;
            default:
                return null;
        }
    }

    /**
     * Creates a ScriptRunner instance by analyzing the settings provided in
     * the DOM node.
     * @param node the DOM node from which the settings must be loaded
     * @param mpp the modbuspal project that is currently running
     * @param projectFile the file associated with the modbuspal project, necessary
     * to resolve relative paths.
     * @param promptUser if true, when the script cannot be found then a dialog
     * will appear to let the user locate the missing script.
     * @param assumedScriptType if the script type cannot be determined from
     * the settings, that this type will be used as a default value.
     * @return A ScriptRunner object encapsulating the script.
     */
    public static ScriptRunner create(Node node, ModbusPalProject mpp, File projectFile, boolean promptUser, int assumedScriptType)
    {
        File scriptFile = null;

        //------------------------
        // find "rel"
        //------------------------

        Node rel = XMLTools.findChild(node, XML_FILE_RELATIVE_PATH_TAG);
        if( rel != null )
        {
            // try to load file from relative path
            String relativePath = rel.getTextContent();
            String absolutePath = FileTools.makeAbsolute(projectFile, relativePath);
            scriptFile = new File(absolutePath);
            if( scriptFile.exists()==false )
            {
                scriptFile=null;
            }
        }

        if( scriptFile==null )
        {
            //-----------------
            // find "abs"
            //-----------------

            Node abs = XMLTools.findChild(node, XML_FILE_ABSOLUTE_PATH_TAG);
            if( abs == null )
            {
                throw new RuntimeException("malformed input");
            }

            String path = abs.getTextContent();
            scriptFile = new File(path);

        }

        //------------------
        // File not found
        //------------------

        if (scriptFile.exists()==false )
        {
            System.out.println("No file found for script "+scriptFile.getPath());

            // IF NO FILE FOUND, PROMPT USER:
            if(promptUser==true)
            {
                // create error message box with 2 buttons:
                scriptFile = GUITools.promptUserFileNotFound(null, scriptFile);
            }

        }

        //-------------------------
        // get script attributes
        //-------------------------

        int sType = assumedScriptType;
        NamedNodeMap attributes = node.getAttributes();
        if(attributes!=null)
        {
            Node typeNode = attributes.getNamedItem(XML_SCRIPT_TYPE_ATTRIBUTE);
            if(typeNode!=null)
            {
                String type = typeNode.getNodeValue();
                sType = stringToType(type, sType);
            }
        }

        ScriptRunner runner = create(mpp, scriptFile, sType);
        return runner;

    }

    /**
     * Encapsulates the specified script file into a ScriptRunner object.
     * The resulting ScriptRunner is not associated with the current
     * ModbusPal project.
     * @param scriptFile the script file to load
     * @return an object encapsulating the script file
     */
    public static ScriptRunner create(File scriptFile)
    {
        return create(null, scriptFile, SCRIPT_TYPE_ON_DEMAND);
    }
    
    /**
     * Creates a ScriptRunner object to encapsulate the specified script file.
     * The ScriptRunner is associated with the specified mobuspal project,
     * which means that it will be listed in the Script Manager dialog.
     * @param mpp the modbuspal project
     * @param file the script file
     * @param type the script type. one of SCRIPT_TYPE_ON_DEMAND,
     * SCRIPT_TYPE_BEFORE_INIT or SCRIPT_TYPE_AFTER_INIT.
     * @return an object encapsulating the script file
     */
    public static ScriptRunner create(ModbusPalProject mpp, File file, int type)
    {
        String extension = FileTools.getExtension(file);
        if( extension==null)
        {
            return null;
        }

        if( extension.compareToIgnoreCase("py")==0 )
        {
            return new ScriptRunner(mpp, file, type);
        }

        return null;
    }


    private final ModbusPalProject modbusPalProject;
    private final File scriptFile;
    private int scriptType;

    
    /**
     * Creates a new ScriptRunner instance, associated with the specified
     * modbuspal project, script file, and script type.
     * @param mpp the modbuspal project
     * @param file the script file
     * @param type the script type
     */
    ScriptRunner(ModbusPalProject mpp, File file, int type)
    {
        modbusPalProject = mpp;
        scriptFile = file;
        scriptType = type;
    }


    /**
     * Saves the settings of this script into the output stream, in XML format.
     * @param out output file where to write into
     * @param projectFile the project file associated with the output stream. It
     * is necessary so that the path of this script can be saved relatively to
     * the path of the project file.
     * @throws IOException 
     */
    public void save(OutputStream out, File projectFile)
    throws IOException
    {
        // create open tag
        StringBuilder openTag = new StringBuilder("<script");
        String type = typeToString(scriptType);
        if(type!=null)
        {
            openTag.append(" type=\"").append(type).append("\"");
        }
        openTag.append(">\r\n");
        out.write(openTag.toString().getBytes());

        // write absolute file projectPath
        saveAbs(out);

        // write relative file projectPath
        if( projectFile!=null)
        {
            saveRel(out,projectFile);
        }

        // create close tag
        String closeTag = "</script>\r\n";
        out.write(closeTag.getBytes());
    }

    private void saveAbs(OutputStream out)
    throws IOException
    {
        // create open tag
        String openTag = "<abs>";
        out.write(openTag.getBytes());

        // write abs file projectPath
        String path = scriptFile.getPath();
        out.write(path.getBytes());

        // create close tag
        String closeTag = "</abs>\r\n";
        out.write(closeTag.getBytes());
    }

    private void saveRel(OutputStream out, File projectFile)
    throws IOException
    {
        String rel = FileTools.makeRelative(projectFile, scriptFile);

        if( rel != null )
        {
            // create open tag
            String openTag = "<rel>";
            out.write(openTag.getBytes());

            out.write( rel.getBytes() );

            // create close tag
            String closeTag = "</rel>\r\n";
            out.write(closeTag.getBytes());
        }
    }

    private void initEnvironment(PythonInterpreter pi)
    {
        // init vars for script file
        pi.set("mbp_script_path", scriptFile.getPath() );
        pi.set("mbp_script_directory", scriptFile.getParent() );
        pi.set("mbp_script_file", scriptFile );

        // init vars for modbuspal project
        pi.set("ModbusPal", modbusPalProject);
    }

    /**
     * Executes this script.
     */
    public void execute()
    {
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(scriptFile);
            PythonInterpreter interp = new PythonInterpreter();
            initEnvironment(interp);
            interp.execfile(in);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        if( in != null )
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    /**
     * Gets the file extension of the script file, which serves to identify the
     * language in which it is written. Currently, only Python is supported.
     * @return "py", the standard extension for Python scripts.
     */
    public String getFileExtension()
    {
        return "py";
    }

    /**
     * Gets the name of the script. It actually is the filename without the
     * file extension. 
     * @return The name of the script?.
     */
    public String getName()
    {
        String filename = scriptFile.getName();
        if( filename!=null )
        {
            int index = filename.lastIndexOf('.');
            if( index!=-1 )
            {
                String ext = filename.substring(index);
                if( ext.compareToIgnoreCase("."+ getFileExtension())==0 )
                {
                    return filename.substring(0,index);
                }
            }
        }
        return filename;
    }


    /**
     * Returns the full pathname of the script file.
     * @return the pathname of the script file.
     */
    public String getPath()
    {
        return scriptFile.getPath();
    }

    /**
     * Requests that the script is interrupted if its currently running.
     * Not implemented yet, this method does nothing.
     */
    public void interrupt()
    {
        
    }

    /**
     * Gets the current type of the script.
     * @return the current type of the script
     */
    public int getType()
    {
        return scriptType;
    }

    /**
     * Defines the type of the script. One of SCRIPT_TYPE_ON_DEMAND,
     * SCRIPT_TYPE_AFTER_INIT or SCRIPT_TYPE_BEFORE_INIT
     * @param type the new script type.
     */
    public void setType(int type)
    {
        scriptType = type;
    }


    /**
     * Returns the file from which this script was loaded.
     * @return the script file
     */
    public File getScriptFile()
    {
        return scriptFile;
    }

    /**
     * ModbusPal will call this method when loading a script this is referenced
     * in the old way as a "binding script". The script will be updated: the 
     * binding class will be instanciated and then registered with the
     * ModbusPalProject#addBindingInstantiator() method.
     */
    public void updateForOldBindings()
    {
        try
        {
            // assume that the class name if the name as
            // the name of the runner, as it was specified
            String classname = getName();
            // open the script file and check is it has been updated already:
            if (FileTools.containsLine(scriptFile, SCRIPT_UPDATED_COMMENT) == false)
            {
                StringBuilder upgrade = new StringBuilder();
                upgrade.append("\r\n\r\n").append(SCRIPT_UPDATED_COMMENT).append("\r\n");
                upgrade.append(classname).append("Instance = ").append(classname).append("();\r\n");
                upgrade.append("ModbusPal.addBindingInstantiator(\"").append(classname).append("\",").append(classname).append("Instance);\r\n\r\n");
                FileTools.append(scriptFile,upgrade.toString());
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ModbusPal will call this method when loading a script this is referenced
     * in the old way as a "generator script". The script will be updated: the 
     * generator class will be instanciated and then registered with the
     * ModbusPalProject#addGeneratorInstantiator() method.
     */
    public void updateForOldGenerators()
    {
        try
        {
            // assume that the class name if the name as
            // the name of the runner, as it was specified
            String classname = getName();
            // open the script file and check is it has been updated already:
            if (FileTools.containsLine(scriptFile, SCRIPT_UPDATED_COMMENT) == false)
            {
                StringBuilder upgrade = new StringBuilder();
                upgrade.append("\r\n\r\n").append(SCRIPT_UPDATED_COMMENT).append("\r\n");
                upgrade.append(classname).append("Instance = ").append(classname).append("();\r\n");
                upgrade.append("ModbusPal.addGeneratorInstantiator(\"").append(classname).append("\",").append(classname).append("Instance);\r\n\r\n");
                FileTools.append(scriptFile,upgrade.toString());
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ScriptRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
