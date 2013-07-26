/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import javax.swing.tree.DefaultMutableTreeNode;

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
}
