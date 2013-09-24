/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import modbuspal.main.ErrorMessage;

/**
 * various tools for Swing
 * @author nnovic
 */
public class GUITools
{
    
    /**
     * Calls the setEnabled() method of the specified component. Then, if the
     * component is actually a Container, this method will descend into the
     * container's hierarchy to do the same recursively on all the children.
     * @param comp the component to enable or disabled
     * @param b true to enable, false to disable
     */
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
    public static void align(Component parent, Window child)
    {
        Point p = parent.getLocationOnScreen();
        int w = parent.getWidth()-child.getWidth();
        int h = parent.getHeight()-child.getHeight();
        child.setLocation(p.x+w/2, p.y+h/2);
    }

    /**
     * Searches up all the parents of the specifed component until a Frame
     * is found and returns null. If the root component is reached and no Frame was found,
     * returns nulls.
     * @param c the component for which a Frame must be searched
     * @return the parent Frame or null.
     */
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


    /**
     * Displays a dialog warning the user that a required file was not found
     * at the excepted location. The user has the possibility to ignore the
     * warning, or to open a FileChooser to locate the file manually.
     * @param parent the parent of the dialogs
     * @param f the missing file
     * @return the replacement file selected by the user, or null.
     */
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
    
    
    
    public static void underline(JLabel label)
    {
        Font font = label.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
    }
}
