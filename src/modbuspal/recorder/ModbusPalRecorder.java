/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.toolkit.HexaTools;
import modbuspal.toolkit.XFileChooser;

/**
 *
 * @author avincon
 */
public class ModbusPalRecorder
implements Runnable
{
    private static ModbusPalRecorder uniqInst = new ModbusPalRecorder();
    private static Date start = new Date();

    public static void recordIncoming(int slaveID, byte[] buffer, int offset, int pduLength)
    {
        uniqInst.record("in", slaveID, buffer, offset, pduLength);
    }

    public static void recordOutgoing(int slaveID, byte[] buffer, int offset, int pduLength)
    {
        uniqInst.record("out", slaveID, buffer, offset, pduLength);
    }


    private PipedInputStream input;
    private PipedOutputStream output;
    private boolean running;
    private FileWriter fileWriter=null;
    
    ModbusPalRecorder()
    {
        output = new PipedOutputStream();
        Thread thread = new Thread(this);
        running=true;
        thread.start();
    }


    synchronized void setOutput(FileWriter writer)
    throws IOException
    {
        if( fileWriter!=null )
        {
            fileWriter.close();
        }
        fileWriter = writer;
    }

    @Override
    public void run()
    {

        try
        {
            // create input reader that the recorder will use to get
            // data that has to be written into the record file.
            input = new PipedInputStream(output, 1024 * 1024);
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader buffer = new BufferedReader(reader);

            System.out.println("Start recorder");
            
            while (running == true)
            {
                try 
                {
                    // read a line from the pipe
                    String line = buffer.readLine();

                    // if recording is enabled...
                    if (fileWriter != null)
                    {
                        fileWriter.write(line+"\r\n");
                    }
                } 
                catch (IOException ex)
                {
                    System.out.println("pipe broken, recreate pipe");
                    input = new PipedInputStream(output, 1024 * 1024);
                    reader = new InputStreamReader(input);
                    buffer = new BufferedReader(reader);
                }
            }

            try
            {
                buffer.close();
                reader.close();
                input.close();
            } 
            catch (IOException ex)
            {
                Logger.getLogger(ModbusPalRecorder.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                System.out.println("Stop recorder");
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(ModbusPalRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void start()
    throws IOException
    {
        XFileChooser fc = new XFileChooser(XFileChooser.RECORDER_FILE);
        fc.showSaveDialog(null);
        File destFile = fc.getSelectedFile();
        System.out.println("recording into "+destFile.getPath());
        uniqInst.setOutput( new FileWriter(destFile) );
    }


    public static void stop()
    throws IOException
    {
        uniqInst.setOutput(null);
    }

    private synchronized void record(String tag, int slaveID, byte[] buffer, int offset, int pduLength)
    {
        Date now = new Date();
        long timestamp = now.getTime() - start.getTime();

        String open = String.format("<%s timestamp=%d slave=%d>", tag, timestamp, slaveID);
        String hexa = HexaTools.toHexa(buffer, offset, pduLength);
        String close = String.format("</%s>\r\n",tag);

        try
        {
            output.write(open.getBytes());
            output.write(hexa.getBytes());
            output.write(close.getBytes());
        } 
        catch (IOException ex)
        {
            Logger.getLogger(ModbusPalRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
