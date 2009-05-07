/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

/**
 *
 * @author nnovic
 */
public class Binding_FLOAT32
extends Binding
{

    public Binding_FLOAT32()
    {
        super();
    }


    @Override
    public int getSize()
    {
        return 32;
    }

    @Override
    protected int getRegister(int rank, double value)
    {
        // get current value and cast it as an int
        float conv = (float)value;
        int val = Float.floatToRawIntBits(conv);
        
        // return "less significant word" of the int
        if( rank == 0 )
        {
            return (val << 16) >> 16;
        }

        // return "most significant word" of the int
        else if( rank == 1 )
        {
            return (val >> 16 );
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
