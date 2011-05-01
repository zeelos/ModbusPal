/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator.linear;

import modbuspal.generator.Generator;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.toolkit.XMLTools;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Linear generator
 * @author nnovic
 */
public class LinearGenerator
extends Generator
{
    private LinearControlPanel panel;
    double startValue = 0.0;
    double endValue = 0.0;
    boolean relativeStart = false;
    boolean relativeEnd = false;

    /**
     * Creates a new instance of LinearGenerator.
     */
    public LinearGenerator()
    {
        setIcon("LinearGenerator.png");
        panel = new LinearControlPanel(this);
    }

    @Override
    public double getValue(double time)
    {
        double y1 = startValue;
        if( relativeStart == true )
        {
            y1 += getInitialValue();
        }

        double y2 = endValue;
        if( relativeEnd == true )
        {
            y2 += y1;
        }

        return y1 + time * (y2-y1) / getDuration();
    }

    @Override
    public void saveGeneratorSettings(OutputStream out)
    throws IOException
    {
        StringBuilder start = new StringBuilder("<start");
        start.append(" value=\"").append(String.valueOf(startValue)).append("\"");
        start.append(" relative=\"").append(Boolean.toString(relativeStart)).append("\"");
        start.append("/>\r\n");
        out.write( start.toString().getBytes() );

        StringBuilder end = new StringBuilder("<end");
        end.append(" value=\"").append(String.valueOf(endValue)).append("\"");
        end.append(" relative=\"").append(Boolean.toString(relativeEnd)).append("\"");
        end.append("/>\r\n");
        out.write( end.toString().getBytes() );
    }

    @Override
    public void loadGeneratorSettings(NodeList childNodes)
    {
        Node startNode = XMLTools.getNode(childNodes, "start");
        loadStart(startNode);

        Node endNode = XMLTools.getNode(childNodes, "end");
        loadEnd(endNode);
    }

    private void loadEnd(Node node)
    {
        // read attributes from xml document
        String endVal = XMLTools.getAttribute("value", node);
        String endRel = XMLTools.getAttribute("relative", node);

        // setup generator's values
        endValue = Double.parseDouble(endVal);
        relativeEnd = Boolean.parseBoolean(endRel);

        // update generator's panel
        panel.endTextField.setText( String.valueOf(endVal) );
        panel.endRelativeCheckBox.setSelected(relativeEnd);
    }

    private void loadStart(Node node)
    {
        // read attributes from xml document
        String startVal = XMLTools.getAttribute("value", node);
        String startRel = XMLTools.getAttribute("relative", node);

        // setup generator's values
        startValue = Double.parseDouble(startVal);
        relativeStart = Boolean.parseBoolean(startRel);
        
        // update generator's panel
        panel.startTextField.setText( String.valueOf(startValue) );
        panel.startRelativeCheckBox.setSelected(relativeStart);
    }

    @Override
    public JPanel getControlPanel()
    {
        return panel;
    }

}
