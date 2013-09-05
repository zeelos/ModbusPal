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
    
    private byte functionCode;
    private int readStartAddress;
    private int quantityToRead;
    
    
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
}
