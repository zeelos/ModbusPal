/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.io.IOException;
import java.util.Enumeration;
import gnu.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import modbuspal.main.ModbusPalProject;
import modbuspal.main.ModbusRequest;
import modbuspal.toolkit.ModbusTools;

/**
 * The serial link waits for incoming requests from a COM port
 * @author nnovic
 */
public class ModbusSerialLink
extends ModbusSlaveProcessor
implements ModbusLink, Runnable, SerialPortEventListener
{
    /** identifier to specify that there is no parity for the serial communication */
    public static final int PARITY_NONE = 0;
    
    /** identifier to specify that the odd parity must be used for the serial communication */
    public static final int PARITY_ODD = 1;
    
    /** identifier to specify that the even parity must be used for the serial communication */
    public static final int PARITY_EVEN = 2;

    public static final int STOP_BITS_1 = 0;

    public static final int STOP_BITS_1_5 = 1;

    public static final int STOP_BITS_2 = 2;
    
    private static ArrayList<CommPortIdentifier> commPorts = new ArrayList<CommPortIdentifier>();

    /**
     * This method will check that the specified com port actually exists.
     * @param comId a string containing a COM port name.
     * @return true if the COM port exists.
     */
    public static boolean exists(String comId)
    {
        for(int i=0; i<commPorts.size(); i++)
        {
            CommPortIdentifier commPort = commPorts.get(i);
            if( commPort.getName().compareTo(comId)==0 )
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 
     */
    public static class CommPortList
    implements ComboBoxModel
    {
        private Object selectedItem;
        CommPortList()
        {
            if( commPorts.size()>=1 )
            {
                selectedItem = commPorts.get(0).getName();
            }
        }
        @Override
        public int getSize() 
        {
            return commPorts.size();
        }
        @Override
        public Object getElementAt(int index)
        {
            return commPorts.get(index).getName();
        }
        @Override
        public void addListDataListener(ListDataListener l)
        {
        }
        @Override
        public void removeListDataListener(ListDataListener l)
        {
        }
        @Override
        public void setSelectedItem(Object anItem)
        {
            selectedItem = anItem;
        }
        @Override
        public Object getSelectedItem()
        {
            return selectedItem;
        }
    }

    /**
     * Returns the list of available COM ports on the host system.
     * @return list of available COM ports
     */
    public static CommPortList getListOfCommPorts()
    {
        return new CommPortList();
    }

    /**
     * Setup the RXTX library and scan the available COM ports
     */
    public static void install()
    {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while( portList.hasMoreElements() )
        {
            CommPortIdentifier com = (CommPortIdentifier)portList.nextElement();
            if( com.getPortType()==CommPortIdentifier.PORT_SERIAL )
            {
                System.out.println("Found "+com.getName() );
                commPorts.add(com);
            }
        }
    }
    private int serialStopBits;
    private SerialPort serialPort;
    private int baudrate;
    private InputStream input;
    private OutputStream output;
    private boolean executeThread=false;
    private Thread serverThread;
    private int serialParity;
    private int flowControl;
    private ModbusLinkListener listener = null;

    /**
     * Creates a new instance of ModbusSerialLink.
     * @param mpp the modbuspal project that holds the slaves information
     * @param index index of the COM port to sue for communication
     * @param speed baudrate of the COM port
     * @param parity parity of the communication
     * @param xonxoff enables or disables XON/XOFF 
     * @param rtscts enables or disables RTS/CTS
     * @throws PortInUseException
     * @throws ClassCastException 
     */
    public ModbusSerialLink(ModbusPalProject mpp, int index, int speed, int parity, int stopBits, boolean xonxoff, boolean rtscts)
    throws PortInUseException, ClassCastException
    {
        super(mpp);

        CommPortIdentifier comm = commPorts.get(index);
        serialPort = (SerialPort)(comm.open("MODBUSPAL",3000));
        baudrate = speed;

        switch(parity)
        {
            case PARITY_NONE:
                serialParity=SerialPort.PARITY_NONE;
                break;
            case PARITY_ODD:
                serialParity=SerialPort.PARITY_ODD;
                break;
            default:
            case PARITY_EVEN:
                serialParity=SerialPort.PARITY_EVEN;
                break;
        }

        switch(stopBits)
        {
            default:
            case STOP_BITS_1:
                serialStopBits=SerialPort.STOPBITS_1;
                break;
            case STOP_BITS_1_5:
                serialStopBits=SerialPort.STOPBITS_1_5;
                break;
            case STOP_BITS_2:
                serialStopBits=SerialPort.STOPBITS_2;
                break;
        }
        
        flowControl = SerialPort.FLOWCONTROL_NONE;
        if( xonxoff==true )
        {
            flowControl |= SerialPort.FLOWCONTROL_XONXOFF_IN;
            flowControl |= SerialPort.FLOWCONTROL_XONXOFF_OUT;
        }
        if( rtscts==true )
        {
            flowControl |= SerialPort.FLOWCONTROL_RTSCTS_IN;
            flowControl |= SerialPort.FLOWCONTROL_RTSCTS_OUT;
        }
    }

    @Override
    public void start(ModbusLinkListener l)
    throws IOException
    {
        listener = l;

        try
        {
            serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, serialStopBits, serialParity);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            System.out.println("Connected to com port");
        }
        catch( TooManyListenersException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (UnsupportedCommOperationException ex)
        {
            throw new RuntimeException(ex);
        }

        executeThread = true;
        serverThread = new Thread(this,"serial link");
        serverThread.start();
    }


    @Override
    public void stop()
    {
        executeThread = false;
        serverThread.interrupt();

        try
        {
            input.close();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(ModbusSerialLink.class.getName()).log(Level.SEVERE, null, ex);
        }

        try
        {
            output.close();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(ModbusSerialLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        serialPort.close();

        try
        {
            serverThread.join();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ModbusTcpIpLink.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            serverThread = null;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0)
    {
        synchronized(input)
        {
            input.notify();
        }
    }

    static int computeCRC(byte[] buffer, int offset, int length)
    {
        // Load a 16–bit register with FFFF hex (all 1’s). This is the CRC
        // register.
        int CRC = 0xFFFF;

        for( int i=0; i<length; i++ )
        {
            // Exclusive OR the first 8–bit byte of the message with the
            // low–order byte of the 16–bit CRC register, putting the result
            // in the CRC register.
            int b = buffer[offset+i] & 0xFF;
            CRC = (CRC ^ b) & 0xFFFF;
            
            for( int j=0; j<8; j++ )
            {
                int LSB = CRC & 1;
                CRC = (CRC >> 1) ;
                if( LSB==1 )
                {
                    CRC = (CRC ^ 0xA001) & 0xFFFF;
                }
            }
        }
        return CRC;
    }

    @Override
    public void run()
    {
        byte buffer[] = new byte[256];
        System.out.println("Start ModbusSerialLink");

        while(executeThread == true)
        {
            try
            {
                // wait until a notification is issued by the SerialEvent
                // callback
                synchronized(input)
                {
                    input.wait(1000);
                }

                // if some data is available then:
                if( input.available() >= 1 )
                {
                    // read all available data
                    int totalLen = input.read(buffer);

                    // read slave address (it is the first byte)
                    int slaveID = ModbusTools.getUint8(buffer,0);

                    // read crc value (located in the last two bytes
                    int crcLSB = ModbusTools.getUint8(buffer, totalLen-2);
                    int crcMSB = ModbusTools.getUint8(buffer, totalLen-1);
                    int receivedCRC = crcMSB * 256 + crcLSB;

                    // compute crc between slave address (included) and crc (excluded)
                    int computedCRC = computeCRC(buffer,0,totalLen-2);

                    int pduLength = totalLen - 3; // 1 for slave address, and 2 for CRC

                    // if CRC are ok, then process the pdu
                    if( receivedCRC == computedCRC )
                    {
                        //System.out.println("read "+ totalLen + " bytes");
                        pduLength = processPDU(slaveID, buffer, 1, pduLength);
                    }

                    else
                    {
                        // handle CRC error with exception code
                        pduLength = makeExceptionResponse(XC_SLAVE_DEVICE_FAILURE, buffer, 1);
                    }

                    // if the output pdu length is positive, then send the content
                    // of the buffer
                    if( pduLength > 0 )
                    {
                        totalLen = 1+ pduLength + 2; // 1 for slave address, and 2 for CRC

                        // compute crc of outgoing reply
                        int outputCRC = computeCRC(buffer,0,totalLen-2);

                        // low order byte of the CRC must be transmitted first
                        buffer[totalLen-2] = (byte)(outputCRC & 0xFF);
                        buffer[totalLen-1] = (byte)((outputCRC>>8) & 0xFF);

                        // write content of buffer into the output stream
                        output.write(buffer, 0, totalLen);
                        output.flush();
                    }
                }
            }
            catch( InterruptedException ex)
            {
                // not an error
            }
            catch (IOException ex)
            {
                if( Thread.interrupted() == false )
                {
                    Logger.getLogger(ModbusSerialLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        System.out.println("Stop ModbusSerialLink");
        listener.linkBroken();
        listener = null;
    }



    @Override
    public void execute(ModbusRequest req)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
