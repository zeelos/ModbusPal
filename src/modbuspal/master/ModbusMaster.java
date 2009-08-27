/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.master;

import modbuspal.main.ModbusRequest;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.link.ModbusLink;

/**
 *
 * @author nnovic
 */
public class ModbusMaster
implements Runnable
{
    private ArrayList<ModbusRequest> requests = new ArrayList<ModbusRequest>();
    private Thread thread = null;
    private boolean quit = false;
    private double reqPeriod = 1.0;
    private ArrayList<MasterListener> masterListeners = new ArrayList<MasterListener>();
    private ModbusLink modbusLink = null;


    //
    //
    // SETUP
    //
    //

    public ModbusMaster()
    {
    }

    
    public void setLink(ModbusLink link)
    {
        modbusLink = link;
        if( modbusLink==null )
        {
            stop();
            notifyLinkIsLost();
        }
        else
        {
            notifyLinkHasBeenSetup();
        }
    }

    //
    //
    // REQUESTS
    //
    //

    void addRequest(ModbusRequest request)
    {
        requests.add(request);
    }

    
    //
    //
    // THREAD
    //
    //


    void start()
    {
        if(thread==null)
        {
            thread = new Thread(this,"master");
            quit = false;
            thread.start();
        }
    }

    void stop()
    {
        if( thread != null )
        {
            quit = true;
            synchronized(this)
            {
                notifyAll();
            }
            try
            {
                thread.join( (long)(reqPeriod*2000.0) );
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(ModbusMaster.class.getName()).log(Level.SEVERE, null, ex);
            }
            thread=null;
        }
    }

    public void run()
    {
        System.out.println("start master thread");

        // Get requests
        ModbusRequest reqList[] = new ModbusRequest[requests.size()];
        reqList = requests.toArray(reqList);

        // init:
        int currentIndex = 0;

        notifyMasterHasStarted();

        while( quit==false )
        {
            // get request to request:
            ModbusRequest currentReq = reqList[currentIndex];

            // send request
            modbusLink.execute(currentReq);
            notifyRequestTransmitted(currentReq);
            
            try {
                // receive reply
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ModbusMaster.class.getName()).log(Level.SEVERE, null, ex);
            }
            notifyReplyReceived(currentReq);

            if( quit == false )
            {
                try
                {
                    currentIndex++;
                    if( currentIndex >= reqList.length )
                    {
                        currentIndex = 0;
                        Thread.sleep( (long)(1*1000.0) );
                    }
                    else
                    {
                        Thread.sleep( (long)(1*1000.0) );
                    }
                }
                catch(Exception e)
                {
                    
                }
            }
        }


        System.out.println("end of master thread");
        if( quit==true )
        {
            quit = false;
        }
        else
        {
            thread=null;
        }
        notifyMasterHasEnded();
    }


    //
    //
    // EVENTS
    //
    //


    public void addMasterListener(MasterListener l)
    {
        assert( masterListeners.contains(l) == false );
        masterListeners.add(l);
    }

    public void removeMasterListener(MasterListener l)
    {
        masterListeners.remove(l);
    }

    private void notifyLinkHasBeenSetup()
    {
        for(MasterListener l:masterListeners)
        {
            l.masterLinkHasBeenSetup(this);
        }
    }

    private void notifyLinkIsLost()
    {
        for(MasterListener l:masterListeners)
        {
            l.masterLinkIsLost(this);
        }
    }


    private void notifyMasterHasEnded()
    {
        for(MasterListener l:masterListeners)
        {
            l.masterHasEnded(this);
        }
    }

    private void notifyMasterHasStarted()
    {
        for(MasterListener l:masterListeners)
        {
            l.masterHasStarted(this);
        }
    }

    private void notifyReplyReceived(ModbusRequest request)
    {
        for(MasterListener l:masterListeners)
        {
            l.replyReceived(request);
        }
    }

    private void notifyRequestTransmitted(ModbusRequest request)
    {
        for(MasterListener l:masterListeners)
        {
            l.requestTransmitted(request);
        }
    }

}
