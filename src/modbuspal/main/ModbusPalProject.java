/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.Automation;
import modbuspal.automation.NullAutomation;
import modbuspal.binding.Binding;
import modbuspal.binding.BindingFactory;
import modbuspal.generator.GeneratorFactory;
import modbuspal.link.ModbusSerialLink;
import modbuspal.script.ScriptListener;
import modbuspal.script.ScriptRunner;
import modbuspal.slave.ModbusSlave;
import modbuspal.slave.ModbusSlavePduProcessor;
import modbuspal.toolkit.FileTools;
import modbuspal.toolkit.GUITools;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author avincon
 */
public class ModbusPalProject
extends ModbusPalProject2
{

    final IdGenerator idGenerator = new IdGenerator();
    File projectFile = null;
    private final ArrayList<ModbusPalListener> listeners = new ArrayList<ModbusPalListener>(); // synchronized
    private final ArrayList<Automation> automations = new ArrayList<Automation>();
    private final ArrayList<ScriptRunner> startupScripts = new ArrayList<ScriptRunner>();
    private final ArrayList<ScriptListener> scriptListeners = new ArrayList<ScriptListener>(); // synchronized
    private final ArrayList<ScriptRunner> ondemandScripts = new ArrayList<ScriptRunner>();
    private boolean learnModeEnabled = false;

    final GeneratorFactory generatorFactory = new GeneratorFactory();
    final BindingFactory bindingFactory = new BindingFactory();
    
    String selectedLink = "none";
    String linkTcpipPort = "502";
    String linkSerialComId = "none";
    String linkSerialBaudrate = "9600";
    int linkSerialParity = ModbusSerialLink.PARITY_EVEN;
    boolean linkSerialXonXoff = false;
    boolean linkSerialRtsCts = false;
    File linkReplayFile = null;
    
    
    //==========================================================================
    //
    // LOAD PROJECT
    //
    //==========================================================================





    public static ModbusPalProject load(File source)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        // the parse will fail if xml doc doesn't match the dtd.
        Document doc = XMLTools.ParseXML(source);

        // normalize text representation
        doc.getDocumentElement().normalize();

        ModbusPalProject project = new ModbusPalProject(doc, source);
        return project;
    }



    ModbusPalProject()
    {
    }

    private ModbusPalProject(Document doc, File source)
    throws InstantiationException, IllegalAccessException
    {
        // get the root node
        String name = doc.getDocumentElement().getNodeName();
        System.out.println("load "+name);
        projectFile = source;

        loadParameters(doc);
        generatorFactory.load(doc, source);
        bindingFactory.load(doc, source);
        loadAutomations(doc);
        loadSlaves(doc);
        loadBindings(doc,null);
        loadScripts(doc, projectFile);

        // execute startup scripts
        for( ScriptRunner runner:startupScripts )
        {
            runner.execute();
        }
    }
    
    /**
     * this method  will parse the content of doc in order to find and parse
     * elements from the doc that are related to the static part of the Automation
     * class.
     * @param doc
     */
    private void loadParameters(Document doc)
    {
        idGenerator.load(doc);
        loadLinks(doc);
    }

    private void loadLinks(Document doc)
    {
        NodeList linksNode = doc.getElementsByTagName("links");
        if( linksNode.getLength() == 1 )
        {
            loadLinksParameters(linksNode.item(0));
        }
        else if( linksNode.getLength() > 1 )
        {
            //TODO: error
        }
    }

    private void loadLinksParameters(Node linkRoot)
    {
        // get the "selected" attributes of the "link" openTag
        selectedLink = XMLTools.getAttribute("selected", linkRoot);

        // find child "tcpip" node and load parameters from it
        Node tcpipNode = XMLTools.findChild(linkRoot, "tcpip");
        linkTcpipPort = XMLTools.getAttribute("port", tcpipNode);

        // find child "serial" node and load parameters from it
        Node serialNode = XMLTools.findChild(linkRoot, "serial");
        loadSerialLinkParameters(serialNode);

        // find child "replay" node and load parameters from it
        Node replayNode = XMLTools.findChild(linkRoot, "replay");
        if( replayNode!=null )
        {
            loadReplayLinkParameters(replayNode);
        }
    }



    private void loadSerialLinkParameters(Node root)
    {
        // load "com" attribute
        linkSerialComId = XMLTools.getAttribute("com", root);

        // load "baudrate" attribute
        linkSerialBaudrate = XMLTools.getAttribute("baudrate", root);

        // load "parity" attribute
        String parity = XMLTools.getAttribute("parity", root);
        if( parity!=null )
        {
            if( parity.compareTo("none")==0 ) {
                linkSerialParity = ModbusSerialLink.PARITY_NONE;
            } else if( parity.compareTo("odd")==0 ) {
                linkSerialParity = ModbusSerialLink.PARITY_ODD;
            } else {
                linkSerialParity = ModbusSerialLink.PARITY_EVEN;
            }
        }

        // load flow control parameters
        Node flowControlNode = XMLTools.findChild(root, "flowcontrol");
        if( flowControlNode != null )
        {
            loadFlowControlParameters(flowControlNode);
        }
    }




    private void loadFlowControlParameters(Node root)
    {
        // load xonxoff attribute
        String xonxoffValue = XMLTools.getAttribute("xonxoff", root);
        if( xonxoffValue!=null )
        {
            linkSerialXonXoff = Boolean.parseBoolean(xonxoffValue);
        }

        // load rtscts attribute
        String rtsctsValue = XMLTools.getAttribute("rtscts", root);
        if( rtsctsValue!=null )
        {
            linkSerialRtsCts = Boolean.parseBoolean(rtsctsValue);
        }
    }

    private void loadReplayLinkParameters(Node node)
    {
        // find "rel"
        Node rel = XMLTools.findChild(node, "rel");
        if( rel != null )
        {
            // try to load file from relative path
            String relativePath = rel.getTextContent();
            String absolutePath = FileTools.makeAbsolute(projectFile, relativePath);
            File file = new File(absolutePath);
            if( file.exists()==true )
            {
                linkReplayFile = file;
                return;
            }
        }

        // find "abs"
        Node abs = XMLTools.findChild(node, "abs");
        if( abs != null )
        {
            String path = abs.getTextContent();
            File file = new File(path);
            if( file.exists()==true )
            {
                linkReplayFile = file;
                return;
            }
        }
    }

    public void loadAutomations(Document doc)
    throws InstantiationException, IllegalAccessException
    {
        NodeList automationsList = doc.getElementsByTagName("automation");
        for(int i=0; i<automationsList.getLength(); i++)
        {
            Node automationNode = automationsList.item(i);
            NamedNodeMap attributes = automationNode.getAttributes();

            // Get the name of the automation we want to load
            String name = attributes.getNamedItem("name").getNodeValue();

            // Check if an automation already exists with the same name:
            Automation automation = getAutomation( name );

            // If already exists:
            if( automation!=null )
            {
                // display a dialog and ask the user what to do:
                ErrorMessage dialog = new ErrorMessage(2);
                dialog.append("An automation called \""+name+"\" already exists. Do you want to overwrite the existing automation or to keep it ?");
                dialog.setButton(0,"Overwrite");
                dialog.setButton(1,"Keep existing");
                dialog.setTitle("Importing automation \""+name+"\"");
                dialog.setVisible(true);

                // if the user does not want to overwrite the existing
                // automation, skip and continue with the other automations
                if( dialog.getButton() != 0 )
                {
                    continue;
                }

                // otherwise, replace the content of the existing automation
                // with the new settings:
                automation.loadAttributes(attributes);

                // remove the existing generators before loading
                // the new ones:
                automation.removeAllGenerators();
            }

            // no automation with this name exists:
            else
            {
                automation = new Automation( attributes );
            }

            // finally, load the generators (whether a new automation is created
            // an existing automation is overwritten).
            automation.loadGenerators( automationNode.getChildNodes(), generatorFactory );
            addAutomation(automation);
        }
    }

    /**
     * looks for all occurences of the "slave" openTag in the provided document
     * and create a modbus slave for each.
     * @param doc
     */
    private void loadSlaves(Document doc)
    {
        NodeList slavesList = doc.getElementsByTagName("slave");
        for(int i=0; i<slavesList.getLength(); i++)
        {
            Node slaveNode = slavesList.item(i);
            //NamedNodeMap attributes = slaveNode.getAttributes();
            //ModbusSlave slave = new ModbusSlave( attributes );
            //slave.load( slaveNode );
            ModbusSlave slave = new ModbusSlave( this, slaveNode );
            addModbusSlave( slave );
        }
    }

    // TODO: why is it public ???
    /**
     * This method scans the content of the document in order to find all
     * "<binding>" tags, and then call the loadBinding(Node) method for each
     * of them.
     * @param doc
     */
    public void loadBindings(Document doc, ModbusSlave slave)
    {
        NodeList list = doc.getElementsByTagName("binding");
        for(int i=0; i<list.getLength(); i++ )
        {
            Node bindingNode = list.item(i);
            try
            {
                loadBinding(bindingNode, slave);
            }

            catch (InstantiationException ex)
            {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex)
            {
                Logger.getLogger(ModbusPalPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }



    /**
     * This method will examine the content of a "<binding>" tag in order to
     * parse the attributes it contains, and also the child tags that may exist.
     * @param node reference on the Node that represents a "<binding>" tag in the
     * project.
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    private void loadBinding(Node node, ModbusSlave slave)
    throws InstantiationException, IllegalAccessException
    {
        // get the attributes of the "binding" openTag
        NamedNodeMap attributes = node.getAttributes();

        // extract the "automation" attribute
        Node automationNode = attributes.getNamedItem("automation");
        String automationName = automationNode.getNodeValue();

        // retrieve the reference to the automation with id "automationID"
        Automation automation = getAutomation(automationName);
        if( automation == null )
        {
            // TODO: display an error message,
            // the binding cannot be achieved if no automation!
            return;
        }

        // extract the "class" attribute
        Node classNode = attributes.getNamedItem("class");
        String className = classNode.getNodeValue();

        // extract the "order" attribute.
        Node orderNode = attributes.getNamedItem("order");
        String orderValue = orderNode.getNodeValue();
        int wordOrder = Integer.parseInt(orderValue);

        // retrieve the register that is the parent of this node
        Node parentRegister = XMLTools.findParent(node,"register");
        String parentAddress = XMLTools.getAttribute(ModbusPalXML.XML_ADDRESS_ATTRIBUTE, parentRegister);
        int registerAddress = Integer.parseInt( parentAddress );

        // Instanciate the binding:
        Binding binding = bindingFactory.newBinding(className);
        binding.setup(automation, wordOrder);


        if( slave==null)
        {
            // retrieve the slave that is the parent of this register
            Node parentSlave = XMLTools.findParent(parentRegister, "slave");
            String slaveAddress = XMLTools.getAttribute(ModbusPalXML.XML_SLAVE_ID_ATTRIBUTE, parentSlave);
            int slaveId = Integer.parseInt(slaveAddress);
            slave = getModbusSlave(slaveId);
        }

        // bind the register and the automation
        slave.getHoldingRegisters().bind(registerAddress, binding);
    }




    /**
     * This method will only load "STARTUP" and "ON DEMAND" scripts.
     * Generator and binding scripts are loaded in a separate procedure.
     * @param doc
     * @param projectFile
     */
    private void loadScripts(Document doc, File projectFile)
    {
        // look for "startup" scripts section
        NodeList startup = doc.getElementsByTagName("startup");
        for( int i=0; i<startup.getLength(); i++ )
        {
            loadStartupScripts( startup.item(i), projectFile );
        }

        // look for "ondemand" scripts section
        NodeList ondemand = doc.getElementsByTagName("ondemand");
        for( int i=0; i<ondemand.getLength(); i++ )
        {
            loadOndemandScripts( ondemand.item(i), projectFile );
        }
    }

    private void loadStartupScripts(Node node, File projectFile)
    {
        // get list of sub nodes
        NodeList nodes = node.getChildNodes();

        for(int i=0; i<nodes.getLength(); i++ )
        {
            Node scriptNode = nodes.item(i);
            if( scriptNode.getNodeName().compareTo("script")==0 )
            {
                File scriptFile = loadScript(scriptNode, projectFile, true);
                if( scriptFile!=null )
                {
                    addStartupScript(scriptFile);
                }
            }
        }
    }

    private void loadOndemandScripts(Node node, File projectFile)
    {
        // get list of sub nodes
        NodeList nodes = node.getChildNodes();

        for(int i=0; i<nodes.getLength(); i++ )
        {
            Node scriptNode = nodes.item(i);
            if( scriptNode.getNodeName().compareTo("script")==0 )
            {
                File scriptFile = loadScript(scriptNode, projectFile, true);
                if( scriptFile!=null )
                {
                    addScript(scriptFile);
                }
            }
        }
    }
  
    private static File loadScript(Node node, File projectFile, boolean promptUser)
    {
        // find "rel"
        Node rel = XMLTools.findChild(node, "rel");
        if( rel != null )
        {
            // try to load file from relative path
            String relativePath = rel.getTextContent();
            String absolutePath = FileTools.makeAbsolute(projectFile, relativePath);
            File file = new File(absolutePath);
            if( file.exists()==true )
            {
                return file;
            }
        }

        // find "abs"
        Node abs = XMLTools.findChild(node, "abs");
        if( abs == null )
        {
            throw new RuntimeException("malformed input");
        }

        String path = abs.getTextContent();
        File file = new File(path);
        if( file.exists()==true )
        {
            return file;
        }

        // Print error
        System.out.println("No file found for script "+file.getPath());

        // IF NO FILE FOUND, PROMPT USER:
        if(promptUser==true)
        {
            // create error message box with 2 buttons:
            return GUITools.promptUserFileNotFound(null, file);
        }

        return null;
    }










    //==========================================================================
    //
    // SAVE PROJECT
    //
    //==========================================================================


    public void save() 
    throws FileNotFoundException, IOException
    {
        // create output stream
        FileOutputStream out = new FileOutputStream(projectFile);
        save(out, projectFile);
        out.close();
    }

    private void save(OutputStream out, File projectFile)
    throws IOException
    {
        String xmlTag = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n";
        out.write( xmlTag.getBytes() );

        String docTag = "<!DOCTYPE modbuspal_project SYSTEM \"modbuspal.dtd\">\r\n";
        out.write( docTag.getBytes() );

        String openTag = "<modbuspal_project>\r\n";
        out.write( openTag.getBytes() );

        saveParameters(out);
        generatorFactory.save(out, projectFile);
        bindingFactory.save(out, projectFile);
        saveAutomations(out);
        saveSlaves(out);
        saveScripts(out, projectFile);

        String closeTag = "</modbuspal_project>\r\n";
        out.write( closeTag.getBytes() );
    }

    /**
     * Saves the project's parameters that do not fall into the "slave" or "automation"
     * categories, like id generator and link settings. The parameters are written
     * into the output stream as XML tags.
     * @param out
     * @throws IOException
     */
    private void saveParameters(OutputStream out)
    throws IOException
    {
        // save id creator:
        idGenerator.save(out);

        // save link parameters
        saveLinks(out);
    }


    private void saveLinks(OutputStream out)
    throws IOException
    {
        StringBuilder openTag = new StringBuilder("<links ");

        // fill "current" with "tcpip" or "serial" depending on the
        // currently selected tab
        openTag.append("selected=\"").append(selectedLink).append("\" ");

        // terminate open openTag and write it
        openTag.append(">\r\n");
        out.write( openTag.toString().getBytes() );

        // write tcp/ip settings
        saveTcpIpLinkParameters(out);

        // write serial settings
        saveSerialLinkParameters(out);

        // write replay settings
        saveReplayLinkParameters(out);

        // close openTag
        String closeTag = "</links>\r\n";
        out.write( closeTag.getBytes() );
    }

    private void saveTcpIpLinkParameters(OutputStream out)
    throws IOException
    {
        StringBuilder tag = new StringBuilder("<tcpip ");
        tag.append("port=\"").append(linkTcpipPort).append("\" ");
        tag.append("/>\r\n");
        out.write( tag.toString().getBytes() );
    }

    private void saveSerialLinkParameters(OutputStream out) throws IOException
    {
        StringBuilder openTag = new StringBuilder("<serial ");
        openTag.append("com=\"").append(linkSerialComId).append("\" ");
        openTag.append("baudrate=\"").append(linkSerialBaudrate).append("\" ");
        switch(linkSerialParity)
        {
            case ModbusSerialLink.PARITY_NONE: openTag.append("parity=\"none\" "); break;
            case ModbusSerialLink.PARITY_ODD: openTag.append("parity=\"odd\" "); break;
            default:
            case ModbusSerialLink.PARITY_EVEN: openTag.append("parity=\"even\" "); break;
        }
        openTag.append(">\r\n");
        out.write( openTag.toString().getBytes() );

        StringBuilder flowControl = new StringBuilder("<flowcontrol ");
        flowControl.append("xonxoff=\"").append(String.valueOf(linkSerialXonXoff)).append("\" ");
        flowControl.append("rtscts=\"").append(String.valueOf(linkSerialRtsCts)).append("\" ");
        flowControl.append("/>\r\n");

        out.write( flowControl.toString().getBytes() );

        String closeTag = "</serial>\r\n";
        out.write( closeTag.getBytes() );
    }


    private void saveReplayLinkParameters(OutputStream out) throws IOException
    {
        if( linkReplayFile!=null )
        {
            String openTag = "<replay>\r\n";
            out.write(openTag.getBytes());

            // create abs tag
            String absTag = "<abs>" + linkReplayFile.getPath() + "</abs>\r\n";
            out.write(absTag.getBytes());

            String rel = FileTools.makeRelative(projectFile, linkReplayFile);
            if( rel != null )
            {
                // create rel tag
                String relTag = "<rel>"+ rel +"</rel>\r\n";
                out.write(relTag.getBytes());
            }

            String closeTag = "</replay>\r\n";
            out.write(closeTag.getBytes());
        }
    }


    private void saveAutomations(OutputStream out)
    throws IOException
    {
        for(int i=0; i<automations.size(); i++)
        {
            Automation automation = automations.get(i);
            automation.save(out);
        }
    }

    private void saveSlaves(OutputStream out)
    throws IOException
    {
        for( int i=0; i<ModbusConst.MAX_MODBUS_SLAVE; i++ )
        {
            ModbusSlave slave = getModbusSlave(i);
            if( slave != null )
            {
                slave.save(out,true);
            }
        }
    }


    private void saveScripts(OutputStream out, File projectFile)
    throws IOException
    {
        saveStartupScripts(out,projectFile);
        saveOndemandScripts(out,projectFile);
    }

    private void saveStartupScripts(OutputStream out, File projectFile)
    throws IOException
    {
        if( startupScripts.isEmpty() )
        {
            return;
        }

        String openTag = "<startup>\r\n";
        out.write( openTag.getBytes() );

        for(ScriptRunner runner:startupScripts)
        {
            runner.save(out,projectFile);
        }

        String closeTag = "</startup>\r\n";
        out.write(closeTag.getBytes());
    }

    private void saveOndemandScripts(OutputStream out, File projectFile)
    throws IOException
    {
        if( ondemandScripts.isEmpty() )
        {
            return;
        }

        String openTag = "<ondemand>\r\n";
        out.write( openTag.getBytes() );

        for(ScriptRunner runner:ondemandScripts)
        {
            runner.save(out,projectFile);
        }

        String closeTag = "</ondemand>\r\n";
        out.write(closeTag.getBytes());
    }









    //==========================================================================
    //
    // AUTOMATIONS MANAGEMENT
    //
    //==========================================================================

    /**
     * Find the automation which exactly matches the specified name.
     * @param name is the name of the automation to obtain
     * @return the Automation object associated with the specified name, or null
     * is no automation exists with that name.
     */
    public Automation getAutomation(String name)
    {
        if( NullAutomation.NAME.compareTo(name)==0 )
        {
            return NullAutomation.getInstance();
        }

        for(int i=0; i<automations.size(); i++)
        {
            Automation automation = automations.get(i);
            if( automation.getName().compareTo(name)==0 )
            {
                return automation;
            }
        }
        return null;
    }

    /**
     * Add the provided automation into the current application. Please note that
     * each automation in the project must have a unique name. The addition will
     * fail if an existing automation already uses the same name as the automation
     * you want to add.
     * @param automation
     * @return true if the automation is added successfully, false otherwise.
     */
    public boolean addAutomation(Automation automation)
    {
        // check if an automation already exists with the same name
        String name = automation.getName();
        if( getAutomation(name) != null )
        {
            return false;
        }

        automations.add(automation);
        int index = automations.indexOf(automation);
        notifyAutomationAdded(automation, index);
        return true;
    }

    private void notifyAutomationAdded(Automation automation, int index)
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.automationAdded(automation, index);
            }
        }
    }

    // TODO: is it safe ???
    /**
     * Get all the automations that are defined in the current project.
     * @return an array containing all the automations that are defined in
     * the project.
     */
    public Automation[] getAutomations()
    {
        Automation[] out = new Automation[0];
        return automations.toArray(out);
    }

    public void startAllAutomations()
    {
        for(int i=0; i<automations.size(); i++ )
        {
            Automation auto = automations.get(i);
            auto.start();
        }
    }

    public void stopAllAutomations()
    {
        for(int i=0; i<automations.size(); i++ )
        {
            Automation auto = automations.get(i);
            auto.stop();
        }
    }

    public void removeAutomation(Automation automation)
    {
        // disconnect the automation from the rest of the project
        automation.disconnect();
        // remove automation from list
        automations.remove(automation);
        notifyAutomationRemoved(automation);
    }

    void removeAllAutomations()
    {
        Automation list[] = new Automation[0];
        list = automations.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            removeAutomation(list[i]);
        }
    }

    private void notifyAutomationRemoved(Automation automation)
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.automationRemoved(automation);
            }
        }
    }


    public boolean automationExists(String name)
    {
        return (getAutomation(name)!=null);
    }


    /**
     * the goal of this method is to verify that each automation in the project
     * has a unique name.
     * @param auto
     * @param name
     * @return a unique automation name. same value as 'name' if it is already
     * unique, or a modified version of 'name'.
     */
    public String checkAutomationNewName(Automation auto, String name)
    {
        // TODO: is synchronization required??
        //synchronized(this)
        {
            Automation already = getAutomation(name);
            if( (already != null) && (already != auto) )
            {
                // check if the name to alter already end with "#n"
                if( name.matches(".*#(\\d+)$")==true )
                {
                    int pos = name.lastIndexOf('#');
                    name = name.substring(0, pos);
                }
                else
                {
                    name = name.trim() + " ";
                }
                name = name + "#" + String.valueOf( idGenerator.createID() );
            }
            auto.setName(name);
        }
        return name;
    }


    /**
     * This method adds an automation slave into the application. If an automation
     * with the same name already exists, a dialog will popup and invite the user
     * to choose between keeping the existing automation or replacing it by the new.
     * @param automation the new automation to add
     * @return a reference on the new or existing automation, depending on the
     * user's choice. null if an error occured.
     */
    public Automation submitAutomation(Automation automation)
    {
        // check if an automation already exists with the same name
        String name = automation.getName();
        Automation existing = getAutomation(name);

        if( existing != null )
        {
            // show a dialog to let the user decide
            // what to do in order to resolve the conflict:
            ErrorMessage conflict = new ErrorMessage(2);
            conflict.setTitle("Address conflict");
            conflict.append("You are trying to add a new automation with name \"" + name + "\".");
            conflict.append("An existing automation already exists with this name. What do you want to do ?");
            conflict.setButton(0, "Keep existing");
            conflict.setButton(1, "Replace with new");
            conflict.setVisible(true);

            // if "Keep existing" is chosen:
            if( conflict.getButton()==0 )
            {
                return existing;
            }

            else
            {
                // before replacing with new, remove old:
                removeAutomation(existing);
            }
        }

        if( addAutomation(automation)==true )
        {
            return automation;
        }
        return null;
    }



    //==========================================================================
    //
    // GENERATORS
    //
    //==========================================================================

    /**
     * remove all instances of the generator whose name is passed
     * in argument. the method will scan all automations of the current
     * project and remove each instance of the generator identified
     * by the provided name.
     * @param classname
     */
    private void removeAllGenerators(String classname)
    {
        int max = automations.size();
        for(int i=0; i<max; i++)
        {
            Automation auto = automations.get(i);
            auto.removeAllGenerators(classname);
        }
    }

    public void removeGeneratorScript(ScriptRunner runner)
    {
        removeAllGenerators(runner.getClassName());
        generatorFactory.remove( runner );
    }

    public void addGeneratorInstanciator(File scriptFile)
    {
        // newInstance a scripted generator handler
        ScriptRunner sr = ScriptRunner.create(this, scriptFile);

        // test if newInstance would work:
        if( sr.newGenerator() != null )
        {
            // add the handler to the factory:
            generatorFactory.add(sr);
        }
        else
        {
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("Script error");
            dialog.append("The script probably contains errors and cannot be executed properly.");
            dialog.setVisible(true);
        }
    }

    public GeneratorFactory getGeneratorFactory()
    {
        return generatorFactory;
    }






    //==========================================================================
    //
    // BINDINGS
    //
    //==========================================================================

    /**
     * remove all instances of the generator whose name is passed
     * in argument. the method will scan all automations of the current
     * project and remove each instance of the generator identified
     * by the provided name.
     * @param classname
     */
    void removeAllBindings(String classname)
    {
        for( int i=0; i<ModbusConst.MAX_MODBUS_SLAVE; i++ )
        {
            ModbusSlave slave = getModbusSlave(i);
            if( slave != null )
            {
                slave.removeAllBindings(classname);
            }
        }
    }

    public void removeBindingScript(ScriptRunner runner)
    {
        removeAllBindings(runner.getClassName());
        bindingFactory.remove( runner );
    }

    public void addBindingInstanciator(File scriptFile)
    {
        // newInstance a scripted generator handler
        ScriptRunner sr = ScriptRunner.create(this, scriptFile);

        // test if newInstance would work:
        if( sr.newBinding() != null )
        {
            // add the handler to the factory:
            bindingFactory.add(sr);
        }
        else
        {
            ErrorMessage dialog = new ErrorMessage("Close");
            dialog.setTitle("Script error");
            dialog.append("The script probably contains errors and cannot be executed properly.");
            dialog.setVisible(true);
        }
    }

    public BindingFactory getBindingFactory()
    {
        return bindingFactory;
    }



    //==========================================================================
    //
    // SLAVES MANAGEMENT
    //
    //==========================================================================

    /**
     * This method adds a modbus slave into the application. The addition will
     * fail a modbus slave with the same id already exists in thecurrent project.
     * @param slave the new modbus slave to add
     * @return true if added successfully, false otherwise.
     */
    public boolean addModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();

        // check if slaveID is already assigned:
        if( getModbusSlave(slaveID) != null )
        {
            return false;
        }

        setModbusSlave(slaveID, slave);
        return true;
    }

    @Override
    protected void notifySlaveAdded(ModbusSlave slave)
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.modbusSlaveAdded(slave);
            }
        }
    }

    /**
     * Returns an array of ModbusSlave objects, which hold all the
     * information on the slaves with the specified name. In ModbusPal, multiple
     * slaves can have the same name.
     * @param name the name of the slaves you want to get
     * @return an array of ModbusSlave objects, each slave in the array having the
     * specified name.
     */
    public ModbusSlave[] findModbusSlaves(String name)
    {
        ArrayList<ModbusSlave> found = new ArrayList<ModbusSlave>();

        for( int i=0; i<ModbusConst.MAX_MODBUS_SLAVE; i++ )
        {
            ModbusSlave slave = getModbusSlave(i);
            if( slave != null )
            {
                if( slave.getName().compareTo(name)==0 )
                {
                    found.add(slave);
                }
            }
        }

        if( found.isEmpty() )
        {
            return null;
        }
        else
        {
            ModbusSlave retval[] = new ModbusSlave[0];
            retval = found.toArray(retval);
            return retval;
        }
    }


    /**
     * This method adds a modbus slave into the application. If a modbus slave
     * with the same id already exists, a dialog will popup and invite the user
     * to choose between keeping the existing slave or replacing it by the new.
     * @param slave the new modbus slave to add
     * @return a reference on the new or existing modbus slave, depending on the
     * user's choice. null if an error occured.
     */
    public ModbusSlave submitModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();

        // check if slaveID is already assigned:
        if( getModbusSlave(slaveID) != null )
        {
            // show a dialog to let the user decide
            // what to do in order to resolve the conflict:
            ErrorMessage conflict = new ErrorMessage(2);
            conflict.setTitle("Address conflict");
            conflict.append("You are trying to add a new slave with address " + slaveID + ".");
            conflict.append("An existing slave already uses this address. What do you want to do ?");
            conflict.setButton(0, "Keep existing");
            conflict.setButton(1, "Replace with new");
            conflict.setVisible(true);

            // if "Keep existing" is chosen:
            if( conflict.getButton()==0 )
            {
                return getModbusSlave(slaveID);
            }

            else
            {
                // before replacing with new, remove old:
                removeModbusSlave(slaveID);
            }
        }

        if( addModbusSlave(slave)==true )
        {
            return slave;
        }
        return null;
    }

    public void removeModbusSlave(int slaveID)
    {
        ModbusSlave slave = getModbusSlave(slaveID);
        if( slave!=null )
        {
            slave.clear();
        }
        // disconnect slave from list
        setModbusSlave(slaveID, null);
    }

    public void removeModbusSlave(ModbusSlave slave)
    {
        int slaveID = slave.getSlaveId();
        removeModbusSlave(slaveID);
    }

    @Override
    protected void notifySlaveRemoved(ModbusSlave slave)
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.modbusSlaveRemoved(slave);
            }
        }
    }



    public void duplicateModbusSlave(int idSrc, int idDst, String name)
    {
        ModbusSlave newSlave = new ModbusSlave(idDst);
        newSlave.setName(name);

        try
        {
            // Create a temporary file in order to exportSlave the model
            File tempFile = File.createTempFile("modbuspal", null);

            // indicate that the file must be deleted at the end of the
            // application, just in case...
            tempFile.deleteOnExit();

            // exportSlave model into xml
            exportSlave(tempFile, idSrc, true, false );

            // import xml into new slave
            importSlave(tempFile, idDst, true, false);

            tempFile.delete();
        }
        catch (Exception ex)
        {
            Logger.getLogger(ModbusPalProject.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // add it into the list
        addModbusSlave(newSlave);
    }

    public void setSlaveEnabled(int slaveID, boolean b)
    {
        ModbusSlave ms = getModbusSlave(slaveID, learnModeEnabled);
        if( ms!=null )
        {
            ms.setEnabled(b);
        }
    }

    /**
     * determine if the slave is enabled or not. If "learn mode" is enabled, this
     * method will always return true. Otherwise, it will return true only if the
     * slave exists in the list of known slaves AND if that slave is enabled.
     * In all cases, if the slave with the provided ID doesn't exist in the list
     * of known slaves, it will be added to the list.
     * @param slaveID
     * @return true if the slave is enabled; false otherwise.
     */
    public boolean isSlaveEnabled(int slaveID)
    {
        if( (slaveID<0) || (slaveID>=ModbusConst.MAX_MODBUS_SLAVE) )
        {
            return false;
        }

        ModbusSlave ms = getModbusSlave(slaveID,learnModeEnabled);
        if( ms!=null )
        {
            return ms.isEnabled();
        }
        return false;
    }

    public void exportSlave(File exportFile, int modbusID, boolean withBindings, boolean withAutomations)
    throws FileNotFoundException, IOException
    {
        ModbusSlave exportedSlave = getModbusSlave(modbusID);
        
        OutputStream out = new FileOutputStream(exportFile);

        String xmlTag = "<?xml version=\"1.0\"?>\r\n";
        out.write( xmlTag.getBytes() );

        String docTag = "<!DOCTYPE modbuspal_slave SYSTEM \"modbuspal.dtd\">\r\n";
        out.write( docTag.getBytes() );

        String openTag = "<modbuspal_slave>\r\n";
        out.write( openTag.getBytes() );

        // if needed, first exportSlave automations (they need to be imported first!)
        if( withAutomations == true )
        {
            String names[] = exportedSlave.getRequiredAutomations();
            for(int i=0; i<names.length; i++)
            {
                Automation automation = getAutomation( names[i] );
                automation.save(out);
            }
        }
        exportedSlave.save(out,withBindings);

        String closeTag = "</modbuspal_slave>\r\n";
        out.write( closeTag.getBytes() );
        out.close();
    }


    public void importSlave(File importFile, int idDst, boolean withBindings, boolean withAutomations)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        Document doc = XMLTools.ParseXML(importFile);

        // normalize text representation
        doc.getDocumentElement().normalize();

        importSlave(doc, idDst, withBindings, withAutomations);
    }

    public void importSlave(Document doc, int idDst, boolean withBindings, boolean withAutomations)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        ModbusSlave target = getModbusSlave(idDst,true);
        importSlave(doc, target, withBindings, withAutomations);
    }

    public void importSlave(Document doc, ModbusSlave target, boolean withBindings, boolean withAutomations)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        // how many slaves in the file?
        NodeList slaves = doc.getElementsByTagName("slave");
        Node slaveNode = slaves.item(0);

        if( withAutomations==true )
        {
            loadAutomations(doc);
        }

        target.load(slaveNode,true);

        if( withBindings==true )
        {
            loadBindings(doc, target);
        }
    }


    //==========================================================================
    //
    // REGISTERS AND COILS
    //
    //==========================================================================


    public ModbusSlavePduProcessor getSlavePduProcessor(int slaveID, byte functionCode)
    {
        ModbusSlave ms = getModbusSlave(slaveID, learnModeEnabled);
        if( ms != null )
        {
            return ms.getPduProcessor(functionCode);
        }

        return null;
    }



    @Deprecated
    public boolean holdingRegistersExist(int slaveID, int startingAddress, int quantity)
    {
        ModbusSlave ms = getModbusSlave(quantity, learnModeEnabled);
        if(ms==null)
        {
            return false;
        }
        return ms.getHoldingRegisters().exist(startingAddress, quantity, learnModeEnabled);
    }

    @Deprecated
    public boolean coilsExist(int slaveID, int startingAddress, int quantity)
    {
        ModbusSlave ms = getModbusSlave(quantity, learnModeEnabled);
        if(ms==null)
        {
            return false;
        }
        return ms.getCoils().exist(startingAddress, quantity, learnModeEnabled);
    }









    
    //==========================================================================
    //
    // SCRIPTS MANAGEMENT
    //
    //==========================================================================

    public void addStartupScript(File scriptFile)
    {
        // create a new script handler
        ScriptRunner runner = ScriptRunner.create(this, scriptFile);
        startupScripts.add(runner);
        notifyStartupScriptAdded(runner);
    }

    public void removeStartupScript(ScriptRunner script)
    {
        if( startupScripts.contains(script) )
        {
            startupScripts.remove(script);
            notifyStartupScriptRemoved(script);
        }
    }

    public void addScript(File scriptFile)
    {
        ScriptRunner runner = ScriptRunner.create(this, scriptFile);
        ondemandScripts.add(runner);
        notifyScriptAdded(runner);
    }

    public void removeScript(ScriptRunner runner)
    {
        if( ondemandScripts.remove(runner)==true )
        {
            notifyScriptRemoved(runner);
        }
    }

    private void notifyStartupScriptAdded(ScriptRunner runner)
    {
        synchronized(scriptListeners)
        {
            for(ScriptListener l:scriptListeners)
            {
                l.startupScriptAdded(runner);
            }
        }
    }

    private void notifyScriptAdded(ScriptRunner runner)
    {
        synchronized(scriptListeners)
        {
            for(ScriptListener l:scriptListeners)
            {
                l.scriptAdded(runner);
            }
        }
    }

    private void notifyScriptRemoved(ScriptRunner runner)
    {
        synchronized(scriptListeners)
        {
            for(ScriptListener l:scriptListeners)
            {
                l.scriptRemoved(runner);
            }
        }
    }

    private void notifyStartupScriptRemoved(ScriptRunner runner)
    {
        synchronized(scriptListeners)
        {
            for(ScriptListener l:scriptListeners)
            {
                l.startupScriptRemoved(runner);
            }
        }
    }

    public Iterable<ScriptRunner> getStartupScripts()
    {
        return startupScripts;
    }

    public Iterable<ScriptRunner> getScripts()
    {
        return ondemandScripts;
    }

    //==========================================================================
    //
    // LISTENERS
    //
    //==========================================================================

    public void addModbusPalListener(ModbusPalListener l)
    {
        synchronized(listeners)
        {
            if( listeners.contains(l)==false )
            {
                listeners.add(l);
            }
        }
    }

    public void removeModbusPalListener(ModbusPalListener l)
    {
        synchronized(listeners)
        {
            if( listeners.contains(l)==true )
            {
                listeners.remove(l);
            }
        }
    }

    public void addScriptListener(ScriptListener l)
    {
        synchronized(scriptListeners)
        {
            if( scriptListeners.contains(l)==false )
            {
                scriptListeners.add(l);
            }
        }
    }

    public void removeScriptListener(ScriptListener l)
    {
        synchronized(scriptListeners)
        {
            if( scriptListeners.contains(l)==true )
            {
                scriptListeners.remove(l);
            }
        }
    }



    //==========================================================================
    //
    // MISC.
    //
    //==========================================================================

    public void setLearnModeEnabled(boolean en)
    {
        learnModeEnabled = en;
    }

    public boolean isLeanModeEnabled()
    {
        return learnModeEnabled;
    }

    public void notifyPDUnotServiced()
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.pduNotServiced();
            }
        }
    }

    public void notifyPDUprocessed()
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.pduProcessed();
            }
        }
    }

    public void notifyExceptionResponse()
    {
        synchronized(listeners)
        {
            for(ModbusPalListener l:listeners)
            {
                l.pduException();
            }
        }
    }

    /**
     * 
     * @param slaveID
     * @param functionCode
     * @return
     * @deprecated
     */
    @Deprecated
    public boolean isFunctionEnabled(int slaveID, byte functionCode)
    {
        //TODO: implement isFunctionEnabled
        return true;
    }


    public static void optimize(Document doc, boolean fix)
    {
        optimizeBindingsVsAutomations(doc,fix);
    }

    private static void optimizeBindingsVsAutomations(Document doc, boolean fix)
    {
        // get list of bindings:
        NodeList bindingList = doc.getElementsByTagName("binding");

        // get list of automations:
        NodeList automationList = doc.getElementsByTagName("automation");

        // for each automation, check that at least one binding is actually
        // using it:

        for( int i=0; i<automationList.getLength(); i++ )
        {
            String name = XMLTools.getAttribute("name", automationList.item(i));
            boolean matched = false;

            for( int j=0; j<bindingList.getLength(); j++)
            {
                String automation = XMLTools.getAttribute("automation", bindingList.item(j));
                if( automation.compareTo(name)==0 )
                {
                    matched = true;
                    break;
                }
            }

            // automation not matched, remove the useless automation:
            if( matched==false )
            {
                doc.removeChild(automationList.item(i));
            }
        }
        

        if( fix==true )
        {
            // for each binding, check that the automation specified in the
            // "automation" attribute actually exists:
            for( int i=0; i<bindingList.getLength(); i++ )
            {
                String automation = XMLTools.getAttribute("automation", bindingList.item(i));
                boolean matched = false;

                for( int j=0; j<automationList.getLength(); j++)
                {
                    String name = XMLTools.getAttribute("name", automationList.item(j));
                    if( automation.compareTo(name)==0 )
                    {
                        matched = true;
                        break;
                    }
                }

                // automation not matched, remove the invalid binding:
                if( matched==false )
                {
                    doc.removeChild(doc);
                }
            }
        }
    }

    public String getName()
    {
        if( projectFile==null )
        {
            return "no name";
        }
        return projectFile.getName();
    }


}
