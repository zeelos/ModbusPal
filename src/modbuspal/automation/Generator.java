/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import modbuspal.automation.linear.LinearGenerator;
import modbuspal.automation.random.RandomGenerator;
import modbuspal.main.XMLTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines a generator. An automation is made of a list of generators.
 * @author nnovic
 */
public abstract class Generator
{

    public static Generator create(Node node)
    throws InvalidClassException
    {
        String value = XMLTools.getAttribute("class", node);
        if( value.compareTo("LinearGenerator")==0 )
        {
            return new LinearGenerator(node.getAttributes());
        }
        else if( value.compareTo("RandomGenerator")==0 )
        {
            return new RandomGenerator(node.getAttributes());
        }
        else
        {
            throw new InvalidClassException(value,"Unknown generator class");
        }
    }


    private ImageIcon icon;
    protected int duration = 10;
    protected double initialValue = 0.0;
    private ArrayList<GeneratorListener> generatorListeners = new ArrayList<GeneratorListener>();

    protected Generator(String iconUrl)
    {
        URL url = getClass().getResource(iconUrl);
        icon = new ImageIcon(url);
    }

    protected Generator(String iconUrl, NamedNodeMap attributes)
    {
        URL url = getClass().getResource(iconUrl);
        icon = new ImageIcon(url);

        Node durNode = attributes.getNamedItem("duration");
        String durVal= durNode.getNodeValue();
        duration = Integer.parseInt(durVal);
    }

    public abstract String getGeneratorName();

    public Icon getIcon()
    {
        return icon;
    }

    public int getDuration()
    {
        return duration;
    }

    public abstract JPanel getPanel();

    public void setInitialValue(double value)
    {
        initialValue = value;
    }

    public abstract double getValue(double time);

    public void save(OutputStream out)
    throws IOException
    {
        String openTag = createOpenTag();
        out.write(openTag.getBytes());

        saveGenerator(out);

        String closeTag = "</generator>\r\n";
        out.write(closeTag.getBytes());
    }

    public abstract void load(NodeList childNodes) ;

    public void setDuration(int val)
    {
        duration = val;
    }

    void notifyGeneratorHasEnded()
    {
        for(GeneratorListener l:generatorListeners)
        {
            l.generatorHasEnded(this);
        }
    }

    void notifyGeneratorHasStarted()
    {
        for(GeneratorListener l:generatorListeners)
        {
            l.generatorHasStarted(this);
        }
    }

    void removeAllGeneratorListeners()
    {
        generatorListeners.clear();
    }

    private String createOpenTag()
    {
        StringBuffer tag = new StringBuffer("<generator");
        tag.append(" class=\""+ getClass().getSimpleName() +"\"");
        tag.append(" duration=\""+ String.valueOf(duration) +"\"");
        tag.append(">\r\n");
        return tag.toString();
    }

    protected abstract void saveGenerator(OutputStream out) throws IOException;

    public void addGeneratorListener(GeneratorListener l)
    {
        assert( generatorListeners.contains(l) == false );
        generatorListeners.add(l);
    }

    public void removeGeneratorListener(GeneratorListener l)
    {
        assert( generatorListeners.contains(l) == true );
        generatorListeners.remove(l);
    }

}
