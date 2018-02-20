/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author nnovic
 */
class TiltLabel
extends JLabel
implements Runnable
{
    //private int tiltCount=0;
    public static final int RED = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;
    private int tiltColor=0;
    private boolean execute=false;
    private Thread thread=null;
    private ImageIcon grayIcon;
    private ImageIcon greenIcon;
    private ImageIcon yellowIcon;
    private ImageIcon redIcon;

    public TiltLabel()
    {
        super();
        loadImages();
        setIcon(grayIcon);
    }

    @Override
    public void setText(String text)
    {
        return;
    }


    private void loadImages()
    {
        URL grayIconUrl = getClass().getResource("img/grayTilt.png");
        URL greenIconUrl = getClass().getResource("img/greenTilt.png");
        URL yellowIconUrl = getClass().getResource("img/yellowTilt.png");
        URL redIconUrl = getClass().getResource("img/redTilt.png");
        grayIcon = new ImageIcon(grayIconUrl);
        greenIcon = new ImageIcon(greenIconUrl);
        yellowIcon = new ImageIcon(yellowIconUrl);
        redIcon = new ImageIcon(redIconUrl);

    }

    public void start()
    {
        execute=true;
        thread = new Thread(this,"tilt");
        thread.start();
    }


    public void stop()
    {
        execute=false;
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(TiltLabel.class.getName()).log(Level.SEVERE, null, ex);
        }
        thread=null;
    }

    public void tilt(int c)
    {
        synchronized(this)
        {
            tiltColor=c;
        }
    }

    @Override
    public void run()
    {
        boolean tilted = false;

        while(execute==true)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(TiltLabel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            synchronized(this)
            {
                if( tilted==true )
                {
                    setIcon(grayIcon);
                    tilted=false;
                }
                else
                {
                    tilted = true;
                    switch(tiltColor)
                    {
                        case RED: setIcon(redIcon); break;
                        case YELLOW: setIcon(yellowIcon); break;
                        case GREEN: setIcon(greenIcon); break;
                        default: tilted = false; break;
                    }
                }
                tiltColor=0;
            }
        } // end of while
        setIcon(grayIcon);
    }

}
