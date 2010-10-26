/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModbusPalFrame.java
 *
 * Created on 22 oct. 2010, 10:48:15
 */

package modbuspal.main;

import java.awt.BorderLayout;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author avincon
 */
public class ModbusPalGui
{

    private static final HashMap<Object,ModbusPalPane> instances = new HashMap<Object,ModbusPalPane>();

    /**
     * this method will try to change the Look and Feel of the applcation,
     * using the system l&f. It means that the application will get the Windows
     * l&f on Windows, etc...
     */
    private static void setNativeLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
          System.out.println("Error setting native LAF: " + e);
        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                setNativeLookAndFeel();
                newFrame().setVisible(true);
            }
        });
    }




    public static class ModbusPalInternalFrame
    extends JInternalFrame
    implements InternalFrameListener
    {
        final ModbusPalPane modbusPal;

        public ModbusPalInternalFrame()
        {
            setTitle(ModbusPalPane.APP_STRING);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(false);
            add( modbusPal, BorderLayout.CENTER );
            pack();
            addInternalFrameListener(this);
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setFrameIcon( new ImageIcon(image2) );
        }

        public void internalFrameOpened(InternalFrameEvent e) {
        }

        public void internalFrameClosing(InternalFrameEvent e) {
            modbusPal.exit();
        }

        public void internalFrameClosed(InternalFrameEvent e) {
        }

        public void internalFrameIconified(InternalFrameEvent e) {
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
        }

        public void internalFrameActivated(InternalFrameEvent e) {
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
        }
    }


    public static class ModbusPalFrame
    extends JFrame
    {
        final ModbusPalPane modbusPal;

        public ModbusPalFrame()
        {
            setTitle(ModbusPalPane.APP_STRING);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(true);
            add( modbusPal, BorderLayout.CENTER );
            pack();
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setIconImage(image2);
        }
    }


    public static ModbusPalFrame newFrame()
    {
        ModbusPalFrame frame = new ModbusPalFrame();
        return frame;
    }

    public static ModbusPalPane getInstance(Object key)
    {
        if( key==null )
        {
            throw new NullPointerException();
        }

        if( instances.containsKey(key)==false )
        {
            instances.put(key, new ModbusPalPane(false) );
        }

        return instances.get(key);
    }


}
