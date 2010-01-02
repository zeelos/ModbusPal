/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.generator.sine;

import modbuspal.generator.Generator;
import javax.swing.JPanel;

/**
 *
 * @author nnovic
 */
public class SineGenerator
extends Generator
{
    private SineControlPanel panel;
    private double initialAngle = 0.0;
    double amplitude = 1.0;
    double period = 1.0;
    boolean catchup = false;

    public SineGenerator()
    {
        setIcon("SineGenerator.png");
        panel = new SineControlPanel(this);
    }

    @Override
    public double getValue(double time)
    {
        double angle = (2*Math.PI) / period;
        return amplitude * Math.sin( initialAngle+time * angle );
    }

    @Override
    public void setInitialValue(double value)
    {
        super.setInitialValue(value);
        if( value > amplitude )
        {
            initialAngle = 2*Math.PI;
        }
        else if( value < -amplitude )
        {
            initialAngle = -2*Math.PI;
        }
        else
        {
            initialAngle = Math.asin(value/amplitude);
        }
    }

    /*@Override
    protected void saveSettings(OutputStream out)
    throws IOException
    {
        StringBuffer start = new StringBuffer("<start");
        start.append(" value=\""+String.valueOf(startValue)+"\"");
        start.append(" relative=\""+Boolean.toString(relativeStart)+"\"");
        start.append("/>\r\n");
        out.write( start.toString().getBytes() );

        StringBuffer end = new StringBuffer("<end");
        end.append(" value=\""+String.valueOf(endValue)+"\"");
        end.append(" relative=\""+Boolean.toString(relativeEnd)+"\"");
        end.append("/>\r\n");
        out.write( end.toString().getBytes() );
    }*/

    /*
    @Override
    protected void loadSettings(NodeList childNodes)
    {
        Node startNode = XMLTools.getNode(childNodes, "start");
        loadStart(startNode);

        Node endNode = XMLTools.getNode(childNodes, "end");
        loadEnd(endNode);
    }
    */

    /*
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
    }*/

    @Override
    public JPanel getControlPanel()
    {
        return panel;
    }

}
