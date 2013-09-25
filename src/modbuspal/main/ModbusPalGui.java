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
import modbuspal.link.ModbusSerialLink;

/**
 * Utilitary methods for creating new instances of ModbusPal
 * @author nnovic
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

    
    public static void install()
    {
        ModbusSerialLink.install();
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) 
    {
        boolean runInstall = false;
        boolean runGui = true;
        
        if( args.length>=1 )
        {
            for(String arg:args)
            {
                if( arg.compareToIgnoreCase("-install")==0 )
                {
                    runInstall = true;
                    runGui = false;
                }
            }
        }
        if( runInstall == true )
        {
            install();
        }
        
        if( runGui == true )
        {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setNativeLookAndFeel();
                    newFrame().setVisible(true);
                }
            });
        }
        
        
    }



    /**
     * A JinternalFrame that contains a ModbusPalPane.
     */
    public static class ModbusPalInternalFrame
    extends JInternalFrame
    implements InternalFrameListener
    {
        final ModbusPalPane modbusPal;

        /**
         * Creates a new instance of ModbusPalInternalFrame
         */
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

        @Override
        public void internalFrameOpened(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            modbusPal.exit();
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
        }
    }

    /**
     * A JFrame with a ModbusPalPane inside
     */
    public static class ModbusPalFrame
    extends JFrame
    {
        final ModbusPalPane modbusPal;

        /**
         * Creates a new instance of ModbusPalFrame
         */
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


    /**
     * Creates a ModbusPalFrame. The internal
     * console of ModbusPal is enabled.
     * @return a new ModbusPalFrame
     */
    public static JFrame newFrame()
    {
        ModbusPalFrame frame = new ModbusPalFrame();
        return frame;
    }

    /**
     * Creates a ModbusPalInternalFrame. The internal
     * console of ModbusPal is enabled.
     * @return a new ModbusPalInternalFrame
     */
    public static JInternalFrame newInternalFrame()
    {
        ModbusPalInternalFrame iframe = new ModbusPalInternalFrame();
        return iframe;
    }

    /**
     * Creates a ModbusPalPane instance. The internal
     * console of ModbusPal is disabled.
     * @return a new ModbusPalPane instance
     */
    public static ModbusPalPane newInstance()
    {
        return new ModbusPalPane(false);
    }


    /** 
     * Returns a ModbusPalPane instance that is associated with
     * the specified key, as in a HashMap. If there is no ModusPalPane
     * associated with that key, a new one is created. Otherwise, the existing
     * one is returned
     * @param key any object that can be used to uniquely identified a particular
     * ModbusPalPane instance. usually a String.
     * @return The ModbusPalPane instance identified by the key
     */
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
