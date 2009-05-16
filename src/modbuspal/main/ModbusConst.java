/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 *
 * @author nnovic
 */
public interface ModbusConst
{
    public static final byte FC_READ_HOLDING_REGISTERS = (byte)0x03;
    public static final byte FC_WRITE_MULTIPLE_REGISTERS = (byte)0x10;

    public static final String MODBUS_FUNCTIONS[] = {
        "(0x00)",
        "(0x01)",
        "(0x02)",
        "(0x03) Read holding registers",
    };

    public static final byte XC_SUCCESSFUL = (byte)0x00;
    public static final byte XC_ILLEGAL_FUNCTION = (byte)0x01;
    public static final byte XC_ILLEGAL_DATA_ADDRESS = (byte)0x02;
    public static final byte XC_ILLEGAL_DATA_VALUE = (byte)0x03;
    public static final byte XC_SLAVE_DEVICE_FAILURE = (byte)0x04;

    public static final int IMPLEMENTATION_MODBUS = 0;
    public static final int IMPLEMENTATION_JBUS = 1;

    public static final int FIRST_MODBUS_SLAVE = 1;
    public static final int LAST_MODBUS_SLAVE = 247;
}
