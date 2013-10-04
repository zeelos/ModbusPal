/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JPanel;
import modbuspal.instanciator.Instantiable;
import modbuspal.master.ModbusMasterRequest;
import org.w3c.dom.NodeList;

/**
 * The interface that a class must implement in order to
 * create a new modbus function manager
 * @author nnovic
 */
public interface ModbusPduProcessor
extends Instantiable<ModbusPduProcessor>
{
    /**
     * This function is called to process the *REQUEST* PDU contained in the provided
     * byte buffer, where the data starts at the specified offset.
     * After processing the content of the *REQUEST*, the reply must be written
     * into the same buffer, starting at the same offset. Note that offset+0
     * contains the function code, and should not be modified (unless the
     * reply is an exception).
     * The createIfNotExist indicates if the "Learn mode" is enabled or not.
     * When the "Learn mode" is enabled, the resources that might be missing to
     * complete the request should be created, so that the request can be serviced.
     * @param functionCode the function code that triggered the call to this PDU processor instance
     * @param slaveID the MODBUS slave address that is the target of the request
     * @param buffer the byte buffer containing the request, and where the reply must be written
     * @param offset offset where the actual data starts in the buffer.
     * @param createIfNotExist true is the "Learn mode" is active, false otherwise.
     * @return the length of the reply. if the returned length is less than or equal to
     * zero, then ModbusPal will react like there was no reply to the request. do not
     * forget that the reply is at least one byte long, because the first byte
     * contains the function code.
     */
    public int processPDU(byte functionCode, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist);

    /**
     * This function is called to process the *REPLY* PDU contained in the provided
     * byte buffer, where the data starts at the specified offset.
     * The createIfNotExist indicates if the "Learn mode" is enabled or not.
     * When the "Learn mode" is enabled, the resources that might be missing to
     * complete the request should be created, so that the request can be serviced.
     * @param functionCode the function code that triggered the call to this PDU processor instance
     * @param slaveID the MODBUS slave address that is the target of the request
     * @param buffer the byte buffer containing the request, and where the reply must be written
     * @param offset offset where the actual data starts in the buffer.
     * @param createIfNotExist true is the "Learn mode" is active, false otherwise.
     * @return true if the data has been processed successfully, false otherwise.
     */
    public boolean processPDU(ModbusMasterRequest mmr, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist);
    
    
    public int buildPDU(ModbusMasterRequest mmr, ModbusSlaveAddress slaveID, byte[] buffer, int offset, boolean createIfNotExist);
    
    /**
     * Returns a JPanel that is designed to graphically reflect the settings
     * of the PduProcessor. Those settings will be, most of the time, various
     * values that will compose the reply to the request. 
     * @return a JPanel to edit the settings of the PduProcessor
     */
    public JPanel getPduPane();

    /**
     * saves the parameter of the instance into the provided output stream,
     * in XML format.
     * @param out
     * @throws IOException
     */
    public void savePduProcessorSettings(OutputStream out) throws IOException;

    /**
     * XML nodes that may contain settings that were previously saved
     * with saveSettings()
     * @param list
     */
    public void loadPduProcessorSettings(NodeList list);

}
