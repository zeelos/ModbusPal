/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

/**
 * The SINT16 binding class
 * @author nnovic
 */
public class Binding_SINT16
extends Binding
{

    /**
     * Creates a new instance of Binding_SINT16.
     */
    public Binding_SINT16()
    {
        super();
    }


    @Override
    public int getSize()
    {
        return 16;
    }


    @Override
    public int getRegister(int rank, double value)
    {
        // get current value and cast it as an int
        int val = (int)value;

        // return "less significant word" of the int
        if( rank == 0 )
        {
            return 0xFFFF & (val);
        }

        // else, return 0 if value is positive, or FFFF if value is negative
        else
        {
            if( val >= 0 )
            {
                return 0x0000;
            }
            else
            {
                return 0xFFFF;
            }
        }
    }
}
