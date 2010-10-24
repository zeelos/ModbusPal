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

import javax.swing.UIManager;

/**
 *
 * @author avincon
 */
public class ModbusPalGui
{

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
                ModbusPal.newFrame("default").setVisible(true);
            }
        });
    }

}
