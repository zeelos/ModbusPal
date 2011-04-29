/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 * This class has been created especially to handle the registers of a 
 * MODBUS slave.
 * @author nnovic
 */
public class ModbusValuesMap
{
    private int indexedValues[] = new int[65536];
    private boolean existingValues[] = new boolean[65536];
    private int orderedIndexes[] = new int[65536];
    private int indexCount = 0;

    /**
     * Mark the register specified by "index" has removed.
     * @param index index (0-65535) of the register
     */
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

    /**
     * Mark the register specified by "index" as added
     * @param index index (0-65535) of the register to add
     */
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

    /**
     * Mark a range of registers as added.
     * @param index index (0-65535) of the first register in the range
     * @param quantity number of registers to add
     */
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

    /**
     * Assigns the specified value to the register identified by its index.
     * @param index index (0-65535) of the register
     * @param value value to assign to the register
     */
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

    /**
     * Returns the value assigned to the register, identified by its index.
     * @param index index (0-65535) of the register
     * @return value of that register. 0 if the register is not marked as added.
     */
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

    /**
     * Marks all registers as removed.
     */
    public void clear()
    {
        existingValues = new boolean[65536];
        indexCount=0;
    }

    /**
     * Checks if the register with the specified index has been marked
     * as added.
     * @param index index (0-65535) of the register to check
     * @return true if the register is marked as added.
     */
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


    /**
     * Returns the register index corresponding to the order at which
     * it has been added in the map. For example, if the first index added
     * to the map was register #135, then getIndexOf(0) returns 135.
     * @param order the order for which the index is requested
     * @return the index of the register
     */
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

    /**
     * Get the order of the register identified by the specified index.
     * For example, if the first register added to the map was #135,
     * then getOrderOf(135) returns 0;
     * @param index the index of the register
     * @return the order of the register
     */
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

    /**
     * Gets the number of registers defined in the map
     * @return number of registers defined in the map
     */
    public int getCount()
    {
        return indexCount;
    }
}
