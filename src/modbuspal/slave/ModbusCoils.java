/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 *
 * @author avincon
 */
public class ModbusCoils
extends ModbusRegisters
{
    @Override
    protected Integer checkValueBoundaries(Integer value)
    {
        if( value<0 )
        {
            return 0;
        }
        if( value>1 )
        {
            return 1;
        }
        return value;
    }
}
