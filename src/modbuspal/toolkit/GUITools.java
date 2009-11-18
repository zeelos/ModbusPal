/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.io.File;
import javax.swing.JFileChooser;
import modbuspal.main.ErrorMessage;

/**
 *
 * @author nnovic
 */
public class GUITools
{
    public static void setAllEnabled(Component comp, boolean b)
    {
        comp.setEnabled(b);

        if( comp instanceof Container )
        {
            Container ct = (Container)comp;
            Component[] comps = ct.getComponents();
            for( int i=0; i<comps.length; i++ )
            {
                setAllEnabled(comps[i],b);
            }
        }

    }

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


    public static File promptUserFileNotFound(Component parent, File f)
    {
        ErrorMessage msg = new ErrorMessage(2);
        msg.setTitle("Script not found");
        msg.append("The following file was not found:\r\n");
        msg.append(f.getPath()+"\r\n");
        msg.append("Use 'Browse' to select its new location or 'Abort' to skip loading this script.\r\n");
        msg.setButton(0, "Browse");
        msg.setButton(1, "Abort");
        msg.setVisible(true);

        if( msg.getButton()==0 )
        {
            JFileChooser fc = new JFileChooser( f.getParent() );
            fc.showDialog(parent, "Locate "+f.getName() );
            return fc.getSelectedFile();
        }

        return null;
    }
}
