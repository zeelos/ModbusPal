/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 * Defines the subset of the MODBUS specification that is currently
 * supported by ModbusPal.
 *
 * @author nnovic
 */
public interface ModbusConst
{
    public static final byte FC_READ_COILS = (byte)0x01;
    public static final byte FC_READ_DISCRETE_INPUTS = (byte)0x02;
    public static final byte FC_READ_HOLDING_REGISTERS = (byte)0x03;
    public static final byte FC_WRITE_SINGLE_COIL = (byte)0x05;
    public static final byte FC_WRITE_SINGLE_REGISTER = (byte)0x06;
    public static final byte FC_WRITE_MULTIPLE_COILS = (byte)0x0F;
    public static final byte FC_WRITE_MULTIPLE_REGISTERS = (byte)0x10;
    public static final byte FC_READ_FILE_RECORD = (byte)0x14;
    public static final byte FC_READ_EXTENDED_REGISTERS = (byte)0x14;
    public static final byte FC_WRITE_FILE_RECORD = (byte)0x15;
    public static final byte FC_READ_WRITE_MULTIPLE_REGISTERS = (byte)0x17;

    public static final byte XC_SUCCESSFUL = (byte)0x00;
    public static final byte XC_ILLEGAL_FUNCTION = (byte)0x01;
    public static final byte XC_ILLEGAL_DATA_ADDRESS = (byte)0x02;
    public static final byte XC_ILLEGAL_DATA_VALUE = (byte)0x03;
    public static final byte XC_SLAVE_DEVICE_FAILURE = (byte)0x04;

    public static final int IMPLEMENTATION_MODBUS = 0;
    public static final int IMPLEMENTATION_JBUS = 1;

    public static final int FIRST_MODBUS_SLAVE = 1;
    public static final int LAST_MODBUS_SLAVE = 247;
    public static final int MAX_MODBUS_SLAVE = LAST_MODBUS_SLAVE+1;

    public static final byte USER_DEFINED_FUNCTION_CODES[] = {65,66,67,68,69,70,71,72,73,74,75,100,101,102,103,104,105,106,107,108,109,110};
}
