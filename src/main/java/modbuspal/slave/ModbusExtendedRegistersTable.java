/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author nnovic
 */
class ModbusExtendedRegistersTable
extends JTable
{
    private ModbusExtendedRegisters registers;

    public ModbusExtendedRegistersTable(ModbusExtendedRegisters model)
    {
        super(model);
    }

    @Override
    public ModbusExtendedRegisters getModel()
    {
        return registers;
    }


    int findColumn(String name)
    {
        for(int i=0; i<getColumnCount(); i++)
        {
            if( getColumnName(i).compareTo(name) == 0 )
            {
                return i;
            }
        }
        throw new ArrayIndexOutOfBoundsException("no column named "+name);
    }

    public int[] getSelectedAddresses()
    {
        int rows[] = getSelectedRows();
        int addresses[] = new int[ rows.length ];

        int col = findColumn(ModbusExtendedRegisters.ADDRESS_COLUMN_NAME);
        for(int i=0; i<rows.length; i++ )
        {
            addresses[i] = (Integer)getValueAt(rows[i], col) - registers.getOffset();
        }

        return addresses;
    }

    public int getSelectedAddress()
    {
        int row = getSelectedRow();
        int col = findColumn(ModbusExtendedRegisters.ADDRESS_COLUMN_NAME);
        return (Integer)getValueAt(row, col) - registers.getOffset();
    }


    @Override
    public void setModel(TableModel dataModel)
    {
        registers = (ModbusExtendedRegisters)dataModel;
        super.setModel(dataModel);
    }

    public int getAddressAt(int row)
    {
        int col = findColumn(ModbusExtendedRegisters.ADDRESS_COLUMN_NAME);
        return (Integer)getValueAt(row, col) - registers.getOffset();
    }

}
