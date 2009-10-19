/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 *
 * @author avincon
 */
public class ModbusValuesMap
{
    private int indexedValues[] = new int[65536];
    private boolean existingValues[] = new boolean[65536];
    private int orderedIndexes[] = new int[65536];
    private int indexCount = 0;

    public void delete(int index)
    {
        if( index<0 )
        {
            return;
        }

        if( index > 65535 )
        {
            return;
        }

        if( existingValues[index]==true )
        {
            int order = getOrderOf(index);
            for( int i=order; i<indexCount-1; i++)
            {
                orderedIndexes[i] = orderedIndexes[i+1];
            }
            existingValues[index]=false;
            indexCount--;
        }
    }

    public void addIndex(int index)
    {
        if( existingValues[index]==false )
        {
            existingValues[index]=true;
            indexedValues[index] = 0;
            orderedIndexes[indexCount] = index;
            indexCount++;
            assert(indexCount<=65536);
        }
    }

    public void addIndexes(int index, int quantity)
    {
        if( index < 0 )
        {
            index=0;
        }

        if( index > 65535 )
        {
            index = 65535;
        }


        if( (index+quantity) > 65536 )
        {
            quantity = 65536-index;
        }

        if( quantity < 0 )
        {
            quantity = 0;
        }

        for( int i=0; i<quantity; i++ )
        {
            addIndex(index+i);
        }
    }

    public void putByIndex(int index, int value)
    {
        if( index<0 )
        {
            return;
        }

        if( index > 65535)
        {
            return;
        }

        if( existingValues[index]==false )
        {
            return;
        }

        indexedValues[index] = value;

    }

    public int getByIndex(int index)
    {
        if( index<0 )
        {
            return 0;
        }

        if( index > 65535)
        {
            return 0;
        }

        if( existingValues[index]==false )
        {
            return 0;
        }

        return indexedValues[index];
    }


    public void clear()
    {
        existingValues = new boolean[65536];
        indexCount=0;
    }

    public boolean indexExists(int index)
    {
        if( index<0 )
        {
            return false;
        }

        if( index>65535)
        {
            return false;
        }

        return existingValues[index];
    }

    public int getIndexOf(int order)
    {
        if( order >= indexCount )
        {
            order= indexCount-1;
        }

        if( order<0 )
        {
            return 0;
        }

        return orderedIndexes[order];
    }

    public int getOrderOf(int index)
    {
        if(index<0)
        {
            index=0;
        }

        if( index>65535 )
        {
            index=65535;
        }

        if( existingValues[index]==false )
        {
            return 0;
        }

        for(int i=0; i<indexCount; i++ )
        {
            if( orderedIndexes[i]==index )
            {
                return i;
            }
        }

        return 0;
    }


    public int getCount()
    {
        return indexCount;
    }
}
