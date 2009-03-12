/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 *
 * @author avincon
 */
public class ModbusRequest
implements ModbusConst
{
    private int slaveId;
    private int startingAddress;
    private int requestedQuantity;
    
    public ModbusRequest(int slave, int fc, int address, int quantity)
    {
        slaveId = slave;
        startingAddress = address;
        requestedQuantity = quantity;
    }

    public byte getFunctionCode()
    {
        return FC_READ_HOLDING_REGISTERS;
    }

    public String getFunction()
    {
        return MODBUS_FUNCTIONS[ getFunctionCode() ];
    }

    public int getSlaveId()
    {
        return slaveId;
    }

    public int getAddress()
    {
        return startingAddress;
    }

    public int getQuantity()
    {
        return requestedQuantity;
    }
}
