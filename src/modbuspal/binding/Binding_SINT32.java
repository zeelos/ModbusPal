/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

/**
 * The SINT32 binding class
 * @author nnovic
 */
public class Binding_SINT32
extends Binding
{

    /**
     * Creates a new instance of Binding_SINT32.
     */
    public Binding_SINT32()
    {
        super();
    }


    @Override
    public int getSize()
    {
        return 32;
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

        // return "most significant word" of the int
        else if( rank == 1 )
        {
            return 0xFFFF & (val >> 16 );
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
