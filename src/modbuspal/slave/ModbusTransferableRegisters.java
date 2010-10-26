/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.table.TableModel;
import modbuspal.slave.ModbusRegisters.RegisterCopy;

/**
 *
 * @author nnovic
 */
class ModbusTransferableRegisters
implements Transferable
{

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(ModbusRegisters.class,"registers");
    public static final DataFlavor SUPPORTED_DATA_FLAVORS[] = { DATA_FLAVOR };
    private ArrayList<RegisterCopy> registers = new ArrayList<RegisterCopy>();


    public ModbusTransferableRegisters(ModbusRegistersTable table)
    {
        TableModel model = table.getModel();
        if( model instanceof ModbusRegisters )
        {
            ModbusRegisters source = (ModbusRegisters)model;

            int addresses[] = table.getSelectedAddresses();
            for(int i=0; i<addresses.length; i++)
            {
                RegisterCopy copy = source.copy( source, addresses[i] );
                registers.add(copy);
            }
        }
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return SUPPORTED_DATA_FLAVORS;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(DATA_FLAVOR);
    }

    public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException, IOException
    {
        if( flavor.equals(DATA_FLAVOR)==false )
        {
            throw new UnsupportedFlavorException(flavor);
        }
        return registers;
    }

    public int getRegistersCount()
    {
        return registers.size();
    }
}
