/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

/**
 *
 * @author avincon
 */
public class AutomationHistory
{
    private double values[] = new double[0];
    private int cursor = 0;
    private boolean full = false;

    public AutomationHistory()
    {
    }

    public void init(int nbPoints)
    {
        values = new double[nbPoints];
        cursor = 0;
        full = false;
    }

    public void push(double value)
    {
        System.out.println("push "+value+" at "+cursor);
        values[cursor] = value;
        cursor = (cursor+1)%values.length;
        if(cursor==0)
        {
            full=true;
        }
    }

    public int getHistoryDepth()
    {
        return values.length;
    }

    public int getValuesCount()
    {
        if( full==false )
        {
            return cursor;
        }
        return values.length;
    }

    public double getValue(int index)
    {
        return values[index];
    }

    
    public double[] getValues()
    {
        return values;
    }
}
