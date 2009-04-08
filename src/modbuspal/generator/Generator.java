/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines a generator. An automation is made of a list of generators.
 * @author nnovic
 */
public class Generator
{   
    private ImageIcon icon;
    protected int duration = 10;
    protected double initialValue = 0.0;
    
    private JPanel controlPanel;

    /**
     * Creates a generator with default values, icon and control panel.
     */
    public Generator()
    {
        setIcon("/modbuspal/automation/Generator.png");
        controlPanel = createControlPanel();
    }

    private JPanel createControlPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );
        JLabel defaultLabel = new JLabel("No parameters.");
        panel.add(defaultLabel, BorderLayout.CENTER);
        return panel;
    }


    /**
     * The subclass can use this mehod to change the default icon of the generator.
     * The icon is the image that is visible on the left of the generator's control
     * panel, in the automation editor. 
     * @param iconUrl
     */
    protected boolean setIcon(String iconUrl)
    {
        URL url = null;

        // try to use class loader
        url = getClass().getResource(iconUrl);
        if( url!=null )
        {
            icon = new ImageIcon(url);
            return true;
        }

        // try to create an url
        try
        {
            url = new URL(iconUrl);
            if( url != null )
            {
                icon = new ImageIcon(url);
                return true;
            }
        }
        catch (MalformedURLException ex)
        {
            url = null;
        }

        // try to use url directly:
        File file = new File(iconUrl);
        if( file.exists() )
        {
            icon = new ImageIcon(file.getAbsolutePath());
            return true;
        }

        return false;
    }

    /**
     * Get the icon that is associated with this generator.
     * @return icon of the generator.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Get the configured duration of the generator.
     * @return duration of the generator, in seconds.
     */
    public int getDuration()
    {
        return duration;
    }

    /**
     * Get the control panel of the generator.
     * @return the control panel of the generator.
     */
    public JPanel getControlPanel()
    {
        return controlPanel;
    }

    
    public void setInitialValue(double value)
    {
        initialValue = value;
    }

    public double getValue(double time)
    {
        return 0.0;
    }


    public final void load(Node genNode)
    {
        NamedNodeMap attributes = genNode.getAttributes();

        Node durNode = attributes.getNamedItem("duration");
        String durVal= durNode.getNodeValue();
        duration = Integer.parseInt(durVal);

        loadSettings( genNode.getChildNodes() );
    }

    protected void loadSettings(NodeList childNodes)
    {
        return;
    }

    void setDuration(int val)
    {
        duration = val;
    }



    public final void save(OutputStream out)
    throws IOException
    {
        String openTag = createOpenTag();
        out.write(openTag.getBytes());

        saveSettings(out);

        String closeTag = "</generator>\r\n";
        out.write(closeTag.getBytes());
    }

    
    public String getClassName()
    {
        return getClass().getSimpleName();
    }

    private String createOpenTag()
    {
        StringBuffer tag = new StringBuffer("<generator");
        tag.append(" class=\""+ getClassName() +"\"");
        tag.append(" duration=\""+ String.valueOf(duration) +"\"");
        tag.append(">\r\n");
        return tag.toString();
    }

    protected void saveSettings(OutputStream out)
    throws IOException
    {
        return;
    }
}
