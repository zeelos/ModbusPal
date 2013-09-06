/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import javax.swing.tree.DefaultMutableTreeNode;
import modbuspal.toolkit.ModbusTools;

/**
 *
 * @author JMC15
 */
public class ModbusMasterRequest 
extends DefaultMutableTreeNode
{       
    public static ModbusMasterRequest getReadHoldingRegistersRequest(int startingAddress, int quantityOfRegisters)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x03;
        output.readStartAddress = startingAddress;
        output.quantityToRead = quantityOfRegisters;
        
        String caption = String.format("3 (0x03) Read holding registers (starting address = %d, quantity of registers = %d)", startingAddress, quantityOfRegisters);
        output.setUserObject(caption);
        return output;
    }
    
    public static ModbusMasterRequest getWriteMultipleRegistersRequest(int startingAddress, int quantityOfRegisters)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x10;
        output.writeStartAddress = startingAddress;
        output.quantityToWrite = quantityOfRegisters;
        
        String caption = String.format("16 (0x10) Write multiple registers (starting address = %d, quantity of registers = %d)", startingAddress, quantityOfRegisters);
        output.setUserObject(caption);
        return output;
    }    
    
    private byte functionCode;
    private int readStartAddress;
    private int quantityToRead;
    private int writeStartAddress;
    private int quantityToWrite;
    
    public byte getFunctionCode()
    {
        return functionCode;
    }
    
    public int getReadAddress()
    {
        return readStartAddress;
    }
    
    public int getReadQuantity()
    {
        return quantityToRead;
    }
    
    public int getWriteAddress()
    {
        return writeStartAddress;
    }
    
    public int getWriteQuantity()
    {
        return quantityToWrite;
    }
    
    public int toBytes(byte[] output, int offset)
    {
        switch(functionCode)
        {
            default: throw new UnsupportedOperationException();  
            case 0x03: return generateReadHoldingRegistersRequest(output, offset);
        }
    }
    
    
    private int generateReadHoldingRegistersRequest(byte[] output, int offset)
    {
        ModbusTools.setUint8(output, offset+0, 0x03); // function code
        ModbusTools.setUint16(output, offset+1, readStartAddress); // starting address
        ModbusTools.setUint16(output, offset+3, quantityToRead); // quantity of registers
        return 5;
    }

    public void notifyPDUprocessed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void notifyExceptionResponse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void notifyPDUnotServiced() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
