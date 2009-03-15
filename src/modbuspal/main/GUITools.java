/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.awt.Component;
import java.awt.Frame;

/**
 *
 * @author avincon
 */
public class GUITools
{
    /**
     * Moves a component so that it is located in the middle of the
     * boundaries of another component. In most cases, this method is used
     * to aligndialogs and frames.
     * @param parent the reference for aligning the child component
     * @param child the component that must be aligned
     */
    public static void align(Component parent, Component child)
    {
        int w = parent.getWidth()-child.getWidth();
        int h = parent.getHeight()-child.getHeight();
        child.setLocation(parent.getX()+w/2, parent.getY()+h/2);
    }

    public static Frame findFrame(Component c)
    {
        while( c!=null )
        {
            if( c instanceof Frame )
            {
                return (Frame)c;
            }
            c = c.getParent();
        }
        return null;
    }

}
