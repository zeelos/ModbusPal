/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

import javax.swing.tree.DefaultMutableTreeNode;
import modbuspal.main.ModbusPalGui;
import modbuspal.main.ModbusPalProject;
import modbuspal.slave.ModbusSlaveAddress;
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

    public static ModbusMasterRequest getReadExtendedRegistersRequest(int startingAddress, int quantityOfRegisters)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x14;
        output.readStartAddress = startingAddress;
        output.quantityToRead = quantityOfRegisters;

        String caption = String.format("20 (0x14) Read extended registers (starting address = %d, quantity of registers = %d)", startingAddress, quantityOfRegisters);
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

    public static ModbusMasterRequest getReadCoilsRequest(int startingAddress, int quantityToRead)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x01;
        output.readStartAddress = startingAddress;
        output.quantityToRead = quantityToRead;

        String caption = String.format("1 (0x01) Read coils (starting address = %d, quantity of coils = %d)", startingAddress, quantityToRead);
        output.setUserObject(caption);
        return output;
    }

    public static ModbusMasterRequest getReadDiscreteInputsRequest(int startingAddress, int quantityToRead)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x02;
        output.readStartAddress = startingAddress;
        output.quantityToRead = quantityToRead;

        String caption = String.format("2 (0x02) Read discrete inputs (starting address = %d, quantity of inputs = %d)", startingAddress, quantityToRead);
        output.setUserObject(caption);
        return output;
    }

    public static ModbusMasterRequest getWriteSingleCoilRequest(int outputAddress)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x05;
        output.writeStartAddress = outputAddress;
        output.quantityToWrite = 1;

        String caption = String.format("5 (0x05) Write single coil (output address = %d)", outputAddress);
        output.setUserObject(caption);
        return output;
    }

    public static ModbusMasterRequest getWriteSingleRegisterRequest(int registerAddress)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x06;
        output.writeStartAddress = registerAddress;
        output.quantityToWrite = 1;

        String caption = String.format("6 (0x06) Write single register (register address = %d)", registerAddress);
        output.setUserObject(caption);
        return output;
    }

    public static ModbusMasterRequest getWriteMultipleCoilsRequest(int startingAddress, int quantityOfOutputs)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x0F;
        output.writeStartAddress = startingAddress;
        output.quantityToWrite = quantityOfOutputs;

        String caption = String.format("15 (0x0F) Write multiple coils (starting address = %d, quantity of coils = %d)", startingAddress, quantityOfOutputs);
        output.setUserObject(caption);
        return output;
    }

    public static ModbusMasterRequest getReadWriteMultipleRegistersRequest(int readStartingAddress, int quantityToRead, int writeStartingAddress, int quantityToWrite)
    {
        ModbusMasterRequest output = new ModbusMasterRequest();
        output.functionCode = 0x17;
        output.readStartAddress = readStartingAddress;
        output.quantityToRead = quantityToRead;
        output.writeStartAddress = writeStartingAddress;
        output.quantityToWrite = quantityToWrite;

        String caption = String.format("23 (0x17) Read/Write multiple registers (read starting address = %d, quantity to read = %d, write starting address = %d, quantity to write = %d)",
                readStartingAddress, quantityToRead,
                writeStartingAddress, quantityToWrite);

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

    public void notifyPDUprocessed() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void notifyExceptionResponse() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void notifyPDUnotServiced() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
