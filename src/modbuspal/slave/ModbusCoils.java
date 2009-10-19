/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import modbuspal.binding.Binding;

/**
 *
 * @author avincon
 */
public class ModbusCoils
extends ModbusRegisters
{
    public ModbusCoils()
    {
        super();
        TXT_REGISTER = "coil";
        TXT_REGISTERS = "coils";
    }



    @Override
    protected int getValue(Binding binding)
    {
        if( binding.getCoil()==true )
        {
            return 1;
        }
        return 0;
    }


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
