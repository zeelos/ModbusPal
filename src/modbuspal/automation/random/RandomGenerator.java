/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation.random;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.automation.*;
import modbuspal.main.XMLTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
public class RandomGenerator
extends Generator
{
    private RandomControlPanel panel;
    double minValue = 0.0;
    double maxValue = 0.0;
    boolean relativeMin = false;
    boolean relativeMax = false;

    public RandomGenerator()
    {
        setIcon("RandomGenerator.png");
        panel = new RandomControlPanel(this);
    }

    @Override
    public double getValue(double time)
    {
        double lowest = minValue;
        if( relativeMin == true )
        {
            lowest += initialValue;
        }

        double highest = maxValue;
        if( relativeMax == true )
        {
            highest += lowest;
        }

        double ran = Math.random();
        return lowest + ran * (highest-lowest);
    }

    @Override
    protected void saveSettings(OutputStream out)
    throws IOException
    {
        StringBuffer start = new StringBuffer("<min");
        start.append(" value=\""+String.valueOf(minValue)+"\"");
        start.append(" relative=\""+Boolean.toString(relativeMin)+"\"");
        start.append("/>\r\n");
        out.write( start.toString().getBytes() );

        StringBuffer end = new StringBuffer("<max");
        end.append(" value=\""+String.valueOf(maxValue)+"\"");
        end.append(" relative=\""+Boolean.toString(relativeMax)+"\"");
        end.append("/>\r\n");
        out.write( end.toString().getBytes() );
    }

    @Override
    protected void loadSettings(NodeList childNodes)
    {
        Node minNode = XMLTools.getNode(childNodes, "min");
        loadMin(minNode);

        Node maxNode = XMLTools.getNode(childNodes, "max");
        loadMax(maxNode);
    }

    private void loadMax(Node node)
    {
        // read attributes from xml document
        String maxVal = XMLTools.getAttribute("value", node);
        String maxRel = XMLTools.getAttribute("relative", node);

        // setup generator's values
        maxValue = Double.parseDouble(maxVal);
        relativeMax = Boolean.parseBoolean(maxRel);

        // update generator's panel
        panel.maxTextField.setText( String.valueOf(maxVal) );
        panel.maxRelativeCheckBox.setSelected(relativeMax);
    }

    private void loadMin(Node node)
    {
        // read attributes from xml document
        String minVal = XMLTools.getAttribute("value", node);
        String minRel = XMLTools.getAttribute("relative", node);

        // setup generator's values
        minValue = Double.parseDouble(minVal);
        relativeMin = Boolean.parseBoolean(minRel);
        
        // update generator's panel
        panel.minTextField.setText( String.valueOf(minValue) );
        panel.minRelativeCheckBox.setSelected(relativeMin);
    }

    @Override
    public JPanel getControlPanel()
    {
        return panel;
    }

}
