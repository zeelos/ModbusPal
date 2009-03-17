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
    private int tiltCount=0;
    private boolean execute=false;
    private Thread thread=null;
    private ImageIcon grayIcon;
    private ImageIcon greenIcon;

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
        grayIcon = new ImageIcon(grayIconUrl);
        greenIcon = new ImageIcon(greenIconUrl);
    }

    public void start()
    {
        execute=true;
        thread = new Thread(this);
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

    public void tilt()
    {
        synchronized(this)
        {
            tiltCount++;
        }
    }

    public void run()
    {
        boolean tilted = false;

        while(execute==true)
        {
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(TiltLabel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            synchronized(this)
            {
                if( tiltCount > 0 )
                {
                    if( tilted==false )
                    {
                        setIcon(greenIcon);
                        tilted = true;
                    }
                }
                else
                {
                    if( tilted==true )
                    {
                        setIcon(grayIcon);
                        tilted=false;
                    }
                }
                tiltCount=0;
            }
        }
        setIcon(grayIcon);
    }

}
